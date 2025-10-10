<template>
  <div class="chat-container">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <button class="back-btn" @click="$router.back()">← 返回</button>
      <h2>🧠 AI 超级智能体</h2>
      <p>具备多种工具和功能的强大AI智能体</p>
    </div>
    
    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(message, index) in messages" :key="index" :class="['message', message.type]">
        <div class="message-content" v-if="message.type === 'user'">
          {{ message.content }}
          <div class="message-actions">
            <button class="btn-link" @click="copy(message.content)">复制</button>
          </div>
        </div>
        <div class="message-content" v-else>
          <div v-html="message.html || message.content"></div>
          <!-- Step内容显示区域 -->
          <div v-if="message.stepContent" class="step-content">
            <div v-html="message.stepHtml || message.stepContent"></div>
          </div>
          <div class="message-actions">
            <button class="btn-link" @click="copy(message.content)">复制</button>
          </div>
        </div>
      </div>
      
      <!-- 加载指示器 -->
      <div v-if="isLoading" class="message ai">
        <div class="message-content">
          <div class="loading"></div>
          AI智能体正在处理中...
        </div>
      </div>
    </div>
    
    <!-- 输入区域 -->
    <div class="chat-input">
      <input
        v-model="inputMessage"
        @keyup.enter="sendMessage"
        placeholder="请输入您的问题或任务..."
        :disabled="isLoading"
      />
      <button @click="sendMessage" :disabled="!inputMessage.trim() || isLoading">发送</button>
      <button class="stop-btn" @click="stopStream" :disabled="!isLoading">停止回答</button>
    </div>
  </div>
</template>

<script>
import { ApiService } from '../services/api.js'
import { renderMarkdown } from '../utils/markdown.js'
import { copyText } from '../utils/clipboard.js'
import { showToast } from '../utils/toast.js'

export default {
  name: 'ManusAgent',
  data() {
    return {
      messages: [],
      inputMessage: '',
      isLoading: false,
      eventSource: null,
      hasCompleted: false
    }
  },
  
  mounted() {
    // 添加欢迎消息
    this.messages.push({
      type: 'ai',
      content: '您好！我是AI超级智能体，拥有多种工具和功能，可以帮您处理各种复杂的任务。请告诉我您需要什么帮助？',
      html: renderMarkdown('您好！我是AI超级智能体，拥有多种工具和功能，可以帮您处理各种复杂的任务。请告诉我您需要什么帮助？')
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
        if (this.eventSource) {
          try { this.eventSource.close() } catch (_) {}
          this.eventSource = null
        }
        // 创建SSE连接
        this.eventSource = ApiService.createManusSSE(
          message,
          this.handleSSEMessage,
          this.handleSSEError,
          this.handleSSEComplete
        )
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
      // ManusAgent: 流式返回，将消息追加到最后一条AI消息
      // 如果最后一条消息是AI消息，则追加内容
      if (this.messages.length > 0 && this.messages[this.messages.length - 1].type === 'ai') {
        const target = this.messages[this.messages.length - 1]
        
        // 检查是否包含Step内容
        const stepMatch = data.match(/Step\d+:\s*[^\n]*(?:\n[^\n]*)*/g)
        if (stepMatch) {
          // 如果有Step内容，将其分离
          const stepContent = stepMatch.join('\n').trim()
          const mainContent = data.replace(/Step\d+:\s*[^\n]*(?:\n[^\n]*)*/g, '').trim()
          
          // 更新主内容
          if (mainContent) {
            target.content += mainContent
          }
          
          // 更新Step内容
          if (target.stepContent) {
            target.stepContent += '\n' + stepContent
          } else {
            target.stepContent = stepContent
          }
          
          target.html = renderMarkdown(target.content)
          target.stepHtml = renderMarkdown(target.stepContent)
        } else {
          // 普通内容直接追加
          target.content += data
          target.html = renderMarkdown(target.content)
        }
      } else {
        // 否则创建新的AI消息
        const aiMsg = {
          type: 'ai',
          content: '',
          html: '',
          stepContent: '',
          stepHtml: ''
        }
        
        // 检查是否包含Step内容
        const stepMatch = data.match(/Step\d+:\s*[^]*?(?=\n|$)/g)
        if (stepMatch) {
          const stepContent = stepMatch.join(' ').trim()
          const mainContent = data.replace(/Step\d+:\s*[^]*?(?=\n|$)/g, '').trim()
          
          aiMsg.content = mainContent
          aiMsg.stepContent = stepContent
          aiMsg.html = renderMarkdown(mainContent)
          aiMsg.stepHtml = renderMarkdown(stepContent)
        } else {
          aiMsg.content = data
          aiMsg.html = renderMarkdown(data)
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
        content: '抱歉，智能体连接已断开，请稍后重试或刷新页面。'
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
          console.log('用户点击停止智能体回答，通过SSE断开向上游传播cancel信号')
          
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
        content: '⏹️ 智能体回答已停止'
      })
      this.scrollToBottom()
    }
  }
}
</script>
