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
            <h2>AI 超级智能体（体验版）</h2>
            <p>具备多种工具和功能的强大AI智能体</p>
            <div class="usage-limit">
              <span class="limit-text">体验版限制：仅可进行10次对话</span>
              <span class="remaining-count">剩余次数：{{ maxConversations - conversationCount }}</span>
            </div>
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
            <!-- Step内容显示区域 -->
            <div v-if="message.stepContent" class="steps-container">
              <div v-for="(step, stepIndex) in parseSteps(message.stepContent)" :key="stepIndex" 
                   class="step-box" :style="{ animationDelay: `${stepIndex * 0.2}s` }">
                <div class="step-header">
                  <span class="step-number">{{ stepIndex + 1 }}</span>
                  <span class="step-title">{{ step.title }}</span>
                </div>
                <div class="step-body" v-html="step.content"></div>
              </div>
            </div>
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
              <div class="loading-text">AI智能体正在处理中...</div>
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
            placeholder="请输入您的问题或任务..."
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
  name: 'ManusAgent',
  data() {
    return {
      messages: [],
      inputMessage: '',
      isLoading: false,
      eventSource: null,
      hasCompleted: false,
      processedMessages: new Set(), // 用于去重
      conversationCount: 0, // 对话次数计数器
      maxConversations: 10 // 最大对话次数
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
      
      // 检查对话次数限制
      if (this.conversationCount >= this.maxConversations) {
        showToast('体验版对话次数已达上限，请升级到完整版')
        return
      }
      
      const message = this.inputMessage.trim()
      this.inputMessage = ''
      
      // 添加用户消息
      this.messages.push({
        type: 'user',
        content: message
      })
      
      // 增加对话次数计数
      this.conversationCount++
      
      // 滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom()
      })
      
      // 开始AI响应
      this.isLoading = true
      
      // 清理已处理的消息记录，开始新的对话
      this.processedMessages.clear()
      
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
      // 检查是否已经处理过这个消息，避免重复
      const messageHash = this.hashMessage(data)
      if (this.processedMessages.has(messageHash)) {
        console.log('检测到重复消息，跳过处理:', data.substring(0, 50) + '...')
        return
      }
      this.processedMessages.add(messageHash)
      
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
          
          // 更新主内容，预处理换行符
          if (mainContent) {
            const processedMainContent = mainContent.replace(/\\n/g, '\n')
            target.content += processedMainContent
          }
          
          // 更新Step内容，预处理换行符
          if (target.stepContent) {
            const processedStepContent = stepContent.replace(/\\n/g, '\n')
            target.stepContent += '\n' + processedStepContent
          } else {
            const processedStepContent = stepContent.replace(/\\n/g, '\n')
            target.stepContent = processedStepContent
          }
          
          target.html = renderMarkdown(target.content)
          target.stepHtml = renderMarkdown(target.stepContent)
        } else {
          // 普通内容直接追加，预处理换行符
          const processedData = data.replace(/\\n/g, '\n')
          target.content += processedData
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
          
          // 预处理换行符
          const processedMainContent = mainContent.replace(/\\n/g, '\n')
          const processedStepContent = stepContent.replace(/\\n/g, '\n')
          
          aiMsg.content = processedMainContent
          aiMsg.stepContent = processedStepContent
          aiMsg.html = renderMarkdown(processedMainContent)
          aiMsg.stepHtml = renderMarkdown(processedStepContent)
        } else {
          // 预处理换行符
          const processedData = data.replace(/\\n/g, '\n')
          aiMsg.content = processedData
          aiMsg.html = renderMarkdown(processedData)
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
     * 生成消息的哈希值，用于去重
     * @param {string} message - 消息内容
     * @returns {string} 哈希值
     */
    hashMessage(message) {
      // 简单的哈希函数，用于去重
      let hash = 0
      for (let i = 0; i < message.length; i++) {
        const char = message.charCodeAt(i)
        hash = ((hash << 5) - hash) + char
        hash = hash & hash // 转换为32位整数
      }
      return hash.toString()
    },
    
    /**
     * 解析Step内容，将每个Step分离成独立的对象
     * @param {string} stepContent - 包含所有Step的字符串
     * @returns {Array} 解析后的Step数组
     */
    parseSteps(stepContent) {
      if (!stepContent) return []
      
      // 使用正则表达式匹配Step内容
      const stepRegex = /Step\s*(\d+):\s*([^\n]*(?:\n(?!Step\s*\d+:)[^\n]*)*)/g
      const steps = []
      let match
      
      while ((match = stepRegex.exec(stepContent)) !== null) {
        const stepNumber = match[1]
        const stepText = match[2].trim()
        
        // 提取标题（第一行）和内容
        const lines = stepText.split('\n')
        const title = lines[0] || `步骤 ${stepNumber}`
        const content = lines.slice(1).join('\n').trim() || stepText
        
        steps.push({
          number: stepNumber,
          title: title,
          content: renderMarkdown(content)
        })
      }
      
      return steps
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
