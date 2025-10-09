# AI Agent 前端应用

基于Vue3的AI智能体前端应用，提供AI编程助手和AI超级智能体两个主要功能。

## 功能特性

- 🏠 **主页导航**: 提供应用切换界面，支持不同AI服务的快速访问
- 🤖 **AI编程助手**: 专业的编程助手，支持实时对话和代码分析
- 🧠 **AI超级智能体**: 强大的AI智能体，具备多种工具和功能
- 💬 **实时聊天**: 基于SSE（Server-Sent Events）的实时消息传输
- 📱 **响应式设计**: 支持桌面和移动设备的良好体验

## 技术栈

- **Vue 3**: 现代化的前端框架
- **Vue Router**: 单页面应用路由管理
- **Pinia**: 状态管理
- **Axios**: HTTP请求库
- **Vite**: 快速构建工具
- **SSE**: 服务器推送事件，实现实时通信

## 项目结构

```
src/
├── components/          # 组件目录
├── views/              # 页面组件
│   ├── Home.vue        # 主页
│   ├── CodeHelper.vue  # AI编程助手
│   └── ManusAgent.vue  # AI超级智能体
├── services/           # 服务层
│   └── api.js         # API服务
├── router/            # 路由配置
│   └── index.js       # 路由定义
├── App.vue            # 根组件
├── main.js            # 入口文件
└── style.css          # 全局样式
```

## 安装和运行

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

项目将在 http://localhost:3000 启动

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## API接口

### 后端接口地址

- 基础URL: `http://localhost:8123/api`

### 主要接口

1. **AI编程助手SSE接口**
   - 路径: `/ai/code_helper/chat/sse`
   - 方法: GET
   - 参数: `message` (用户消息), `chatId` (聊天室ID)
   - 返回: Server-Sent Events流

2. **AI超级智能体SSE接口**
   - 路径: `/ai/manus/chat`
   - 方法: GET
   - 参数: `message` (用户消息)
   - 返回: Server-Sent Events流

## 使用说明

1. **访问主页**: 打开应用后，您将看到两个AI服务的卡片
2. **AI编程助手**: 点击进入编程助手页面，可以询问编程相关问题
3. **AI超级智能体**: 点击进入智能体页面，可以处理各种复杂任务
4. **实时对话**: 在聊天界面输入消息，AI将实时响应
5. **会话管理**: 编程助手支持聊天室ID区分不同会话

## 开发说明

### 添加新功能

1. 在 `src/views/` 目录下创建新的页面组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在 `src/services/api.js` 中添加相应的API方法
4. 更新主页导航链接

### 样式定制

- 全局样式定义在 `src/style.css`
- 组件样式使用scoped CSS
- 支持响应式设计，适配不同屏幕尺寸

## 注意事项

- 确保后端服务在 `http://localhost:8123` 运行
- SSE连接需要后端支持CORS配置
- 建议在现代浏览器中使用以获得最佳体验

## 许可证

MIT License


