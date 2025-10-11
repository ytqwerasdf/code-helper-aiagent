<template>
  <div class="chat-container">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <button class="back-btn" @click="$router.back()">
        <span class="btn-icon">←</span>
        <span class="btn-text">返回</span>
      </button>
      <div class="header-content">
        <div class="ai-avatar-header">
          <div class="avatar-container">
            <div class="avatar-glow"></div>
            <span class="avatar-text">AI</span>
          </div>
          <div class="header-info">
            <h2>AI 编程助手</h2>
            <p>聊天室ID: {{ chatId }}</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(message, index) in messages" :key="index" :class="['message', message.type]" 
           :style="{ animationDelay: `${index * 0.1}s` }">
        <div class="message-content" v-if="message.type === 'user'">
          <div class="message-avatar user-avatar">
            <span class="avatar-icon">👤</span>
          </div>
          <div class="message-bubble user-bubble">
            <div class="message-text">{{ message.content }}</div>
          </div>
          <div class="message-actions">
            <button class="btn-link" @click="copy(message.content)">
              <span class="btn-icon">📋</span>
              复制
            </button>
          </div>
        </div>
        <div class="message-content" v-else>
          <div class="message-avatar ai-avatar">
            <div class="avatar-glow"></div>
            <span class="avatar-text">AI</span>
          </div>
          <div class="message-bubble ai-bubble">
            <div v-html="message.html || message.content" class="message-text"></div>
          </div>
          <div class="message-actions">
            <button class="btn-link" @click="copy(message.content)">
              <span class="btn-icon">📋</span>
              复制
            </button>
          </div>
        </div>
      </div>
      
      <!-- 加载指示器 -->
      <div v-if="isLoading" class="message ai loading-message">
        <div class="message-content">
          <div class="loading-container">
            <div class="ai-avatar">
              <div class="avatar-glow"></div>
              <span class="avatar-text">AI</span>
            </div>
            <div class="loading-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <div class="loading-text">AI正在思考中...</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 输入区域 -->
    <div class="chat-input">
      <div class="input-container">
        <div class="input-wrapper">
          <input
            v-model="inputMessage"
            @keyup.enter="sendMessage"
            placeholder="请输入您的编程问题..."
            :disabled="isLoading"
            class="modern-input"
          />
          <div class="input-border"></div>
          <div class="input-glow"></div>
        </div>
        <div class="button-group">
          <button @click="sendMessage" :disabled="!inputMessage.trim() || isLoading" 
                  class="send-btn" :class="{ 'pulse': !isLoading && inputMessage.trim() }">
            <span class="btn-content">
              <span class="btn-icon">🚀</span>
              <span class="btn-text">发送</span>
            </span>
            <div class="btn-ripple"></div>
          </button>
          <button class="stop-btn" @click="stopStream" :disabled="!isLoading">
            <span class="btn-content">
              <span class="btn-icon">⏹️</span>
              <span class="btn-text">停止</span>
            </span>
          </button>
        </div>
      </div>
      <div class="input-footer">
        <div class="status-indicator" :class="{ 'active': isLoading }">
          <div class="status-dot"></div>
          <span class="status-text">{{ isLoading ? 'AI正在思考...' : '准备就绪' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ApiService } from '../services/api.js'
import { renderMarkdown } from '../utils/markdown.js'
import { copyText } from '../utils/clipboard.js'
import { showToast } from '../utils/toast.js'

export default {
  name: 'CodeHelper',
  data() {
    return {
      chatId: '',
      messages: [],
      inputMessage: '',
      isLoading: false,
      eventSource: null,
      hasCompleted: false
    }
  },
  
  mounted() {
    // 生成聊天室ID
    this.chatId = ApiService.generateChatId()
    
    // 添加欢迎消息
    this.messages.push({
      type: 'ai',
      content: '您好！我是AI编程助手，有什么编程问题可以帮您解决吗？'
    })
  },
  
  beforeUnmount() {
    // 清理SSE连接
    if (this.eventSource) {
      this.eventSource.close()
    }
  },
  
  methods: {
    /**
     * 发送消息
     */
    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) {
        return
      }
      
      const message = this.inputMessage.trim()
      this.inputMessage = ''
      
      // 添加用户消息
      this.messages.push({
        type: 'user',
        content: message
      })
      
      // 滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom()
      })
      
      // 开始AI响应
      this.isLoading = true
      
      try {
        // 发送前若已有连接，先关闭，避免多路流重复输出
        if (this.eventSource) {
          try { this.eventSource.close() } catch (_) {}
          this.eventSource = null
        }
        // 创建SSE连接
        this.eventSource = ApiService.createCodeHelperSSE(
          message,
          this.chatId,
          this.handleSSEMessage,
          this.handleSSEError,
          this.handleSSEComplete
        )
        // 开始新一轮响应，重置完成标记
        this.hasCompleted = false
      } catch (error) {
        console.error('发送消息失败:', error)
        this.handleSSEError(error)
      }
    },
    
    /**
     * 处理SSE消息
     * @param {string} data - 接收到的数据
     */
    handleSSEMessage(data) {
      // 如果最后一条消息是AI消息，则追加内容
      if (this.messages.length > 0 && this.messages[this.messages.length - 1].type === 'ai') {
        const target = this.messages[this.messages.length - 1]
        target.content += data
        target.html = renderMarkdown(target.content)
      } else {
        // 否则创建新的AI消息
        const aiMsg = {
          type: 'ai',
          content: data,
          html: renderMarkdown(data)
        }
        this.messages.push(aiMsg)
      }
      
      // 滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom()
      })
    },
    
    /**
     * 处理SSE错误
     * @param {Error} error - 错误对象
     */
    handleSSEError(error) {
      console.log('SSE连接错误或异常结束')
      // 若已正常完成，则忽略后续 onerror
      if (this.hasCompleted) {
        return
      }
      // 检查是否为正常关闭，如果是则不显示错误提示
      if (this.eventSource && this.eventSource._normalClose) {
        console.log('SSE连接正常关闭，不显示错误提示')
        return
      }
      this.isLoading = false
      this.messages.push({
        type: 'ai',
        content: '抱歉，连接已断开，请稍后重试或刷新页面。'
      })
      this.scrollToBottom()
    },
    
    /**
     * 处理SSE完成
     */
    handleSSEComplete() {
      console.log('SSE连接正常关闭')
      this.isLoading = false
      this.hasCompleted = true
      this.eventSource = null
    },
    
    /**
     * 滚动到消息底部
     */
    scrollToBottom() {
      const container = this.$refs.messagesContainer
      if (container) {
        container.scrollTop = container.scrollHeight
      }
    },
    /**
     * 复制指定文本
     */
    async copy(text) {
      await copyText(text)
      showToast('复制成功')
    },
    /**
     * 停止当前 SSE 流
     * 关键：通过SSE连接断开来向上游传播cancel信号
     */
    async stopStream() {
      if (this.eventSource) {
        try {
          console.log('用户点击停止回答，通过SSE断开向上游传播cancel信号')
          
          // 使用新的取消机制，通过SSE断开传播cancel信号
          if (typeof this.eventSource.cancel === 'function') {
            await this.eventSource.cancel()
            console.log('SSE连接已关闭，cancel信号已向上游传播')
          } else {
            // 降级处理：直接关闭连接
            console.warn('使用降级处理方式关闭连接')
            this.eventSource.close()
          }
        } catch (error) {
          console.error('停止流时出错:', error)
          // 即使出错也要关闭连接
          try { this.eventSource.close() } catch (_) {}
        }
        this.eventSource = null
      }
      this.isLoading = false
      this.hasCompleted = true
      
      // 添加用户友好的提示消息
      this.messages.push({
        type: 'ai',
        content: '⏹️ 回答已停止'
      })
      this.scrollToBottom()
    }
  }
}
</script>
