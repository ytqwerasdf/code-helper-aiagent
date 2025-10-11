<template>
  <div class="home-container">
    <!-- 粒子背景 -->
    <div class="particles-background" ref="particlesContainer"></div>
    
    <!-- 霓虹灯装饰 -->
    <div class="neon-decoration">
      <div class="neon-line neon-line-1"></div>
      <div class="neon-line neon-line-2"></div>
      <div class="neon-line neon-line-3"></div>
    </div>
    
    <!-- 页面头部 -->
    <div class="home-header">
      <div class="title-container">
        <div class="terminal-window" @click="startTerminalAnimation" :class="{ 'clickable': !terminalStarted, 'animating': terminalAnimating }">
          <div class="terminal-header">
            <div class="terminal-buttons">
              <span class="btn close"></span>
              <span class="btn minimize"></span>
              <span class="btn maximize"></span>
            </div>
            <div class="terminal-title">AI_SYSTEM_v2.0</div>
          </div>
          <div class="terminal-body">
            <div class="terminal-line" v-if="terminalStarted">
              <span class="prompt">$</span>
              <span class="command" :class="{ 'typing': commandTyping }">{{ displayedCommand }}</span>
              <span class="cursor" v-if="commandTyping">|</span>
            </div>
            <div class="terminal-line" v-for="(line, index) in displayedLines" :key="index" 
                 :class="{ 'visible': line.visible }">
              <span class="output">{{ line.text }}</span>
              <span class="cursor" v-if="line.typing">|</span>
            </div>
            <div class="terminal-line click-hint" v-if="!terminalStarted">
              <span class="hint">Click to start AI system...</span>
            </div>
          </div>
        </div>
        
        <h1 class="main-title">
          <span class="title-text">AI 智能助手平台</span>
          <div class="title-glow"></div>
        </h1>
        <p class="subtitle">专业的AI编程助手和文档助手服务</p>
        <div class="tech-lines">
          <div class="tech-line"></div>
          <div class="tech-line"></div>
          <div class="tech-line"></div>
        </div>
      </div>
    </div>
    
    <!-- 功能卡片区域 -->
    <div class="features-grid">
      <div class="feature-card" @click="navigateTo('/code-helper')" @mouseenter="onCardHover" @mouseleave="onCardLeave">
        <div class="card-background"></div>
        <div class="card-content">
          <div class="feature-icon">
            <div class="icon-container">💻</div>
            <div class="icon-glow"></div>
          </div>
          <h3>编程助手</h3>
          <p>专业的编程问题解答和代码生成服务，帮助您解决各种编程难题</p>
          <div class="feature-tags">
            <span class="tag">代码生成</span>
            <span class="tag">问题解答</span>
            <span class="tag">编程指导</span>
          </div>
        </div>
        <div class="card-border"></div>
      </div>
      
      <div class="feature-card" @click="navigateTo('/manus-agent')" @mouseenter="onCardHover" @mouseleave="onCardLeave">
        <div class="card-background"></div>
        <div class="card-content">
          <div class="feature-icon">
            <div class="icon-container">📝</div>
            <div class="icon-glow"></div>
          </div>
          <h3>文档助手</h3>
          <p>智能文档处理和内容生成，帮助您高效完成各类文档编写任务</p>
          <div class="feature-tags">
            <span class="tag">文档生成</span>
            <span class="tag">内容优化</span>
            <span class="tag">格式整理</span>
          </div>
        </div>
        <div class="card-border"></div>
      </div>
    </div>
    
    <!-- 底部科技装饰 -->
    <div class="bottom-decoration">
      <div class="circuit-line"></div>
      <div class="floating-elements">
        <div class="floating-element" v-for="i in 5" :key="i"></div>
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
  
  data() {
    return {
      particles: [],
      animationId: null,
      terminalStarted: false,
      terminalAnimating: false,
      commandTyping: false,
      displayedCommand: '',
      displayedLines: [],
      terminalLines: [
        { text: '[INFO] AI Assistant Platform Initialized', delay: 1000 },
        { text: '[INFO] System Status: Online', delay: 1500 },
        { text: '[INFO] Neural Networks: Active', delay: 2000 },
        { text: '[INFO] Ready for user commands...', delay: 2500 }
      ],
      fullCommand: './ai-assistant --init --mode=enhanced'
    }
  },
  
  mounted() {
    this.initParticles()
    this.startAnimation()
  },
  
  beforeUnmount() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId)
    }
  },
  
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
    },
    
    /**
     * 初始化粒子系统
     */
    initParticles() {
      const container = this.$refs.particlesContainer
      if (!container) return
      
      // 创建粒子
      for (let i = 0; i < 50; i++) {
        this.particles.push({
          x: Math.random() * window.innerWidth,
          y: Math.random() * window.innerHeight,
          vx: (Math.random() - 0.5) * 0.5,
          vy: (Math.random() - 0.5) * 0.5,
          size: Math.random() * 3 + 1,
          opacity: Math.random() * 0.5 + 0.2
        })
      }
    },
    
    /**
     * 开始动画循环
     */
    startAnimation() {
      const animate = () => {
        this.updateParticles()
        this.renderParticles()
        this.animationId = requestAnimationFrame(animate)
      }
      animate()
    },
    
    /**
     * 更新粒子位置
     */
    updateParticles() {
      this.particles.forEach(particle => {
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
    
    /**
     * 渲染粒子
     */
    renderParticles() {
      const container = this.$refs.particlesContainer
      if (!container) return
      
      container.innerHTML = ''
      
      this.particles.forEach(particle => {
        const element = document.createElement('div')
        element.className = 'particle'
        element.style.cssText = `
          position: absolute;
          left: ${particle.x}px;
          top: ${particle.y}px;
          width: ${particle.size}px;
          height: ${particle.size}px;
          background: radial-gradient(circle, rgba(255,255,255,${particle.opacity}) 0%, transparent 70%);
          border-radius: 50%;
          pointer-events: none;
        `
        container.appendChild(element)
      })
    },
    
    /**
     * 卡片悬停效果
     */
    onCardHover(event) {
      const card = event.currentTarget
      card.style.transform = 'translateY(-10px) scale(1.02)'
    },
    
    /**
     * 卡片离开效果
     */
    onCardLeave(event) {
      const card = event.currentTarget
      card.style.transform = 'translateY(0) scale(1)'
    },
    
    /**
     * 开始终端动画
     */
    async startTerminalAnimation() {
      if (this.terminalStarted || this.terminalAnimating) return
      
      this.terminalAnimating = true
      this.terminalStarted = true
      
      // 打字机效果输入命令
      await this.typeCommand()
      
      // 等待一下再显示输出
      await this.delay(500)
      
      // 逐行显示输出，每行使用打字机效果
      for (let i = 0; i < this.terminalLines.length; i++) {
        await this.delay(this.terminalLines[i].delay)
        
        // 添加新行，初始为空
        this.displayedLines.push({
          text: '',
          visible: true,
          typing: true
        })
        
        // 打字机效果显示这一行
        await this.typeLine(this.terminalLines[i].text, i)
        
        // 标记这一行打字完成
        this.displayedLines[i].typing = false
      }
      
      this.terminalAnimating = false
    },
    
    /**
     * 打字机效果输入命令
     */
    async typeCommand() {
      this.commandTyping = true
      this.displayedCommand = ''
      
      for (let i = 0; i < this.fullCommand.length; i++) {
        this.displayedCommand += this.fullCommand[i]
        // 随机化打字速度，模拟真实打字
        const delay = Math.random() * 30 + 20 // 20-50ms随机延迟
        await this.delay(delay)
      }
      
      this.commandTyping = false
    },
    
    /**
     * 打字机效果显示单行文本
     */
    async typeLine(text, lineIndex) {
      const line = this.displayedLines[lineIndex]
      if (!line) return
      
      for (let i = 0; i < text.length; i++) {
        line.text += text[i]
        // 随机化打字速度
        const delay = Math.random() * 40 + 30 // 30-70ms随机延迟
        await this.delay(delay)
      }
    },
    
    /**
     * 延迟函数
     */
    delay(ms) {
      return new Promise(resolve => setTimeout(resolve, ms))
    }
  }
}
</script>

<style scoped>
/* 主容器 */
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 50%, #0d1117 100%);
  padding: 2rem;
  color: #e6e6e6;
  position: relative;
  overflow: hidden;
}

