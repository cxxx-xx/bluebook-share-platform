<template>
  <div class="post-page">
    <div class="post-container" v-if="post">
      <div class="post-header">
        <div class="post-avatar" @click="viewProfile(post.user.id)">
          <img v-if="post.user.avatar" :src="`/avatars/${post.user.avatar}`">
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
      <h1 class="post-title">{{ post.title }}</h1>
      <div class="post-content">{{ post.content }}</div>
      
      <img v-if="post.image" :src="post.image" class="post-image">

      <div class="post-actions">
        <button :class="['action-btn', { active: post.isLiked }]" @click="toggleLike">
          ❤️ <span>{{ formatNumber(post.likesCount) }}</span>
        </button>
        <button :class="['action-btn', { active: post.isCollected }]" @click="toggleCollect">
          ⭐ <span>{{ formatNumber(post.collectsCount) }}</span>
        </button>
        <button :class="['action-btn', { active: post.isDisliked }]" @click="toggleDislike">
          👎 <span>{{ formatNumber(post.dislikesCount) }}</span>
        </button>
      </div>

      <div class="comments-section">
        <h3>评论 ({{ post.commentsCount }})</h3>
        
        <div class="comment-form">
          <textarea v-model="newComment" placeholder="写下你的评论..."></textarea>
          <button @click="addComment">发表评论</button>
        </div>

        <div class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">
              <img v-if="comment.user.avatar" :src="`/avatars/${comment.user.avatar}`">
              <span v-else>{{ comment.user.username.charAt(0).toUpperCase() }}</span>
            </div>
            <div class="comment-content">
              <div class="comment-username">{{ comment.user.username }}</div>
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-meta">
                <span>{{ formatTime(comment.createdAt) }}</span>
                <button v-if="comment.user.id === currentUserId" @click="deleteComment(comment.id)">删除</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api'

export default {
  name: 'Post',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const post = ref(null)
    const comments = ref([])
    const newComment = ref('')

    const currentUserId = computed(() => localStorage.getItem('userId'))

    const categories = {
      food: '美食', travel: '旅行', digital: '数码', fashion: '穿搭',
      home: '家居', beauty: '美妆', fitness: '健身', other: '其他'
    }

    const levelNames = {
      1: '布衣无名', 2: '里闾崭露', 3: '一技鸣乡', 4: '名动郡邑',
      5: '誉满江湖', 6: '声闻天下', 7: '名冠当世', 8: '千古流芳', 9: '万世不朽'
    }

    const getLevelName = (level) => levelNames[level] || '布衣无名'
    const getCategoryName = (key) => categories[key] || '其他'

    const formatTime = (time) => {
      const date = new Date(time)
      return date.toLocaleString('zh-CN')
    }

    const formatNumber = (num) => {
      if (num >= 100000000) return (num / 100000000).toFixed(1) + '亿'
      if (num >= 10000) return (num / 10000).toFixed(1) + '万'
      return num?.toString() || '0'
    }

    const loadPost = async () => {
      try {
        const res = await api.getPost(route.params.id)
        post.value = res
        comments.value = res.comments || []
      } catch (error) {
        console.error('Failed to load post:', error)
      }
    }

    const toggleLike = async () => {
      if (!currentUserId.value) {
        router.push('/login')
        return
      }
      
      try {
        if (post.value.isLiked) {
          await api.unlikePost(post.value.id)
          post.value.likesCount--
        } else {
          await api.likePost(post.value.id)
          post.value.likesCount++
        }
        post.value.isLiked = !post.value.isLiked
      } catch (error) {
        alert(error.response?.data?.error || '操作失败')
      }
    }

    const toggleCollect = async () => {
      if (!currentUserId.value) {
        router.push('/login')
        return
      }
      
      try {
        if (post.value.isCollected) {
          await api.uncollectPost(post.value.id)
          post.value.collectsCount--
        } else {
          await api.collectPost(post.value.id)
          post.value.collectsCount++
        }
        post.value.isCollected = !post.value.isCollected
      } catch (error) {
        alert(error.response?.data?.error || '操作失败')
      }
    }

    const toggleDislike = async () => {
      if (!currentUserId.value) {
        router.push('/login')
        return
      }
      
      try {
        if (post.value.isDisliked) {
          await api.undislikePost(post.value.id)
          post.value.dislikesCount--
        } else {
          await api.dislikePost(post.value.id)
          post.value.dislikesCount++
        }
        post.value.isDisliked = !post.value.isDisliked
      } catch (error) {
        alert(error.response?.data?.error || '操作失败')
      }
    }

    const addComment = async () => {
      if (!currentUserId.value) {
        router.push('/login')
        return
      }
      
      if (!newComment.value.trim()) return
      
      try {
        await api.addComment(post.value.id, newComment.value)
        await loadPost()
        newComment.value = ''
      } catch (error) {
        alert(error.response?.data?.error || '评论失败')
      }
    }

    const deleteComment = async (commentId) => {
      try {
        await api.deleteComment(commentId)
        comments.value = comments.value.filter(c => c.id !== commentId)
        post.value.commentsCount--
      } catch (error) {
        alert(error.response?.data?.error || '删除失败')
      }
    }

    const viewProfile = (id) => router.push(`/profile/${id}`)

    onMounted(() => {
      loadPost()
    })

    return {
      post,
      comments,
      newComment,
      currentUserId,
      getLevelName,
      getCategoryName,
      formatTime,
      formatNumber,
      toggleLike,
      toggleCollect,
      toggleDislike,
      addComment,
      deleteComment,
      viewProfile
    }
  }
}
</script>

<style scoped>
.post-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.post-container {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 30px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.post-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  cursor: pointer;
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
.level-4 { background: linear-gradient(135deg, #b8860b, #daa520); color: white; }
.level-5 { background: linear-gradient(135deg, #8b4513, #cd853f); color: #fff8dc; }
.level-6 { background: linear-gradient(135deg, #4a0e0e, #8b0000); color: #ffd700; }
.level-9 { background: linear-gradient(135deg, #0f0f0f, #1c1c1c); color: #ffd700; border: 1px solid #ffd700; }

.post-meta {
  color: #999;
  font-size: 14px;
}

.category-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-radius: 12px;
  font-size: 12px;
  margin-bottom: 15px;
}

.post-title {
  font-size: 24px;
  margin-bottom: 20px;
}

.post-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.post-image {
  width: 100%;
  max-height: 500px;
  object-fit: cover;
  border-radius: 15px;
  margin-bottom: 20px;
}

.post-actions {
  display: flex;
  gap: 20px;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.action-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  padding: 10px 20px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}

.action-btn.active {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
}

.comments-section {
  margin-top: 30px;
}

.comments-section h3 {
  margin-bottom: 20px;
}

.comment-form {
  margin-bottom: 30px;
}

.comment-form textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid #e0e0e0;
  border-radius: 15px;
  resize: vertical;
  min-height: 100px;
  margin-bottom: 10px;
}

.comment-form button {
  padding: 10px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  overflow: hidden;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-content {
  flex: 1;
}

.comment-username {
  font-weight: 600;
  margin-bottom: 5px;
}

.comment-text {
  color: #333;
  margin-bottom: 5px;
}

.comment-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 12px;
}

.comment-meta button {
  background: none;
  border: none;
  color: #ff4757;
  cursor: pointer;
}
</style>
