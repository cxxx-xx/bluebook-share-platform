<template>
  <div class="home-page">
    <div class="category-tabs">
      <div 
        v-for="cat in categories" 
        :key="cat.key"
        :class="['category-tab', { active: currentCategory === cat.key }]"
        @click="switchCategory(cat.key)"
      >
        {{ cat.name }}
      </div>
    </div>

    <div class="posts-container">
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="posts.length === 0" class="empty-state">
        暂无帖子
      </div>
      
      <div v-else class="posts-grid">
        <div v-for="post in posts" :key="post.id" class="post-card" @click="viewPost(post.id)">
          <div class="post-header">
            <div class="post-avatar" @click.stop="viewProfile(post.user.id)">
              <img v-if="post.user.avatar" :src="`/avatars/${post.user.avatar}`" @error="handleAvatarError">
              <span v-else>{{ post.user.username.charAt(0).toUpperCase() }}</span>
            </div>
            <div class="post-user-info">
              <div class="post-username">
                {{ post.user.username }}
                <span :class="['post-level', `level-${post.user.level}`]">
                  Lv.{{ post.user.level }} {{ getLevelName(post.user.level) }}
                </span>
              </div>
              <div class="post-meta">{{ formatTime(post.createdAt) }}</div>
            </div>
          </div>
          
          <span class="category-tag">{{ getCategoryName(post.category) }}</span>
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-content">{{ post.content.substring(0, 200) }}{{ post.content.length > 200 ? '...' : '' }}</p>
          
          <img v-if="post.image" :src="post.image" class="post-image" @error="handleImageError">
          
          <div class="post-actions">
            <button class="action-btn" @click.stop="likePost(post.id, $event)">
              ❤️ <span>{{ formatNumber(post.likesCount) }}</span>
            </button>
            <button class="action-btn" @click.stop="collectPost(post.id, $event)">
              ⭐ <span>{{ formatNumber(post.collectsCount) }}</span>
            </button>
            <button class="action-btn">
              💬 <span>{{ formatNumber(post.commentsCount) }}</span>
            </button>
            <button class="action-btn" @click.stop="dislikePost(post.id, $event)">
              👎 <span>{{ formatNumber(post.dislikesCount) }}</span>
            </button>
          </div>
        </div>
      </div>

      <div v-if="hasMore" class="load-more">
        <button @click="loadMore">加载更多</button>
      </div>
    </div>

    <button v-if="isLoggedIn" class="fab" @click="showCreatePost = true">+</button>

    <div v-if="showCreatePost" class="modal" @click.self="showCreatePost = false">
      <div class="modal-content">
        <h2>发布帖子</h2>
        <form @submit.prevent="createPost">
          <div class="form-group">
            <label>标题</label>
            <input v-model="newPost.title" type="text" required>
          </div>
          <div class="form-group">
            <label>分类</label>
            <select v-model="newPost.category" required>
              <option v-for="cat in categories.slice(1)" :key="cat.key" :value="cat.key">
                {{ cat.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>内容</label>
            <textarea v-model="newPost.content" rows="6" required></textarea>
          </div>
          <div class="form-group">
            <label>图片</label>
            <input type="file" accept="image/*" @change="handleImageSelect">
            <img v-if="newPost.imagePreview" :src="newPost.imagePreview" class="image-preview">
          </div>
          <div class="form-actions">
            <button type="button" @click="showCreatePost = false">取消</button>
            <button type="submit" class="btn-primary">发布</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from '@/api'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const posts = ref([])
    const loading = ref(true)
    const currentPage = ref(0)
    const hasMore = ref(true)
    const currentCategory = ref('all')
    const showCreatePost = ref(false)
    const newPost = ref({
      title: '',
      content: '',
      category: '',
      image: null,
      imagePreview: null
    })

    const categories = [
      { key: 'all', name: '全部' },
      { key: 'food', name: '美食' },
      { key: 'travel', name: '旅行' },
      { key: 'digital', name: '数码' },
      { key: 'fashion', name: '穿搭' },
      { key: 'home', name: '家居' },
      { key: 'beauty', name: '美妆' },
      { key: 'fitness', name: '健身' },
      { key: 'other', name: '其他' }
    ]

    const levelNames = {
      1: '布衣无名', 2: '里闾崭露', 3: '一技鸣乡', 4: '名动郡邑',
      5: '誉满江湖', 6: '声闻天下', 7: '名冠当世', 8: '千古流芳', 9: '万世不朽'
    }

    const isLoggedIn = computed(() => !!localStorage.getItem('userId'))

    const getLevelName = (level) => levelNames[level] || '布衣无名'
    
    const getCategoryName = (key) => categories.find(c => c.key === key)?.name || '其他'

    const formatTime = (time) => {
      const date = new Date(time)
      const now = new Date()
      const diff = (now - date) / 1000
      
      if (diff < 60) return '刚刚'
      if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
      if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
      if (diff < 604800) return `${Math.floor(diff / 86400)}天前`
      return date.toLocaleDateString()
    }

    const formatNumber = (num) => {
      if (num >= 100000000) return (num / 100000000).toFixed(1) + '亿'
      if (num >= 10000) return (num / 10000).toFixed(1) + '万'
      return num.toString()
    }

    const loadPosts = async () => {
      loading.value = true
      try {
        let res
        const keyword = route.query.search
        if (keyword) {
          res = await api.searchPosts(keyword, currentPage.value)
        } else if (currentCategory.value === 'all') {
          res = await api.getPosts(currentPage.value)
        } else {
          res = await api.getPostsByCategory(currentCategory.value, currentPage.value)
        }
        
        if (currentPage.value === 0) {
          posts.value = res.posts || []
        } else {
          posts.value.push(...(res.posts || []))
        }
        hasMore.value = res.hasNext
      } catch (error) {
        console.error('Failed to load posts:', error)
      } finally {
        loading.value = false
      }
    }

    const loadMore = () => {
      currentPage.value++
      loadPosts()
    }

    const switchCategory = (category) => {
      currentCategory.value = category
      currentPage.value = 0
      loadPosts()
    }

    const viewPost = (id) => router.push(`/post/${id}`)
    const viewProfile = (id) => router.push(`/profile/${id}`)

    const likePost = async (postId, event) => {
      event.stopPropagation()
      try {
        await api.likePost(postId)
        const post = posts.value.find(p => p.id === postId)
        if (post) {
          post.likesCount++
          post.liked = true
        }
      } catch (error) {
        const errorMsg = error.response?.data?.error || '操作失败'
        if (errorMsg.includes('登录')) {
          alert('请先登录')
          router.push('/login')
        } else {
          alert(errorMsg)
        }
      }
    }

    const collectPost = async (postId, event) => {
      event.stopPropagation()
      try {
        await api.collectPost(postId)
        const post = posts.value.find(p => p.id === postId)
        if (post) {
          post.collectsCount++
          post.collected = true
        }
      } catch (error) {
        const errorMsg = error.response?.data?.error || '操作失败'
        if (errorMsg.includes('登录')) {
          alert('请先登录')
          router.push('/login')
        } else {
          alert(errorMsg)
        }
      }
    }

    const dislikePost = async (postId, event) => {
      event.stopPropagation()
      try {
        await api.dislikePost(postId)
        const post = posts.value.find(p => p.id === postId)
        if (post) {
          post.dislikesCount++
          post.disliked = true
        }
      } catch (error) {
        const errorMsg = error.response?.data?.error || '操作失败'
        if (errorMsg.includes('登录')) {
          alert('请先登录')
          router.push('/login')
        } else {
          alert(errorMsg)
        }
      }
    }

    const handleImageSelect = async (event) => {
      const file = event.target.files[0]
      if (file) {
        newPost.value.image = file
        newPost.value.imagePreview = URL.createObjectURL(file)
      }
    }

    const createPost = async () => {
      try {
        let imageUrl = ''
        if (newPost.value.image) {
          imageUrl = await api.uploadPostImage(newPost.value.image)
        }
        
        await api.createPost({
          title: newPost.value.title,
          content: newPost.value.content,
          category: newPost.value.category,
          image: imageUrl
        })
        
        showCreatePost.value = false
        newPost.value = { title: '', content: '', category: '', image: null, imagePreview: null }
        currentPage.value = 0
        loadPosts()
      } catch (error) {
        alert(error.response?.data?.error || '发布失败')
      }
    }

    const handleAvatarError = (e) => {
      e.target.style.display = 'none'
    }

    const handleImageError = (e) => {
      e.target.style.display = 'none'
    }

    onMounted(() => {
      loadPosts()
    })

    return {
      posts,
      loading,
      hasMore,
      currentCategory,
      categories,
      showCreatePost,
      newPost,
      isLoggedIn,
      getLevelName,
      getCategoryName,
      formatTime,
      formatNumber,
      loadMore,
      switchCategory,
      viewPost,
      viewProfile,
      likePost,
      collectPost,
      dislikePost,
      handleImageSelect,
      createPost,
      handleAvatarError,
      handleImageError
    }
  }
}
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.category-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.category-tab {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-tab:hover {
  background: rgba(102, 126, 234, 0.2);
}

.category-tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.post-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.post-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.post-avatar {
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

.post-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-username {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.post-level {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.level-1 { background: #e8e8e8; color: #666; }
.level-2 { background: #d4c4a8; color: #5a4a2a; }
.level-3 { background: #c8a882; color: #3a2a0a; }
.level-4 { background: linear-gradient(135deg, #b8860b, #daa520); color: white; }
.level-5 { background: linear-gradient(135deg, #8b4513, #cd853f); color: #fff8dc; }
.level-6 { background: linear-gradient(135deg, #4a0e0e, #8b0000); color: #ffd700; }
.level-7 { background: linear-gradient(135deg, #1a1a2e, #16213e); color: #e6d5ac; border: 1px solid #e6d5ac; }
.level-8 { background: linear-gradient(135deg, #2c3e50, #1a252f); color: #ecf0f1; border: 1px solid #7f8c8d; }
.level-9 { background: linear-gradient(135deg, #0f0f0f, #1c1c1c); color: #ffd700; border: 1px solid #ffd700; }

.post-meta {
  font-size: 12px;
  color: #999;
}

.category-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-radius: 12px;
  font-size: 12px;
  margin-bottom: 10px;
}

.post-title {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

.post-content {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 15px;
}

.post-image {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 15px;
}

.post-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: color 0.3s ease;
}

.action-btn:hover {
  color: #667eea;
}

.fab {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 30px;
  border: none;
  cursor: pointer;
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
  transition: transform 0.3s ease;
}

.fab:hover {
  transform: scale(1.1);
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
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.image-preview {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: 10px;
  margin-top: 10px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}

.form-actions button {
  padding: 12px 30px;
  border-radius: 25px;
  border: none;
  cursor: pointer;
  font-weight: 500;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.loading, .empty-state {
  text-align: center;
  padding: 50px;
  color: #666;
}

.load-more {
  text-align: center;
  margin-top: 30px;
}

.load-more button {
  padding: 12px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
}
</style>