/* 粒子背景 */
.particles-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

/* 霓虹灯装饰 */
.neon-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 2;
}

.neon-line {
  position: absolute;
  background: linear-gradient(90deg, transparent, #00ffff, transparent);
  height: 2px;
  animation: neonFlow 3s ease-in-out infinite;
}

.neon-line-1 {
  top: 20%;
  left: -100%;
  width: 100%;
  animation-delay: 0s;
}

.neon-line-2 {
  top: 60%;
  right: -100%;
  width: 100%;
  animation-delay: 1s;
  animation-direction: reverse;
}

.neon-line-3 {
  top: 80%;
  left: -100%;
  width: 100%;
  animation-delay: 2s;
}

@keyframes neonFlow {
  0%, 100% { transform: translateX(-100%); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translateX(100%); }
}

/* 页面头部 */
.home-header {
  text-align: center;
  margin-bottom: 3rem;
  position: relative;
  z-index: 3;
}

/* 终端窗口 */
.terminal-window {
  background: rgba(0, 0, 0, 0.9);
  border: 1px solid rgba(0, 255, 0, 0.3);
  border-radius: 8px;
  margin-bottom: 2rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  transition: all 0.3s ease;
}

.terminal-window.clickable {
  cursor: pointer;
  border-color: rgba(0, 255, 0, 0.6);
  box-shadow: 0 8px 32px rgba(0, 255, 0, 0.2);
}

.terminal-window.clickable:hover {
  border-color: rgba(0, 255, 0, 0.8);
  box-shadow: 0 12px 40px rgba(0, 255, 0, 0.3);
  transform: translateY(-2px);
}

.terminal-window.animating {
  border-color: rgba(0, 255, 0, 0.8);
  box-shadow: 0 8px 32px rgba(0, 255, 0, 0.4);
}

.terminal-header {
  background: rgba(0, 20, 40, 0.8);
  padding: 0.75rem 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0, 255, 0, 0.2);
}

