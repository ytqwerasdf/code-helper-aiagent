import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8123/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * API服务类
 * 提供与后端AI服务的通信功能
 */
export class ApiService {
  // 存储活跃的SSE连接，用于取消操作
  static activeConnections = new Map()
  /**
   * 生成唯一的聊天室ID
   * @returns {string} 聊天室ID
   */
  static generateChatId() {
    return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
  }

  /**
   * 创建SSE连接用于AI编程助手
   * @param {string} message - 用户消息
   * @param {string} chatId - 聊天室ID
   * @param {Function} onMessage - 消息回调函数
   * @param {Function} onError - 错误回调函数
   * @param {Function} onComplete - 完成回调函数
   * @returns {EventSource} SSE连接对象
   */
  static createCodeHelperSSE(message, chatId, onMessage, onError, onComplete) {
    const url = `${api.defaults.baseURL}/ai/code_helper/chat/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}&_t=${Date.now()}`
    
    const eventSource = new EventSource(url, {
      withCredentials: false
    })
    const connectionId = `code_helper_${chatId}_${Date.now()}`
    
    // 存储连接信息，用于取消操作
    this.activeConnections.set(connectionId, {
      eventSource,
      chatId,
      type: 'code_helper'
    })
    
    // 连接建立事件
    eventSource.onopen = (event) => {
      console.log('SSE连接已建立')
      // 可以在这里添加连接建立后的处理逻辑
    }
    
    // 消息接收事件
    eventSource.onmessage = (event) => {
      try {
        let data = event.data
        // 处理字符编码问题，确保正确显示中文
        if (typeof data === 'string') {
          // 检查是否包含乱码，如果是则尝试修复
          if (data.includes('æ') || data.includes('è') || data.includes('å')) {
            try {
              // 使用更简单的方法处理编码问题
              data = unescape(encodeURIComponent(data))
            } catch (e) {
              console.warn('字符解码失败，使用原始数据:', e)
            }
          }
        }
        
        // 检查是否为结束标志
        if (data === '[DONE]' || data === '[[END]]') {
          console.log('SSE连接正常关闭')
          this.activeConnections.delete(connectionId)
          // 标记为正常关闭
          eventSource._normalClose = true
          onComplete && onComplete()
        } else {
          // 传递普通消息
          onMessage && onMessage(data)
        }
      } catch (error) {
        console.error('SSE消息解析错误:', error)
        onError && onError(error)
      }
    }
    
    // 连接错误事件
    eventSource.onerror = (error) => {
      console.log('SSE连接错误或异常结束')
      this.activeConnections.delete(connectionId)
      // 主动关闭以阻止浏览器自动重连，避免重复请求同一 message
      try { eventSource.close() } catch (_) {}
      onError && onError(error)
    }
    
    // 添加取消方法到eventSource对象
    eventSource.cancel = async () => {
      await this.cancelConnection(connectionId, chatId, 'code_helper')
    }
    
    // 标记连接状态，防止重复取消
    eventSource._isCancelling = false
    
    return eventSource
  }

  /**
   * 创建SSE连接用于AI超级智能体
   * @param {string} message - 用户消息
   * @param {Function} onMessage - 消息回调函数
   * @param {Function} onError - 错误回调函数
   * @param {Function} onComplete - 完成回调函数
   * @returns {EventSource} SSE连接对象
   */
  static createManusSSE(message, onMessage, onError, onComplete) {
    const url = `${api.defaults.baseURL}/ai/manus/chat?message=${encodeURIComponent(message)}&_t=${Date.now()}`
    
    const eventSource = new EventSource(url, {
      withCredentials: false
    })
    const connectionId = `manus_${Date.now()}`
    
    // 存储连接信息，用于取消操作
    this.activeConnections.set(connectionId, {
      eventSource,
      chatId: null, // Manus没有chatId
      type: 'manus'
    })
    
    // 连接建立事件
    eventSource.onopen = (event) => {
      console.log('SSE连接已建立')
      // 可以在这里添加连接建立后的处理逻辑
    }
    
    // 消息接收事件
    eventSource.onmessage = (event) => {
      try {
        let data = event.data
        // 处理字符编码问题，确保正确显示中文
        if (typeof data === 'string') {
          // 检查是否包含乱码，如果是则尝试修复
          if (data.includes('æ') || data.includes('è') || data.includes('å')) {
            try {
              // 使用更简单的方法处理编码问题
              data = unescape(encodeURIComponent(data))
            } catch (e) {
              console.warn('字符解码失败，使用原始数据:', e)
            }
          }
        }
        
        // 检查是否为结束标志
        if (data === '[DONE]' || data === '[[END]]') {
          console.log('SSE连接正常关闭')
          this.activeConnections.delete(connectionId)
          // 标记为正常关闭
          eventSource._normalClose = true
          onComplete && onComplete()
        } else {
          // 传递普通消息
          onMessage && onMessage(data)
        }
      } catch (error) {
        console.error('SSE消息解析错误:', error)
        onError && onError(error)
      }
    }
    
    // 连接错误事件
    eventSource.onerror = (error) => {
      console.log('SSE连接错误或异常结束')
      this.activeConnections.delete(connectionId)
      try { eventSource.close() } catch (_) {}
      onError && onError(error)
    }
    
    // 添加取消方法到eventSource对象
    eventSource.cancel = async () => {
      await this.cancelConnection(connectionId, null, 'manus')
    }
    
    // 标记连接状态，防止重复取消
    eventSource._isCancelling = false
    
    return eventSource
  }

  /**
   * 取消指定的SSE连接
   * 关键：通过SSE连接断开来向上游传播cancel信号，让后端的doOnCancel能够感知
   * @param {string} connectionId - 连接ID
   * @param {string} chatId - 聊天室ID（可选）
   * @param {string} type - 连接类型
   */
  static async cancelConnection(connectionId, chatId, type) {
    const connection = this.activeConnections.get(connectionId)
    if (!connection) {
      console.warn('连接不存在或已被取消:', connectionId)
      return
    }

    // 防止重复取消
    if (connection.eventSource._isCancelling) {
      console.warn('连接正在取消中，跳过重复请求:', connectionId)
      return
    }
    connection.eventSource._isCancelling = true

    try {
      console.log('开始取消连接，通过SSE断开向上游传播cancel信号:', connectionId)
      
      // 直接关闭SSE连接，这会向上游传播cancel信号
      // 后端的 Flux.merge(dataStream, heartbeat) 会感知到下游断开
      // 从而触发 doOnCancel 回调
      if (connection.eventSource && connection.eventSource.readyState !== EventSource.CLOSED) {
        connection.eventSource.close()
        console.log('SSE连接已关闭，cancel信号向上游传播:', connectionId)
      }

      // 从活跃连接中移除
      this.activeConnections.delete(connectionId)
      
      console.log('连接取消流程完成:', connectionId)
    } catch (error) {
      console.error('取消连接时出错:', error)
      // 即使出错也要清理连接
      if (connection.eventSource && connection.eventSource.readyState !== EventSource.CLOSED) {
        connection.eventSource.close()
      }
      this.activeConnections.delete(connectionId)
    }
  }

  /**
   * 取消所有活跃的连接
   */
  static cancelAllConnections() {
    for (const [connectionId, connection] of this.activeConnections) {
      this.cancelConnection(connectionId, connection.chatId, connection.type)
    }
  }
}

export default api
