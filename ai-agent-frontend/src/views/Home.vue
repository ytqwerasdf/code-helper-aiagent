<template>
  <div class="home-container">
    <!-- 页面头部 -->
    <div class="home-header">
      <h1>🤖 AI 智能助手平台</h1>
      <p>欢迎使用AI智能助手，选择您需要的服务开始体验</p>
    </div>
    
    <!-- 功能卡片区域 -->
    <div class="features-grid">
      <div class="feature-card" @click="navigateTo('/code-helper')">
        <div class="feature-icon">💻</div>
        <h3>编程助手</h3>
        <p>专业的编程问题解答和代码生成服务，帮助您解决各种编程难题</p>
        <div class="feature-tags">
          <span class="tag">代码生成</span>
          <span class="tag">问题解答</span>
          <span class="tag">编程指导</span>
        </div>
      </div>
      
      <div class="feature-card" @click="navigateTo('/manus-agent')">
        <div class="feature-icon">📝</div>
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
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
  color: white;
}

.home-header {
  text-align: center;
  margin-bottom: 3rem;
}

.home-header h1 {
  font-size: 3rem;
  margin-bottom: 1rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.home-header p {
  font-size: 1.2rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.feature-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
}

.feature-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 12px 40px rgba(0,0,0,0.2);
}

.feature-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  font-weight: 600;
}

.feature-card p {
  margin-bottom: 1.5rem;
  opacity: 0.9;
  line-height: 1.6;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.tag {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.3rem 0.8rem;
  border-radius: 15px;
  font-size: 0.85rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
}


/* 响应式设计 */
@media (max-width: 768px) {
  .home-container {
    padding: 1rem;
  }
  
  .home-header h1 {
    font-size: 2rem;
  }
  
  .home-header p {
    font-size: 1rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .feature-card {
    padding: 1.5rem;
  }
}
</style>
