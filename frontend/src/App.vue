<template>
  <div id="app">
    <nav class="navbar" v-if="!isAuthPage">
      <div class="nav-brand" @click="$router.push('/')">
        <span class="brand-icon">📖</span>
        <span class="brand-text">蓝书</span>
      </div>
      <div class="nav-search">
        <input type="text" v-model="searchKeyword" placeholder="搜索帖子..." @keyup.enter="searchPosts">
        <button @click="searchPosts">搜索</button>
      </div>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/ranking" class="nav-link">排行榜</router-link>
        <template v-if="isLoggedIn">
          <router-link to="/notifications" class="nav-link">
            通知
            <span v-if="notificationUnread > 0" class="badge">{{ notificationUnread }}</span>
          </router-link>
          <router-link to="/messages" class="nav-link">
            私信
            <span v-if="messageUnread > 0" class="badge">{{ messageUnread }}</span>
          </router-link>
          <router-link to="/profile" class="nav-link">我的</router-link>
          <router-link v-if="isAdmin" to="/admin" class="nav-link">管理</router-link>
          <button @click="logout" class="nav-link btn-logout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link nav-link-primary">注册</router-link>
        </template>
      </div>
    </nav>
    <router-view />
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from './api'

export default {
  name: 'App',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const searchKeyword = ref('')
    const notificationUnread = ref(0)
    const messageUnread = ref(0)
    const isLoggedIn = ref(false)
    const isAdmin = ref(false)
    
    const isAuthPage = computed(() => {
      return ['/login', '/register'].includes(route.path)
    })
    
    const checkLoginStatus = async () => {
      try {
        const res = await api.checkLogin()
        if (res.id) {
          isLoggedIn.value = true
          isAdmin.value = res.isAdmin === 'true' || res.isAdmin === true
          localStorage.setItem('userId', res.id)
          localStorage.setItem('username', res.username)
          localStorage.setItem('isAdmin', res.isAdmin)
          fetchUnreadCount()
        } else {
          isLoggedIn.value = false
          isAdmin.value = false
          localStorage.removeItem('userId')
          localStorage.removeItem('username')
          localStorage.removeItem('isAdmin')
        }
      } catch (error) {
        isLoggedIn.value = false
        isAdmin.value = false
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('isAdmin')
      }
    }
    
    const searchPosts = () => {
      if (searchKeyword.value.trim()) {
        router.push({ path: '/', query: { search: searchKeyword.value } })
      }
    }
    
    const logout = async () => {
      try {
        await api.logout()
      } catch (error) {
        console.error('Logout failed:', error)
      }
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('isAdmin')
      isLoggedIn.value = false
      isAdmin.value = false
      router.push('/login')
    }
    
    const fetchUnreadCount = async () => {
      if (isLoggedIn.value) {
        try {
          const [notifRes, msgRes] = await Promise.all([
            api.getNotificationUnreadCount(),
            api.getUnreadCount()
          ])
          notificationUnread.value = notifRes.count || 0
          messageUnread.value = msgRes.count || 0
        } catch (error) {
          console.error('Failed to fetch unread count:', error)
        }
      }
    }
    
    onMounted(() => {
      checkLoginStatus()
    })
    
    return {
      searchKeyword,
      isLoggedIn,
      isAuthPage,
      notificationUnread,
      messageUnread,
      isAdmin,
      searchPosts,
      logout
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

#app {
  min-height: 100vh;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.brand-icon {
  font-size: 28px;
}

.brand-text {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-search {
  display: flex;
  gap: 10px;
}

.nav-search input {
  padding: 10px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 25px;
  width: 300px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.nav-search input:focus {
  outline: none;
  border-color: #667eea;
}

.nav-search button {
  padding: 10px 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  transition: transform 0.3s ease;
}

.nav-search button:hover {
  transform: scale(1.05);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-link {
  text-decoration: none;
  color: #333;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.nav-link.router-link-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.nav-link-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white !important;
}

.btn-logout {
  background: none;
  border: none;
  cursor: pointer;
  font-size: inherit;
}

.badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ff4757;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}
</style>
