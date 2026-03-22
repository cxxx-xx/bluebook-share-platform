/**
 * Vue Router 路由配置文件
 * 
 * 技术说明：
 * - 使用Vue Router 4.x版本
 * - 采用HTML5 History模式（createWebHistory）
 * - 支持路由懒加载（可选优化）
 * - 包含路由守卫进行权限控制
 * 
 * 路由结构：
 * - 公开页面：首页、登录、注册、排行榜、帖子详情、用户主页
 * - 需登录页面：通知、私信、收藏、个人中心
 * - 管理员页面：后台管理（需管理员权限）
 */

import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件
import Home from '@/views/Home.vue'           // 首页 - 帖子列表
import Login from '@/views/Login.vue'         // 登录页
import Register from '@/views/Register.vue'   // 注册页
import Profile from '@/views/Profile.vue'     // 个人主页
import Post from '@/views/Post.vue'           // 帖子详情/发布页
import Ranking from '@/views/Ranking.vue'     // 排行榜
import Notifications from '@/views/Notifications.vue' // 通知页
import Messages from '@/views/Messages.vue'   // 私信页
import Collections from '@/views/Collections.vue' // 收藏页
import Admin from '@/views/Admin.vue'         // 管理员后台

/**
 * 路由配置数组
 * 
 * 每个路由对象包含：
 * - path: URL路径
 * - name: 路由名称（用于编程式导航）
 * - component: 对应的Vue组件
 * - params: 动态路由参数（如:id）
 */
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile
  },
  {
    path: '/profile/:id',
    name: 'UserProfile',
    component: Profile
  },
  {
    path: '/post/:id',
    name: 'Post',
    component: Post
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: Ranking
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: Notifications
  },
  {
    path: '/messages',
    name: 'Messages',
    component: Messages
  },
  {
    path: '/messages/:id',
    name: 'Chat',
    component: Messages
  },
  {
    path: '/collections',
    name: 'Collections',
    component: Collections
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin
  }
]

/**
 * 创建路由实例
 * 
 * createWebHistory: 使用HTML5 History模式
 * - URL更美观，无#号
 * - 需要服务器配置支持（nginx配置try_files）
 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局前置路由守卫
 * 
 * 功能：
 * 1. 检查用户登录状态
 * 2. 检查管理员权限
 * 3. 控制页面访问权限
 * 
 * @param {Object} to - 目标路由对象
 * @param {Object} from - 来源路由对象
 * @param {Function} next - 放行函数
 */
router.beforeEach((to, from, next) => {
  // 从localStorage获取登录状态
  const isLoggedIn = !!localStorage.getItem('userId')
  
  // 管理员页面权限检查
  if (to.path === '/admin') {
    const isAdmin = localStorage.getItem('isAdmin') === 'true' || localStorage.getItem('isAdmin') === true
    if (!isLoggedIn || !isAdmin) {
      next('/')
      return
    }
  }
  
  // 需要登录的页面检查
  if (to.path === '/notifications' || to.path === '/messages' || to.path === '/collections') {
    if (!isLoggedIn) {
      next('/login')
      return
    }
  }
  
  next()
})

export default router
