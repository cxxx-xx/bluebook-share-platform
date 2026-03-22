/**
 * 蓝书前端应用入口文件
 * 
 * 技术栈：
 * - Vue.js 3：渐进式JavaScript框架，采用Composition API
 * - Vue Router 4：官方路由管理器，支持SPA单页应用
 * - Axios：HTTP客户端，用于与后端API通信
 * - Vite 5.0：下一代前端构建工具，提供快速的开发体验
 * 
 * 应用架构：
 * - 单页应用(SPA)架构
 * - 前后端分离设计
 * - RESTful API通信
 * - Session会话管理
 * 
 * @author Bluebook Team
 * @version 1.0
 */

// 从Vue 3导入createApp函数，用于创建应用实例
import { createApp } from 'vue'

// 导入根组件
import App from './App.vue'

// 导入路由配置
import router from './router'

// 导入全局样式
import './styles/main.css'

// 创建Vue应用实例
const app = createApp(App)

// 注册路由插件
app.use(router)

// 挂载应用到DOM元素#app
app.mount('#app')