.terminal-buttons {
  display: flex;
  gap: 0.5rem;
}

.terminal-buttons .btn {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.terminal-buttons .close {
  background: #ff5f56;
}

.terminal-buttons .minimize {
  background: #ffbd2e;
}

.terminal-buttons .maximize {
  background: #27ca3f;
}

.terminal-title {
  color: #00ff00;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  font-weight: 600;
}

.terminal-body {
  padding: 1rem;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.6;
}

.terminal-line {
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.terminal-line.visible {
  opacity: 1;
  transform: translateX(0);
}

.prompt {
  color: #00ff00;
  font-weight: bold;
}

.command {
  color: #0099ff;
  position: relative;
}

.command.typing {
  animation: typing 0.5s ease-in-out infinite;
}

.cursor {
  color: #00ff00;
  animation: blink 1s infinite;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

@keyframes typing {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0.7; }
}

.output {
  color: #e6e6e6;
  margin-left: 1rem;
}

.click-hint {
  opacity: 1 !important;
  transform: translateX(0) !important;
}

.hint {
  color: rgba(0, 255, 0, 0.6);
  font-style: italic;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.title-container {
  position: relative;
}

.main-title {
  position: relative;
  font-size: 3.5rem;
  margin-bottom: 1rem;
  font-weight: 700;
  text-shadow: 0 0 20px rgba(0, 255, 255, 0.5);
  animation: titleGlow 2s ease-in-out infinite alternate;
}

.title-text {
  position: relative;
  z-index: 2;
  background: linear-gradient(45deg, #00ff00, #0099ff);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradientShift 3s ease-in-out infinite;
  font-family: 'Courier New', monospace;
  font-weight: 700;
}

.title-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, #00ffff, #ff00ff);
  filter: blur(20px);
  opacity: 0.3;
  z-index: 1;
}

@keyframes titleGlow {
  0% { text-shadow: 0 0 20px rgba(0, 255, 255, 0.5); }
  100% { text-shadow: 0 0 30px rgba(255, 0, 255, 0.8); }
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.subtitle {
  font-size: 1.3rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto 2rem;
  position: relative;
}

.tech-lines {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

.tech-line {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00ffff, transparent);
  animation: techPulse 2s ease-in-out infinite;
}

.tech-line:nth-child(2) {
  animation-delay: 0.5s;
}

.tech-line:nth-child(3) {
  animation-delay: 1s;
}

@keyframes techPulse {
  0%, 100% { opacity: 0.3; transform: scaleX(0.5); }
  50% { opacity: 1; transform: scaleX(1); }
}

/* 功能卡片区域 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 3;
}

.feature-card {
  position: relative;
  background: rgba(0, 20, 40, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 255, 0, 0.3);
  border-radius: 8px;
  padding: 2.5rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 8px 32px rgba(0,0,0,0.5);
  overflow: hidden;
  font-family: 'Courier New', monospace;
}

.card-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 255, 255, 0.1), rgba(255, 0, 255, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 25px;
}

.card-content {
  position: relative;
  z-index: 2;
}

.card-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 25px;
  padding: 2px;
  background: linear-gradient(135deg, #00ffff, #ff00ff, #ffff00);
  opacity: 0;
  transition: opacity 0.3s ease;
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: subtract;
}

.feature-card:hover {
  transform: translateY(-15px) scale(1.03);
  box-shadow: 0 20px 60px rgba(0, 255, 0, 0.3);
  border-color: rgba(0, 255, 0, 0.6);
}

.feature-card:hover .card-background {
  opacity: 1;
}

.feature-card:hover .card-border {
  opacity: 1;
}

.feature-icon {
  position: relative;
  font-size: 4.5rem;
  margin-bottom: 1.5rem;
  display: inline-block;
}

.icon-container {
  position: relative;
  z-index: 2;
  animation: iconFloat 3s ease-in-out infinite;
}

.icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(0, 255, 255, 0.3), transparent);
  border-radius: 50%;
  filter: blur(15px);
  animation: iconGlow 2s ease-in-out infinite alternate;
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

@keyframes iconGlow {
  0% { opacity: 0.3; transform: translate(-50%, -50%) scale(0.8); }
  100% { opacity: 0.8; transform: translate(-50%, -50%) scale(1.2); }
}

.feature-card h3 {
  font-size: 1.8rem;
  margin-bottom: 1rem;
  font-weight: 600;
  background: linear-gradient(45deg, #00ff00, #0099ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-family: 'Courier New', monospace;
}

.feature-card p {
  margin-bottom: 1.5rem;
  opacity: 0.9;
  line-height: 1.6;
  font-size: 1.1rem;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.tag {
  background: rgba(0, 255, 0, 0.1);
  padding: 0.4rem 1rem;
  border-radius: 4px;
  font-size: 0.9rem;
  border: 1px solid rgba(0, 255, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  font-family: 'Courier New', monospace;
  color: #00ff00;
}

.tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.tag:hover::before {
  left: 100%;
}

.tag:hover {
  background: rgba(0, 255, 0, 0.2);
  transform: translateY(-2px);
  border-color: rgba(0, 255, 0, 0.6);
}

/* 底部装饰 */
.bottom-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100px;
  pointer-events: none;
  z-index: 2;
}

.circuit-line {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 200px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00ffff, transparent);
  animation: circuitFlow 4s ease-in-out infinite;
}

@keyframes circuitFlow {
  0%, 100% { opacity: 0; transform: translateX(-50%) scaleX(0); }
  50% { opacity: 1; transform: translateX(-50%) scaleX(1); }
}

.floating-elements {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100px;
}

.floating-element {
  position: absolute;
  width: 4px;
  height: 4px;
  background: #00ffff;
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

.floating-element:nth-child(1) { left: 10%; animation-delay: 0s; }
.floating-element:nth-child(2) { left: 30%; animation-delay: 1s; }
.floating-element:nth-child(3) { left: 50%; animation-delay: 2s; }
.floating-element:nth-child(4) { left: 70%; animation-delay: 3s; }
.floating-element:nth-child(5) { left: 90%; animation-delay: 4s; }

@keyframes float {
  0%, 100% { transform: translateY(0px) scale(1); opacity: 0.3; }
  50% { transform: translateY(-20px) scale(1.2); opacity: 1; }
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
    padding: 2rem;
  }
  
  .feature-icon {
    font-size: 3.5rem;
  }
  
  .tech-lines {
    gap: 0.5rem;
  }
  
  .tech-line {
    width: 40px;
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
    padding: 1.5rem;
  }
  
  .feature-icon {
    font-size: 3rem;
  }
  
  .feature-card h3 {
    font-size: 1.5rem;
  }
  
  .feature-card p {
    font-size: 1rem;
  }
  
  .tag {
    font-size: 0.8rem;
    padding: 0.3rem 0.8rem;
  }
  
  .terminal-window {
    margin: 0 0.5rem 1.5rem;
  }
  
  .terminal-body {
    padding: 0.75rem;
    font-size: 0.8rem;
  }
  
  .terminal-title {
    font-size: 0.8rem;
  }
}
</style>
