<template>
  <div class="collections-page">
    <h1>我的收藏</h1>

    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="collections.length === 0" class="empty-state">暂无收藏</div>
    
    <div v-else class="posts-grid">
      <div v-for="post in collections" :key="post.id" class="post-card" @click="viewPost(post.id)">
        <h3>{{ post.title }}</h3>
        <p>{{ post.content.substring(0, 100) }}...</p>
        <div class="post-meta">
          <span>{{ post.user.username }}</span>
          <span>{{ formatTime(post.createdAt) }}</span>
        </div>
        <div class="post-stats">
          <span>❤️ {{ post.likesCount }}</span>
          <span>⭐ {{ post.collectsCount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api'

export default {
  name: 'Collections',
  setup() {
    const router = useRouter()
    const collections = ref([])
    const loading = ref(true)

    const formatTime = (time) => {
      const date = new Date(time)
      return date.toLocaleDateString()
    }

    const loadCollections = async () => {
      loading.value = true
      try {
        const res = await api.getCollections()
        collections.value = res.collections || []
      } catch (error) {
        console.error('Failed to load collections:', error)
      } finally {
        loading.value = false
      }
    }

    const viewPost = (id) => router.push(`/post/${id}`)

    onMounted(() => {
      loadCollections()
    })

    return {
      collections,
      loading,
      formatTime,
      viewPost
    }
  }
}
</script>

<style scoped>
.collections-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.collections-page h1 {
  color: white;
  margin-bottom: 20px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.post-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-radius: 15px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.post-card:hover {
  transform: translateY(-5px);
}

.post-card h3 {
  margin-bottom: 10px;
}

.post-card p {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 12px;
  margin-bottom: 10px;
}

.post-stats {
  display: flex;
  gap: 15px;
  color: #666;
  font-size: 12px;
}

.loading, .empty-state {
  text-align: center;
  padding: 50px;
  color: #666;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
}
</style>
