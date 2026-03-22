<template>
  <div class="messages-page">
    <div class="conversations-list" v-if="!selectedUser">
      <h1>私信</h1>
      
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="conversations.length === 0" class="empty-state">
        暂无会话<br>
        <span class="hint">互关的好友会自动显示在这里</span>
      </div>
      
      <div v-else>
        <div 
          v-for="conv in conversations" 
          :key="conv.id"
          class="conversation-item"
          @click="openConversation(conv)"
        >
          <div class="user-avatar">
            <img v-if="conv.avatar" :src="`/avatars/${conv.avatar}`">
            <span v-else>{{ conv.username?.charAt(0).toUpperCase() }}</span>
          </div>
          <div class="conversation-info">
            <div class="username">{{ conv.username }}</div>
            <div class="last-message" v-if="conv.lastMessage">{{ conv.lastMessage }}</div>
            <div class="last-message empty" v-else>点击开始聊天</div>
          </div>
          <div class="conversation-meta">
            <div class="time" v-if="conv.lastMessageTime">{{ formatTime(conv.lastMessageTime) }}</div>
            <div v-if="conv.isMutualFollow" class="mutual-tag">互关</div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-container" v-else>
      <div class="chat-header">
        <button @click="closeConversation">← 返回</button>
        <span>{{ selectedUser.username }}</span>
        <span v-if="!isMutualFollow" class="message-limit">
          剩余 {{ remainingMessages }} 条消息
        </span>
      </div>

      <div class="messages-list" ref="messagesList">
        <div v-if="messages.length === 0" class="empty-chat">
          开始和 {{ selectedUser.username }} 聊天吧
        </div>
        <div 
          v-for="msg in messages" 
          :key="msg.id"
          :class="['message', { sent: msg.sender.id == currentUserId }]"
        >
          <div class="message-content">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
        </div>
      </div>

      <div class="message-input">
        <input 
          v-model="newMessage" 
          @keyup.enter="sendMessage"
          :placeholder="canSend ? '输入消息...' : '互关后才能继续聊天'"
          :disabled="!canSend"
        >
        <button @click="sendMessage" :disabled="!canSend || !newMessage.trim()">发送</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api'

export default {
  name: 'Messages',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const conversations = ref([])
    const messages = ref([])
    const selectedUser = ref(null)
    const loading = ref(true)
    const newMessage = ref('')
    const isMutualFollow = ref(false)
    const remainingMessages = ref(3)
    const messagesList = ref(null)
    
    const currentUserId = computed(() => localStorage.getItem('userId'))
    
    const canSend = computed(() => {
      return isMutualFollow.value || remainingMessages.value > 0
    })

    const formatTime = (time) => {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }

    const loadConversations = async () => {
      loading.value = true
      try {
        const res = await api.getConversations()
        conversations.value = res.conversations || []
      } catch (error) {
        console.error('Failed to load conversations:', error)
      } finally {
        loading.value = false
      }
    }

    const openConversation = async (conv) => {
      selectedUser.value = conv
      isMutualFollow.value = conv.isMutualFollow
      
      try {
        const res = await api.getConversation(conv.id)
        messages.value = res.messages || []
        
        const canRes = await api.canSendMessage(conv.id)
        remainingMessages.value = 3 - canRes.sentCount
        
        scrollToBottom()
      } catch (error) {
        console.error('Failed to load conversation:', error)
      }
    }

    const openConversationWithUser = async (userId) => {
      const numUserId = parseInt(userId)
      const conv = conversations.value.find(c => c.id === numUserId)
      if (conv) {
        await openConversation(conv)
      } else {
        try {
          const userInfo = await api.getUserInfo(numUserId)
          selectedUser.value = {
            id: userInfo.id,
            username: userInfo.username,
            avatar: userInfo.avatar,
            isMutualFollow: false
          }
          isMutualFollow.value = false
          messages.value = []
          remainingMessages.value = 3
          
          const canRes = await api.canSendMessage(numUserId)
          if (canRes.isMutualFollow !== undefined) {
            isMutualFollow.value = canRes.isMutualFollow
            selectedUser.value.isMutualFollow = canRes.isMutualFollow
          }
          if (canRes.remainingMessages !== undefined) {
            remainingMessages.value = canRes.remainingMessages === -1 ? 999 : canRes.remainingMessages
          }
        } catch (error) {
          console.error('Failed to load user info:', error)
          alert('无法加载用户信息')
        }
      }
    }

    const closeConversation = () => {
      selectedUser.value = null
      messages.value = []
      router.push('/messages')
    }

    const sendMessage = async () => {
      if (!newMessage.value.trim() || !canSend.value) return
      
      try {
        await api.sendMessage(selectedUser.value.id, newMessage.value)
        messages.value.push({
          id: Date.now(),
          content: newMessage.value,
          sender: { id: currentUserId.value },
          createdAt: new Date().toISOString()
        })
        
        if (!isMutualFollow.value) {
          remainingMessages.value--
        }
        
        newMessage.value = ''
        scrollToBottom()
      } catch (error) {
        alert(error.response?.data?.error || '发送失败')
      }
    }

    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesList.value) {
          messagesList.value.scrollTop = messagesList.value.scrollHeight
        }
      })
    }

    onMounted(async () => {
      await loadConversations()
      
      if (route.params.id) {
        await openConversationWithUser(route.params.id)
      }
    })

    watch(() => route.params.id, async (newId) => {
      if (newId) {
        await openConversationWithUser(newId)
      }
    })

    return {
      conversations,
      messages,
      selectedUser,
      loading,
      newMessage,
      isMutualFollow,
      remainingMessages,
      canSend,
      currentUserId,
      messagesList,
      formatTime,
      openConversation,
      openConversationWithUser,
      closeConversation,
      sendMessage
    }
  }
}
</script>

<style scoped>
.messages-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.conversations-list h1 {
  color: white;
  margin-bottom: 20px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  margin-bottom: 10px;
  cursor: pointer;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  overflow: hidden;
}

.conversation-info {
  flex: 1;
}

.username {
  font-weight: 600;
}

.last-message {
  color: #666;
  font-size: 14px;
}

.conversation-meta {
  text-align: right;
}

.time {
  color: #999;
  font-size: 12px;
}

.mutual-tag {
  background: #667eea;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 10px;
}

.chat-container {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  height: 70vh;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.chat-header button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
}

.message-limit {
  color: #ff4757;
  font-size: 12px;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

.message {
  margin-bottom: 15px;
  max-width: 70%;
}

.message.sent {
  margin-left: auto;
  text-align: right;
}

.message-content {
  display: inline-block;
  padding: 10px 15px;
  background: #f0f0f0;
  border-radius: 15px;
}

.message.sent .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-time {
  font-size: 10px;
  color: #999;
  margin-top: 5px;
}

.message-input {
  display: flex;
  gap: 10px;
  padding: 15px;
  border-top: 1px solid #eee;
}

.message-input input {
  flex: 1;
  padding: 10px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 25px;
}

.message-input button {
  padding: 10px 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
}

.message-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading, .empty-state {
  text-align: center;
  padding: 50px;
  color: #666;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
}

.empty-state .hint {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
  display: block;
}

.last-message.empty {
  color: #999;
  font-style: italic;
}

.empty-chat {
  text-align: center;
  padding: 50px;
  color: #999;
}
</style>
