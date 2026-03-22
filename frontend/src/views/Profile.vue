<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="profile-avatar" @click="showAvatarUpload = true">
        <img v-if="userInfo.avatar" :src="`/avatars/${userInfo.avatar}`" @error="handleAvatarError">
        <span v-else>{{ userInfo.username?.charAt(0).toUpperCase() }}</span>
        <div class="avatar-overlay">更换头像</div>
      </div>
      <div class="profile-info">
        <h1>{{ userInfo.username }}</h1>
        <div class="profile-level" :class="`level-${userInfo.level}`">
          Lv.{{ userInfo.level }} {{ getLevelName(userInfo.level) }}
        </div>
        <div class="profile-fame">知名度: {{ formatNumber(userInfo.fame) }}</div>
      </div>
    </div>

    <div class="profile-stats">
      <div class="stat-item" @click="showFollowers">
        <div class="stat-value">{{ formatNumber(userInfo.fansCount) }}</div>
        <div class="stat-label">粉丝</div>
      </div>
      <div class="stat-item" @click="showFollowing">
        <div class="stat-value">{{ formatNumber(userInfo.followCount) }}</div>
        <div class="stat-label">关注</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ formatNumber(userInfo.postCount) }}</div>
        <div class="stat-label">分享</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ formatNumber(userInfo.likesReceived) }}</div>
        <div class="stat-label">获赞</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ formatNumber(userInfo.collectsReceived) }}</div>
        <div class="stat-label">被收藏</div>
      </div>
    </div>

    <div class="profile-actions" v-if="!isOwnProfile">
      <button 
        :class="['btn-follow', { following: isFollowing }]" 
        @click="toggleFollow"
      >
        {{ isFollowing ? '已关注' : '关注' }}
      </button>
      <button class="btn-message" @click="sendMessage">私信</button>
    </div>

    <div class="profile-tabs">
      <div :class="['tab', { active: activeTab === 'posts' }]" @click="handleTabChange('posts')">帖子</div>
      <div v-if="isOwnProfile" :class="['tab', { active: activeTab === 'liked' }]" @click="handleTabChange('liked')">喜欢</div>
      <div v-if="isOwnProfile" :class="['tab', { active: activeTab === 'collections' }]" @click="handleTabChange('collections')">收藏</div>
    </div>

    <div class="posts-grid">
      <div v-for="post in displayPosts" :key="post.id" class="post-card" @click="viewPost(post.id)">
        <h3>{{ post.title }}</h3>
        <p>{{ post.content?.substring(0, 100) }}...</p>
        <div class="post-stats">
          <span>❤️ {{ post.likesCount }}</span>
          <span>⭐ {{ post.collectsCount }}</span>
          <span>💬 {{ post.commentsCount }}</span>
        </div>
      </div>
    </div>

    <div v-if="showAvatarUpload" class="modal" @click.self="showAvatarUpload = false">
      <div class="modal-content">
        <h2>更换头像</h2>
        <input type="file" accept="image/*" @change="handleAvatarSelect">
        <div class="form-actions">
          <button @click="showAvatarUpload = false">取消</button>
          <button class="btn-primary" @click="uploadAvatar">上传</button>
        </div>
      </div>
    </div>

    <div v-if="showFollowList" class="modal" @click.self="showFollowList = false">
      <div class="modal-content follow-list-modal">
        <h2>{{ followListType === 'followers' ? '粉丝列表' : '关注列表' }}</h2>
        <div class="follow-list">
          <div v-if="followLoading" class="loading">加载中...</div>
          <div v-else-if="followList.length === 0" class="empty">暂无{{ followListType === 'followers' ? '粉丝' : '关注' }}</div>
          <div v-else v-for="user in followList" :key="user.id" class="follow-item" @click="viewUserProfile(user.id)">
            <div class="follow-avatar">
              <img v-if="user.avatar" :src="`/avatars/${user.avatar}`">
              <span v-else>{{ user.username?.charAt(0).toUpperCase() }}</span>
            </div>
            <div class="follow-info">
              <div class="follow-username">{{ user.username }}</div>
              <div class="follow-level">Lv.{{ user.level }}</div>
            </div>
          </div>
        </div>
        <button class="btn-close" @click="showFollowList = false">关闭</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from '@/api'

