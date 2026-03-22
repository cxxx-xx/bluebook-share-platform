<template>
  <div class="ranking-page">
    <h1>{{ rankTitle }}</h1>

    <div class="tabs-container">
      <div class="tab-group">
        <span class="tab-group-title">总榜</span>
        <div :class="['tab', { active: currentType === 'search' }]" @click="switchType('search')">搜索量榜</div>
        <div :class="['tab', { active: currentType === 'fame' }]" @click="switchType('fame')">知名度榜</div>
      </div>
      <div class="tab-group">
        <span class="tab-group-title">知名度上涨榜</span>
        <div :class="['tab', { active: currentType === 'daily' }]" @click="switchType('daily')">日榜</div>
        <div :class="['tab', { active: currentType === 'weekly' }]" @click="switchType('weekly')">周榜</div>
        <div :class="['tab', { active: currentType === 'monthly' }]" @click="switchType('monthly')">月榜</div>
        <div :class="['tab', { active: currentType === 'yearly' }]" @click="switchType('yearly')">年榜</div>
      </div>
      <div class="tab-group">
        <span class="tab-group-title">互动榜</span>
        <div :class="['tab', { active: currentType === 'fans' }]" @click="switchType('fans')">粉丝榜</div>
        <div :class="['tab', { active: currentType === 'likes' }]" @click="switchType('likes')">获赞榜</div>
        <div :class="['tab', { active: currentType === 'posts' }]" @click="switchType('posts')">分享榜</div>
        <div :class="['tab', { active: currentType === 'collects' }]" @click="switchType('collects')">收藏榜</div>
      </div>
    </div>

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

    <div class="ranking-list">
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="users.length === 0" class="empty-state">暂无数据</div>
      
      <div v-else>
        <div 
          v-for="user in users" 
          :key="user.id" 
          class="ranking-item"
          @click="viewProfile(user.id)"
        >
          <div :class="['rank-number', `rank-${user.rank}`]">{{ user.rank }}</div>
          <div class="user-avatar">
            <img v-if="user.avatar" :src="`/avatars/${user.avatar}`" @error="handleAvatarError">
            <span v-else>{{ user.username.charAt(0).toUpperCase() }}</span>
          </div>
          <div class="user-info">
            <div class="user-name">
              {{ user.username }}
              <span :class="['user-level', `level-${user.level}`]">
                Lv.{{ user.level }} {{ getLevelName(user.level) }}
              </span>
            </div>
            <div class="user-stats">
              粉丝 {{ formatNumber(user.fansCount) }} | 分享 {{ formatNumber(user.postCount) }} | 获赞 {{ formatNumber(user.likesReceived) }}
            </div>
          </div>
          <div class="user-score">
            <div class="score-value">{{ formatNumber(user.scoreValue) }}</div>
            <div class="score-label">{{ scoreLabel }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from '@/api'

export default {
  name: 'Ranking',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const users = ref([])
    const loading = ref(true)
    const currentType = ref('search')
    const currentCategory = ref('all')

    const categories = [
      { key: 'all', name: '总榜' },
      { key: 'food', name: '美食' },
      { key: 'travel', name: '旅行' },
      { key: 'digital', name: '数码' },
      { key: 'fashion', name: '穿搭' },
      { key: 'home', name: '家居' },
      { key: 'beauty', name: '美妆' },
      { key: 'fitness', name: '健身' },
      { key: 'other', name: '其他' }
    ]

    const rankTitles = {
      search: '搜索量排行榜',
      fame: '知名度排行榜',
      daily: '日知名度上涨榜',
      weekly: '周知名度上涨榜',
      monthly: '月知名度上涨榜',
      yearly: '年知名度上涨榜',
      fans: '粉丝排行榜',
      likes: '获赞排行榜',
      posts: '分享排行榜',
      collects: '收藏排行榜'
    }

    const scoreLabels = {
      search: '搜索次数',
      fame: '知名度',
      daily: '日增长',
      weekly: '周增长',
      monthly: '月增长',
      yearly: '年增长',
      fans: '粉丝数',
      likes: '获赞数',
      posts: '分享数',
      collects: '被收藏数'
    }

    const levelNames = {
      1: '布衣无名', 2: '里闾崭露', 3: '一技鸣乡', 4: '名动郡邑',
      5: '誉满江湖', 6: '声闻天下', 7: '名冠当世', 8: '千古流芳', 9: '万世不朽'
    }

    const rankTitle = computed(() => rankTitles[currentType.value])
    const scoreLabel = computed(() => scoreLabels[currentType.value])

    const getLevelName = (level) => levelNames[level] || '布衣无名'

    const formatNumber = (num) => {
      if (num >= 100000000) return (num / 100000000).toFixed(1) + '亿'
      if (num >= 10000) return (num / 10000).toFixed(1) + '万'
      return num?.toString() || '0'
    }

    const loadRanking = async () => {
      loading.value = true
      try {
        let res
        const interactionTypes = ['fans', 'likes', 'posts', 'collects']
        
        if (interactionTypes.includes(currentType.value)) {
          res = await api.getRanking(currentType.value)
        } else if (currentCategory.value === 'all') {
          res = await api.getRanking(currentType.value)
        } else {
          res = await api.getCategoryRanking(currentCategory.value, currentType.value)
        }
        
        users.value = res.users || []
      } catch (error) {
        console.error('Failed to load ranking:', error)
      } finally {
        loading.value = false
      }
    }

    const switchType = (type) => {
      currentType.value = type
      loadRanking()
    }

    const switchCategory = (category) => {
      currentCategory.value = category
      loadRanking()
    }

    const viewProfile = (id) => router.push(`/profile/${id}`)

    const handleAvatarError = (e) => {
      e.target.style.display = 'none'
    }

    onMounted(() => {
      loadRanking()
    })

    return {
      users,
      loading,
      currentType,
      currentCategory,
      categories,
      rankTitle,
      scoreLabel,
      getLevelName,
      formatNumber,
      switchType,
      switchCategory,
      viewProfile,
      handleAvatarError
    }
  }
}
</script>

<style scoped>
.ranking-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.ranking-page h1 {
  text-align: center;
  color: white;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.tabs-container {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-radius: 15px;
  margin-bottom: 20px;
}

.tab-group {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.tab-group:last-child {
  margin-bottom: 0;
}

.tab-group-title {
  font-weight: 600;
  color: #667eea;
  min-width: 100px;
}

.tab {
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(102, 126, 234, 0.1);
}

.tab:hover {
  background: rgba(102, 126, 234, 0.2);
}

.tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
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
}

.category-tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.ranking-list {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 20px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.ranking-item:hover {
  background: rgba(102, 126, 234, 0.1);
}

.rank-number {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  border-radius: 10px;
  background: #e0e0e0;
}

.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffb300);
  color: white;
}

.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
  color: white;
}

.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #b87333);
  color: white;
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

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-level {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.level-1 { background: #e8e8e8; color: #666; }
.level-4 { background: linear-gradient(135deg, #b8860b, #daa520); color: white; }
.level-5 { background: linear-gradient(135deg, #8b4513, #cd853f); color: #fff8dc; }
.level-6 { background: linear-gradient(135deg, #4a0e0e, #8b0000); color: #ffd700; }
.level-9 { background: linear-gradient(135deg, #0f0f0f, #1c1c1c); color: #ffd700; border: 1px solid #ffd700; }

.user-stats {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.user-score {
  text-align: right;
}

.score-value {
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
}

.score-label {
  font-size: 12px;
  color: #999;
}

.loading, .empty-state {
  text-align: center;
  padding: 50px;
  color: #666;
}
</style>
