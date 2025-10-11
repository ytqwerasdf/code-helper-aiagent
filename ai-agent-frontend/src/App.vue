<template>
  <div id="app">
    <!-- 全局背景粒子 -->
    <div class="global-particles" ref="globalParticles"></div>
    
    <!-- 全局网格背景 -->
    <div class="global-grid"></div>
    
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
      globalParticles: [],
      animationId: null,
      isLoading: true
    }
  },
  
  mounted() {
    this.initGlobalParticles()
    this.startGlobalAnimation()
    this.initSEO()
    
    // 模拟加载时间
    setTimeout(() => {
      this.isLoading = false
    }, 1500)
  },
  
  beforeUnmount() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId)
    }
  },
  
  methods: {
    initGlobalParticles() {
      const container = this.$refs.globalParticles
      if (!container) return
      
      // 创建全局粒子
      for (let i = 0; i < 30; i++) {
        this.globalParticles.push({
          x: Math.random() * window.innerWidth,
          y: Math.random() * window.innerHeight,
          vx: (Math.random() - 0.5) * 0.3,
          vy: (Math.random() - 0.5) * 0.3,
          size: Math.random() * 2 + 1,
          opacity: Math.random() * 0.3 + 0.1
        })
      }
    },
    
    startGlobalAnimation() {
      const animate = () => {
        this.updateGlobalParticles()
        this.renderGlobalParticles()
        this.animationId = requestAnimationFrame(animate)
      }
      animate()
    },
    
    updateGlobalParticles() {
      this.globalParticles.forEach(particle => {
        particle.x += particle.vx
        particle.y += particle.vy
        
        // 边界检测
        if (particle.x < 0 || particle.x > window.innerWidth) {
          particle.vx *= -1
        }
        if (particle.y < 0 || particle.y > window.innerHeight) {
          particle.vy *= -1
        }
        
        // 保持粒子在屏幕内
        particle.x = Math.max(0, Math.min(window.innerWidth, particle.x))
        particle.y = Math.max(0, Math.min(window.innerHeight, particle.y))
      })
    },
    
    renderGlobalParticles() {
      const container = this.$refs.globalParticles
      if (!container) return
      
      container.innerHTML = ''
      
      this.globalParticles.forEach(particle => {
        const element = document.createElement('div')
        element.className = 'global-particle'
        element.style.cssText = `
          position: absolute;
          left: ${particle.x}px;
          top: ${particle.y}px;
          width: ${particle.size}px;
          height: ${particle.size}px;
          background: radial-gradient(circle, rgba(0,255,0,${particle.opacity}) 0%, transparent 70%);
          border-radius: 50%;
          pointer-events: none;
        `
        container.appendChild(element)
      })
    },
    
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
#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #ffffff;
  height: 100vh;
  margin: 0;
  padding: 0;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 50%, #0d1117 100%);
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 50%, #0d1117 100%);
}

/* 全局粒子背景 */
.global-particles {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

/* 全局网格背景 */
.global-grid {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(0, 255, 0, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 0, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
  z-index: 0;
  opacity: 0.3;
}

/* 全局加载器 */
.global-loader {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 50%, #0d1117 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loader-content {
  text-align: center;
  color: #00ff00;
  font-family: 'Courier New', monospace;
}

.loader-spinner {
  width: 60px;
  height: 60px;
  border: 3px solid rgba(0, 255, 0, 0.3);
  border-top: 3px solid #00ff00;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

.loader-text {
  font-size: 1.2rem;
  font-weight: 600;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

/* 页面切换动画 */
.page-enter-active, .page-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.page-enter-from {
  opacity: 0;
  transform: translateX(100px) scale(0.95);
}

.page-leave-to {
  opacity: 0;
  transform: translateX(-100px) scale(0.95);
}


/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #00ffff, #ff00ff);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #00cccc, #ff00cc);
}
</style>

