<template>
  <div class="home-container">
    <!-- 页面头部 -->
    <div class="home-header">
      <div class="title-container">
        <h1 class="main-title">
          <span class="title-text">AI 智能助手平台</span>
        </h1>
        <p class="subtitle">专业的AI编程助手和文档助手服务</p>
      </div>
    </div>
    
    <!-- 功能卡片区域 -->
    <div class="features-grid">
      <div class="feature-card" @click="navigateTo('/code-helper')">
        <div class="card-content">
          <div class="feature-icon">
            <div class="icon-container">💻</div>
          </div>
          <h3>编程助手</h3>
          <p>专业的编程问题解答和代码生成服务，帮助您解决各种编程难题</p>
          <div class="feature-tags">
            <span class="tag">代码生成</span>
            <span class="tag">问题解答</span>
            <span class="tag">编程指导</span>
          </div>
        </div>
      </div>
      
      <div class="feature-card" @click="navigateTo('/manus-agent')">
        <div class="card-content">
          <div class="feature-icon">
            <div class="icon-container">📝</div>
          </div>
          <h3>文档助手</h3>
          <p>智能文档处理和内容生成，帮助您高效完成各类文档编写任务</p>
          <div class="feature-tags">
            <span class="tag">文档生成</span>
            <span class="tag">内容优化</span>
            <span class="tag">格式整理</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 首页组件
 * 提供AI助手平台的功能导航和介绍
 */
export default {
  name: 'Home',
  
  methods: {
    /**
     * 导航到指定路由
     * @param {string} path - 目标路由路径
     */
    navigateTo(path) {
      // 添加页面切换动画
      document.body.style.overflow = 'hidden'
      setTimeout(() => {
        this.$router.push(path)
      }, 300)
    }
  }
}
</script>

<style scoped>
/* 主容器 - 简约复古风格 */
.home-container {
  min-height: 100vh;
  background: #ffffff;
  padding: 2rem;
  color: #333333;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
}

/* 页面头部 */
.home-header {
  text-align: center;
  margin-bottom: 3rem;
}

.title-container {
  position: relative;
}

.main-title {
  font-size: 3rem;
  margin-bottom: 1rem;
  font-weight: 600;
  color: #333333;
}

.title-text {
  background: linear-gradient(45deg, #333333, #666666);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 1.2rem;
  color: #666666;
  max-width: 600px;
  margin: 0 auto;
}

/* 功能卡片区域 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.feature-card {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border-color: #d5d5d5;
}

.card-content {
  position: relative;
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1.5rem;
  display: inline-block;
}

.icon-container {
  position: relative;
}

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  font-weight: 500;
  color: #333333;
}

.feature-card p {
  margin-bottom: 1.5rem;
  line-height: 1.6;
  color: #666666;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.tag {
  background: #f5f5f5;
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  font-size: 0.875rem;
  border: 1px solid #e5e5e5;
  transition: all 0.2s ease;
  color: #666666;
}

.tag:hover {
  background: #e5e5e5;
  color: #333333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-container {
    padding: 1rem;
  }
  
  .main-title {
    font-size: 2.5rem;
  }
  
  .subtitle {
    font-size: 1.1rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .feature-card {
    padding: 1.5rem;
  }
  
  .feature-icon {
    font-size: 2.5rem;
  }
}

@media (max-width: 480px) {
  .home-container {
    padding: 0.5rem;
  }
  
  .main-title {
    font-size: 2rem;
  }
  
  .subtitle {
    font-size: 1rem;
  }
  
  .features-grid {
    gap: 1rem;
  }
  
  .feature-card {
    padding: 1.25rem;
  }
  
  .feature-icon {
    font-size: 2rem;
  }
  
  .feature-card h3 {
    font-size: 1.25rem;
  }
  
  .feature-card p {
    font-size: 0.9rem;
  }
  
  .tag {
    font-size: 0.8rem;
    padding: 0.3rem 0.6rem;
  }
}


</style>
