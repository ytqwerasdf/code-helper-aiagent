<template>
  <div id="app">
    <!-- 页面切换动画容器 -->
    <transition name="page" mode="out-in">
      <router-view />
    </transition>
    
    <!-- 全局加载指示器 -->
    <div v-if="isLoading" class="global-loader">
      <div class="loader-content">
        <div class="loader-spinner"></div>
        <div class="loader-text">AI系统初始化中...</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'App',
  
  data() {
    return {
      isLoading: true
    }
  },
  
  mounted() {
    this.initSEO()
    
    // 模拟加载时间
    setTimeout(() => {
      this.isLoading = false
    }, 1000)
  },
  
  methods: {
    /**
     * 初始化SEO
     */
    initSEO() {
      // 设置页面标题和描述
      document.title = 'AI智能助手平台 - 专业的AI编程助手和文档助手'
      
      // 设置meta标签
      const metaDescription = document.querySelector('meta[name="description"]')
      if (metaDescription) {
        metaDescription.setAttribute('content', '专业的AI智能助手平台，提供编程助手和文档助手服务，支持代码生成、问题解答、文档处理等功能。')
      } else {
        const meta = document.createElement('meta')
        meta.name = 'description'
        meta.content = '专业的AI智能助手平台，提供编程助手和文档助手服务，支持代码生成、问题解答、文档处理等功能。'
        document.head.appendChild(meta)
      }
      
      // 设置关键词
      const metaKeywords = document.querySelector('meta[name="keywords"]')
      if (metaKeywords) {
        metaKeywords.setAttribute('content', 'AI助手,编程助手,文档助手,代码生成,人工智能,智能问答')
      } else {
        const meta = document.createElement('meta')
        meta.name = 'keywords'
        meta.content = 'AI助手,编程助手,文档助手,代码生成,人工智能,智能问答'
        document.head.appendChild(meta)
      }
      
      // 设置viewport
      const viewport = document.querySelector('meta[name="viewport"]')
      if (viewport) {
        viewport.setAttribute('content', 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no')
      }
    }
  }
}
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #333333;
  background: #ffffff;
  line-height: 1.6;
  font-size: 14px;
}

#app {
  min-height: 100vh;
  background: #ffffff;
}

/* 页面切换动画 */
.page-enter-active, .page-leave-active {
  transition: opacity 0.3s ease;
}

.page-enter-from, .page-leave-to {
  opacity: 0;
}

/* 全局加载指示器 */
.global-loader {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loader-content {
  text-align: center;
}

.loader-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #333333;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

.loader-text {
  color: #666666;
  font-size: 14px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 简约复古按钮样式 */
button {
  border: 1px solid #e5e5e5;
  background: #ffffff;
  cursor: pointer;
  font-family: inherit;
  color: #333333;
  border-radius: 6px;
  padding: 8px 16px;
  transition: all 0.2s ease;
}

button:hover {
  background: #f5f5f5;
  border-color: #d5d5d5;
}

/* 简约复古输入框样式 */
input, textarea {
  border: 1px solid #e5e5e5;
  outline: none;
  font-family: inherit;
  color: #333333;
  background: #ffffff;
  border-radius: 6px;
  padding: 8px 12px;
  transition: all 0.2s ease;
}

input:focus, textarea:focus {
  border-color: #0066cc;
  box-shadow: 0 0 0 2px rgba(0, 102, 204, 0.1);
}

/* 简约复古链接样式 */
a {
  color: #333333;
  text-decoration: none;
  transition: color 0.2s ease;
}

a:hover {
  color: #0066cc;
  text-decoration: none;
}

/* 代码高亮样式 - 模仿图片中的灰色背景框 */
code, .code-highlight {
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 2px 6px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: #1a1a1a;
}
</style>