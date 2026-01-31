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
    
    <!-- 聊天消息区域：用户向上滚动时不再自动跟到底部 -->
    <div class="chat-messages" ref="messagesContainer" @scroll="onMessagesScroll">
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
      
      <!-- RAG选项 -->
      <div class="rag-options">
        <div class="rag-toggle">
          <label class="rag-label">
            <input 
              type="checkbox" 
              v-model="useRAG" 
              :disabled="isLoading"
              class="rag-checkbox"
            />
            <span class="rag-text">
              <span class="rag-icon">🧠</span>
              启用RAG增强检索
            </span>
          </label>
          <div class="rag-description">
            {{ useRAG ? getRAGTypeDescription(ragType) : 'RAG模式将使用知识库检索来提供更准确的编程建议' }}
          </div>
        </div>
        
        <!-- RAG类型选择 -->
        <div 
          class="rag-type-selector" 
          v-if="useRAG"
          :class="{ 'collapsed': ragTypeSelectorCollapsed }"
          @touchstart="handleTouchStart"
          @touchmove="handleTouchMove"
          @touchend="handleTouchEnd"
        >
          <div class="rag-type-header" @click="toggleRAGTypeSelector">
            <label class="rag-type-label">
              <span class="rag-type-icon">📚</span>
              <span class="rag-type-text">知识库选择：</span>
            </label>
            <span class="collapse-indicator" :class="{ 'collapsed': ragTypeSelectorCollapsed }">
              {{ ragTypeSelectorCollapsed ? '展开' : '收起' }}
            </span>
          </div>
          <div class="rag-type-content" v-show="!ragTypeSelectorCollapsed">
            <select 
              v-model="ragType" 
              :disabled="isLoading"
              class="rag-type-select"
            >
              <option value="java">Java编程知识库</option>
              <option value="more">更多知识库待添加</option>
            </select>
            <div class="rag-type-description">
              {{ getRAGTypeDescription(ragType) }}
            </div>
          </div>
        </div>
      </div>
      
      <div class="input-footer">
        <div class="status-indicator" :class="{ 'active': isLoading }">
          <div class="status-dot"></div>
          <span class="status-text">{{ isLoading ? 'AI正在思考...' : '准备就绪' }}</span>
        </div>
        <div class="rag-status" v-if="useRAG">
          <span class="rag-badge">RAG模式已启用</span>
          <span class="rag-type-badge">{{ getRAGTypeName(ragType) }}</span>
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
      hasCompleted: false,
      useRAG: false,
      ragType: 'java',
      ragTypeSelectorCollapsed: false,
      touchStartY: 0,
      touchEndY: 0,
      /** 用户是否在底部附近（未向上滑动），用于决定新内容到来时是否自动滚到底部 */
      userAtBottom: true
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
    this.$nextTick(() => this.injectCopyCodeButtons())
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
      
      // 用户刚发送，跟随新回复到底部
      this.userAtBottom = true
      this.$nextTick(() => {
        this.scrollToBottom(true)
      })
      
      // 开始AI响应
      this.isLoading = true
      
      try {
        // 发送前若已有连接，先关闭，避免多路流重复输出
        if (this.eventSource) {
          try { this.eventSource.close() } catch (_) {}
          this.eventSource = null
        }
        // 根据RAG选项创建SSE连接
        if (this.useRAG) {
          this.eventSource = ApiService.createCodeHelperRAGSSE(
            message,
            this.chatId,
            this.handleSSEMessage,
            this.handleSSEError,
            this.handleSSEComplete
          )
        } else {
          this.eventSource = ApiService.createCodeHelperSSE(
            message,
            this.chatId,
            this.handleSSEMessage,
            this.handleSSEError,
            this.handleSSEComplete
          )
        }
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
        // 预处理换行符
        const processedData = data.replace(/\\n/g, '\n')
        target.content += processedData
        // 立即渲染，renderMarkdown 内部会处理不完整的 markdown 语法
        target.html = renderMarkdown(target.content)
      } else {
        // 否则创建新的AI消息
        // 预处理换行符
        const processedData = data.replace(/\\n/g, '\n')
        const aiMsg = {
          type: 'ai',
          content: processedData,
          html: renderMarkdown(processedData)
        }
        this.messages.push(aiMsg)
      }
      
      // 仅当用户仍在底部附近时才自动滚到底部，避免打断向上阅读；并为代码块注入复制按钮
      this.$nextTick(() => {
        this.scrollToBottom(false)
        this.injectCopyCodeButtons()
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
      this.scrollToBottom(true)
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
     * 消息区域滚动时更新「是否在底部」状态
     */
    onMessagesScroll() {
      const container = this.$refs.messagesContainer
      if (!container) return
      const threshold = 80
      const atBottom = container.scrollHeight - container.scrollTop - container.clientHeight <= threshold
      this.userAtBottom = atBottom
    },
    /**
     * 滚动到消息底部
     * @param {boolean} force - 为 true 时强制滚到底部（如发送消息、错误提示）；为 false 时仅当用户原本在底部才滚动
     */
    scrollToBottom(force = false) {
      const container = this.$refs.messagesContainer
      if (!container) return
      if (!force && !this.userAtBottom) return
      container.scrollTop = container.scrollHeight
      this.userAtBottom = true
    },
    /**
     * 复制指定文本
     */
    async copy(text) {
      await copyText(text)
      showToast('复制成功')
    },
    /**
     * 为消息区域内所有代码块注入「复制代码」按钮
     */
    injectCopyCodeButtons() {
      const container = this.$refs.messagesContainer
      if (!container) return
      const pres = container.querySelectorAll('.message-text pre')
      pres.forEach((pre) => {
        if (pre.closest('.code-block-wrapper')) return
        const wrapper = document.createElement('div')
        wrapper.className = 'code-block-wrapper'
        const btn = document.createElement('button')
        btn.type = 'button'
        btn.className = 'copy-code-btn'
        btn.textContent = '复制代码'
        btn.addEventListener('click', async () => {
          const codeEl = pre.querySelector('code')
          const text = codeEl ? codeEl.textContent : pre.textContent
          await copyText(text || '')
          showToast('复制成功')
        })
        pre.parentNode.insertBefore(wrapper, pre)
        wrapper.appendChild(pre)
        wrapper.appendChild(btn)
      })
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
      this.scrollToBottom(true)
    },
    
    /**
     * 获取RAG类型描述
     * @param {string} type - RAG类型
     * @returns {string} 描述文本
     */
    getRAGTypeDescription(type) {
      const descriptions = {
        java: '内置精选的 Java 编程知识，涵盖并发、集合、JVM 等常见问题',
        more: '更多专用知识库正在上线中，敬请期待'
      }
      return descriptions[type] || descriptions.java
    },
    
    /**
     * 获取RAG类型显示名称
     * @param {string} type - RAG类型
     * @returns {string} 显示名称
     */
    getRAGTypeName(type) {
      const names = {
        java: 'Java编程知识库',
        more: '更多知识库'
      }
      return names[type] || names.java
    },
    
    /**
     * 切换RAG类型选择器的折叠状态
     */
    toggleRAGTypeSelector() {
      this.ragTypeSelectorCollapsed = !this.ragTypeSelectorCollapsed
    },
    
    /**
     * 处理触摸开始事件
     * @param {TouchEvent} event - 触摸事件
     */
    handleTouchStart(event) {
      this.touchStartY = event.touches[0].clientY
    },
    
    /**
     * 处理触摸移动事件
     * @param {TouchEvent} event - 触摸事件
     */
    handleTouchMove(event) {
      // 阻止默认滚动行为，让我们的手势处理生效
      event.preventDefault()
    },
    
    /**
     * 处理触摸结束事件
     * @param {TouchEvent} event - 触摸事件
     */
    handleTouchEnd(event) {
      this.touchEndY = event.changedTouches[0].clientY
      const deltaY = this.touchStartY - this.touchEndY
      
      // 如果向下滑动超过50px，则折叠RAG类型选择器
      if (deltaY < -50 && !this.ragTypeSelectorCollapsed) {
        this.ragTypeSelectorCollapsed = true
      }
      // 如果向上滑动超过50px，则展开RAG类型选择器
      else if (deltaY > 50 && this.ragTypeSelectorCollapsed) {
        this.ragTypeSelectorCollapsed = false
      }
    }
  }
}
</script>
