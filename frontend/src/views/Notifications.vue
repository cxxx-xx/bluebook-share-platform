<template>
  <div class="notifications-page">
    <h1>消息中心</h1>

    <div class="notification-tabs">
      <div :class="['tab', { active: activeTab === 'all' }]" @click="activeTab = 'all'">全部</div>
      <div :class="['tab', { active: activeTab === 'LIKE' }]" @click="activeTab = 'LIKE'">点赞</div>
      <div :class="['tab', { active: activeTab === 'COLLECT' }]" @click="activeTab = 'COLLECT'">收藏</div>
      <div :class="['tab', { active: activeTab === 'COMMENT' }]" @click="activeTab = 'COMMENT'">评论</div>
      <div :class="['tab', { active: activeTab === 'FOLLOW' }]" @click="activeTab = 'FOLLOW'">关注</div>
      <div :class="['tab', { active: activeTab === 'MESSAGE' }]" @click="activeTab = 'MESSAGE'">私信</div>
      <div :class="['tab', { active: activeTab === 'SYSTEM' }]" @click="activeTab = 'SYSTEM'">系统</div>
    </div>

    <div class="notifications-list">
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="filteredNotifications.length === 0" class="empty-state">暂无消息</div>
      
      <div v-else>
        <div 
          v-for="notification in filteredNotifications" 
          :key="notification.id"
          :class="['notification-item', { unread: !notification.isRead }]"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-icon">{{ getTypeIcon(notification.type) }}</div>
          <div class="notification-content">
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-text">{{ notification.content }}</div>
            <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
          </div>
          <button class="btn-delete" @click.stop="deleteNotification(notification.id)">删除</button>
        </div>
      </div>
    </div>

    <button v-if="notifications.length > 0" class="btn-mark-all" @click="markAllRead">
      全部标记已读
    </button>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api'

export default {
  name: 'Notifications',
  setup() {
    const router = useRouter()
    const notifications = ref([])
    const loading = ref(true)
    const activeTab = ref('all')

    const filteredNotifications = computed(() => {
      if (activeTab.value === 'all') {
        return notifications.value
      }
      return notifications.value.filter(n => n.type === activeTab.value)
    })

    const typeIcons = {
      LIKE: '❤️',
      COLLECT: '⭐',
      COMMENT: '💬',
      FOLLOW: '👤',
      MESSAGE: '✉️',
      SYSTEM: '📢'
    }

    const getTypeIcon = (type) => typeIcons[type] || '📢'

    const formatTime = (time) => {
      const date = new Date(time)
      const now = new Date()
      const diff = (now - date) / 1000
      
      if (diff < 60) return '刚刚'
      if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
      if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
      return date.toLocaleDateString()
    }

    const loadNotifications = async () => {
      loading.value = true
      try {
        const res = await api.getNotifications()
        notifications.value = res.notifications || []
      } catch (error) {
        console.error('Failed to load notifications:', error)
      } finally {
        loading.value = false
      }
    }

    const handleNotificationClick = async (notification) => {
      if (!notification.isRead) {
        await api.markNotificationRead(notification.id)
        notification.isRead = true
      }
      
      if (notification.type === 'MESSAGE' && notification.relatedUserId) {
        router.push(`/messages/${notification.relatedUserId}`)
      } else if (notification.type === 'FOLLOW' && notification.relatedUserId) {
        router.push(`/profile/${notification.relatedUserId}`)
      } else if (notification.relatedId) {
        router.push(`/post/${notification.relatedId}`)
      }
    }

    const deleteNotification = async (id) => {
      try {
        await api.deleteNotification(id)
        notifications.value = notifications.value.filter(n => n.id !== id)
      } catch (error) {
        console.error('Failed to delete notification:', error)
      }
    }

    const markAllRead = async () => {
      try {
        await api.markAllNotificationsRead()
        notifications.value.forEach(n => n.isRead = true)
      } catch (error) {
        console.error('Failed to mark all read:', error)
      }
    }

    onMounted(() => {
      loadNotifications()
    })

    return {
      notifications,
      loading,
      activeTab,
      filteredNotifications,
      getTypeIcon,
      formatTime,
      handleNotificationClick,
      deleteNotification,
      markAllRead
    }
  }
}
</script>

<style scoped>
.notifications-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.notifications-page h1 {
  color: white;
  text-align: center;
  margin-bottom: 20px;
}

.notification-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.tab {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  cursor: pointer;
}

.tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.notifications-list {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 20px;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.notification-item:hover {
  background: rgba(102, 126, 234, 0.1);
}

.notification-item.unread {
  background: rgba(102, 126, 234, 0.05);
}

.notification-icon {
  font-size: 24px;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 600;
  margin-bottom: 5px;
}

.notification-text {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.notification-time {
  color: #999;
  font-size: 12px;
}

.btn-delete {
  padding: 5px 15px;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  font-size: 12px;
}

.btn-mark-all {
  display: block;
  width: 100%;
  margin-top: 20px;
  padding: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
}

.loading, .empty-state {
  text-align: center;
  padding: 50px;
  color: #666;
}
</style>
