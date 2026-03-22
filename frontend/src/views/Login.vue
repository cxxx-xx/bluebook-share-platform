<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <span class="brand-icon">📖</span>
        <h1>蓝书</h1>
        <p>多品类好物分享平台</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>账号</label>
          <input v-model="username" type="text" placeholder="用户名或手机号" required>
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" required>
        </div>

        <div class="form-group">
          <label>验证码（请点击高亮的格子）</label>
          <div class="captcha-grid">
            <div 
              v-for="(pos, index) in 16" 
              :key="index"
              :class="['captcha-cell', { 
                highlight: isHighlight(index),
                clicked: clickedPositions.includes(index)
              }]"
              @click="handleCaptchaClick(index)"
            >
              <span v-if="clickedPositions.includes(index)" class="click-mark">✓</span>
            </div>
          </div>
        </div>

        <div class="form-group remember-me">
          <label class="checkbox-label">
            <input type="checkbox" v-model="rememberMe">
            <span>记住我（7天内免登录）</span>
          </label>
        </div>

        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="login-footer">
        <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const username = ref('')
    const password = ref('')
    const clickedPositions = ref([])
    const highlightPositions = ref([])
    const loading = ref(false)
    const rememberMe = ref(false)

    const loadCaptcha = async () => {
      try {
        const positions = await api.getCaptcha()
        const coords = positions.split(',').map(Number)
        highlightPositions.value = []
        for (let i = 0; i < coords.length; i += 2) {
          const row = coords[i]
          const col = coords[i + 1]
          const index = row * 4 + col
          highlightPositions.value.push(index)
        }
        clickedPositions.value = []
      } catch (error) {
        console.error('Failed to load captcha:', error)
      }
    }

    const isHighlight = (index) => {
      return highlightPositions.value.includes(index)
    }

    const handleCaptchaClick = (index) => {
      if (!isHighlight(index)) return
      
      if (clickedPositions.value.includes(index)) {
        clickedPositions.value = clickedPositions.value.filter(i => i !== index)
      } else {
        clickedPositions.value.push(index)
      }
    }

    const handleLogin = async () => {
      if (clickedPositions.value.length !== 4) {
        alert('请点击4个高亮格子')
        return
      }

      loading.value = true
      try {
        const captcha = clickedPositions.value.map(index => {
          const row = Math.floor(index / 4)
          const col = index % 4
          return `${row},${col}`
        }).join(',')
        
        const res = await api.login(username.value, password.value, captcha, rememberMe.value)
        
        if (res.success) {
          localStorage.setItem('userId', res.id)
          localStorage.setItem('username', res.username)
          localStorage.setItem('isAdmin', res.isAdmin)
          window.location.href = '/'
        } else {
          alert(res.error || '登录失败')
          loadCaptcha()
        }
      } catch (error) {
        alert(error.response?.data?.error || '登录失败')
        loadCaptcha()
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      loadCaptcha()
    })

    return {
      username,
      password,
      clickedPositions,
      loading,
      rememberMe,
      isHighlight,
      handleCaptchaClick,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  background: rgba(255, 255, 255, 0.95);
  padding: 40px;
  border-radius: 20px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.brand-icon {
  font-size: 48px;
}

.login-header h1 {
  font-size: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 10px 0;
}

.login-header p {
  color: #666;
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

.form-group input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.captcha-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 5px;
  margin-bottom: 10px;
}

.captcha-cell {
  aspect-ratio: 1;
  background: #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.captcha-cell:hover {
  opacity: 0.8;
}

.captcha-cell.highlight {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.captcha-cell.clicked {
  background: #4CAF50 !important;
}

.click-mark {
  color: white;
  font-weight: bold;
  font-size: 20px;
}

.captcha-hint {
  font-size: 12px;
  color: #666;
}

.btn-login {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.btn-login:hover:not(:disabled) {
  transform: scale(1.02);
}

.btn-login:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.login-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.remember-me {
  margin: 15px 0;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}
</style>