export default {
  name: 'Profile',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const userInfo = ref({})
    const posts = ref([])
    const collections = ref([])
    const likedPosts = ref([])
    const isFollowing = ref(false)
    const activeTab = ref('posts')
    const showAvatarUpload = ref(false)
    const selectedAvatar = ref(null)
    const showFollowList = ref(false)
    const followListType = ref('')
    const followList = ref([])
    const followLoading = ref(false)

    const isOwnProfile = computed(() => {
      const currentUserId = localStorage.getItem('userId')
      return route.params.id === currentUserId || (!route.params.id && currentUserId)
    })

    const displayPosts = computed(() => {
      if (activeTab.value === 'posts') return posts.value
      if (activeTab.value === 'liked') return likedPosts.value
      return collections.value
    })

    watch(() => route.params.id, () => {
      loadUserInfo()
      loadPosts()
      activeTab.value = 'posts'
    })

    const levelNames = {
      1: '布衣无名', 2: '里闾崭露', 3: '一技鸣乡', 4: '名动郡邑',
      5: '誉满江湖', 6: '声闻天下', 7: '名冠当世', 8: '千古流芳', 9: '万世不朽'
    }

    const getLevelName = (level) => levelNames[level] || '布衣无名'

    const formatNumber = (num) => {
      if (num >= 100000000) return (num / 100000000).toFixed(1) + '亿'
      if (num >= 10000) return (num / 10000).toFixed(1) + '万'
      return num?.toString() || '0'
    }

    const loadUserInfo = async () => {
      const userId = route.params.id || localStorage.getItem('userId')
      if (!userId) {
        router.push('/login')
        return
      }
      
      try {
        userInfo.value = await api.getUserInfo(userId)
        
        if (!isOwnProfile.value) {
          const status = await api.getFollowStatus(userId)
          isFollowing.value = status.isFollowing
        }
      } catch (error) {
        console.error('Failed to load user info:', error)
      }
    }

    const loadPosts = async () => {
      const userId = route.params.id || localStorage.getItem('userId')
      try {
        posts.value = await api.getUserPosts(userId)
      } catch (error) {
        console.error('Failed to load posts:', error)
      }
    }

    const loadCollections = async () => {
      if (!isOwnProfile.value) return
      try {
        const res = await api.getCollections()
        collections.value = res.collections || []
      } catch (error) {
        console.error('Failed to load collections:', error)
      }
    }

    const loadLikedPosts = async () => {
      if (!isOwnProfile.value) return
      try {
        const userId = localStorage.getItem('userId')
        likedPosts.value = await api.getUserLikedPosts(userId)
      } catch (error) {
        console.error('Failed to load liked posts:', error)
      }
    }

    const handleTabChange = (tab) => {
      activeTab.value = tab
      if (tab === 'collections' && collections.value.length === 0) {
        loadCollections()
      }
      if (tab === 'liked' && likedPosts.value.length === 0) {
        loadLikedPosts()
      }
    }

    const toggleFollow = async () => {
      try {
        if (isFollowing.value) {
          await api.unfollow(userInfo.value.id)
          isFollowing.value = false
          userInfo.value.fansCount--
        } else {
          await api.follow(userInfo.value.id)
          isFollowing.value = true
          userInfo.value.fansCount++
        }
      } catch (error) {
        alert(error.response?.data?.error || '操作失败')
      }
    }

    const sendMessage = () => {
      router.push(`/messages/${userInfo.value.id}`)
    }

    const handleAvatarSelect = (event) => {
      selectedAvatar.value = event.target.files[0]
    }

    const uploadAvatar = async () => {
      if (!selectedAvatar.value) {
        alert('请选择图片')
        return
      }
      
      try {
        await api.uploadAvatar(localStorage.getItem('userId'), selectedAvatar.value)
        showAvatarUpload.value = false
        loadUserInfo()
      } catch (error) {
        alert('上传失败')
      }
    }

    const viewPost = (id) => router.push(`/post/${id}`)

    const handleAvatarError = (e) => {
      e.target.style.display = 'none'
    }

    const showFollowers = async () => {
      followListType.value = 'followers'
      showFollowList.value = true
      followLoading.value = true
      try {
        const userId = route.params.id || localStorage.getItem('userId')
        followList.value = await api.getFollowers(userId)
      } catch (error) {
        console.error('Failed to load followers:', error)
      } finally {
        followLoading.value = false
      }
    }

    const showFollowing = async () => {
      followListType.value = 'following'
      showFollowList.value = true
      followLoading.value = true
      try {
        const userId = route.params.id || localStorage.getItem('userId')
        followList.value = await api.getFollowing(userId)
      } catch (error) {
        console.error('Failed to load following:', error)
      } finally {
        followLoading.value = false
      }
    }

    const viewUserProfile = (userId) => {
      showFollowList.value = false
      router.push(`/profile/${userId}`)
    }

    onMounted(() => {
      loadUserInfo()
      loadPosts()
    })

    return {
      userInfo,
      posts,
      collections,
      likedPosts,
      displayPosts,
      isFollowing,
      isOwnProfile,
      activeTab,
      showAvatarUpload,
      showFollowList,
      followListType,
      followList,
      followLoading,
      getLevelName,
      formatNumber,
      toggleFollow,
      sendMessage,
      handleAvatarSelect,
      uploadAvatar,
      viewPost,
      handleAvatarError,
      handleTabChange,
      showFollowers,
      showFollowing,
      viewUserProfile
    }
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
  background: rgba(255, 255, 255, 0.95);
  padding: 30px;
  border-radius: 20px;
  margin-bottom: 20px;
}

.profile-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 48px;
  font-weight: bold;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  text-align: center;
  padding: 5px;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.profile-avatar:hover .avatar-overlay {
  opacity: 1;
}

.profile-info h1 {
  font-size: 28px;
  margin-bottom: 10px;
}

.profile-level {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 14px;
  margin-bottom: 10px;
}

.level-1 { background: #e8e8e8; color: #666; }
.level-4 { background: linear-gradient(135deg, #b8860b, #daa520); color: white; }
.level-5 { background: linear-gradient(135deg, #8b4513, #cd853f); color: #fff8dc; }
.level-6 { background: linear-gradient(135deg, #4a0e0e, #8b0000); color: #ffd700; }
.level-9 { background: linear-gradient(135deg, #0f0f0f, #1c1c1c); color: #ffd700; border: 1px solid #ffd700; }

.profile-fame {
  color: #666;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-radius: 15px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  cursor: pointer;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.profile-actions {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.btn-follow, .btn-message {
  flex: 1;
  padding: 12px;
  border-radius: 25px;
  border: none;
  cursor: pointer;
  font-weight: 500;
}

.btn-follow {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-follow.following {
  background: #e0e0e0;
  color: #666;
}

.btn-message {
  background: white;
  border: 2px solid #667eea;
  color: #667eea;
}

.profile-tabs {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
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

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.post-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-radius: 15px;
  cursor: pointer;
}

.post-card h3 {
  margin-bottom: 10px;
}

.post-card p {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.post-stats {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 12px;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 20px;
  width: 90%;
  max-width: 400px;
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 20px;
  justify-content: flex-end;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 20px;
  cursor: pointer;
}

.follow-list-modal {
  max-width: 500px;
  max-height: 70vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.follow-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
  margin: 15px 0;
}

.follow-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.follow-item:hover {
  background: rgba(102, 126, 234, 0.1);
}

.follow-avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  overflow: hidden;
}

.follow-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.follow-info {
  flex: 1;
}

.follow-username {
  font-weight: 600;
  margin-bottom: 3px;
}

.follow-level {
  font-size: 12px;
  color: #666;
}

.btn-close {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}

.loading, .empty {
  text-align: center;
  padding: 30px;
  color: #666;
}
</style>
