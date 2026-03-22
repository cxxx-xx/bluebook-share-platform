<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <span class="brand-icon">📖</span>
        <h1>注册蓝书</h1>
        <p>加入我们，分享美好生活</p>
      </div>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="username" type="text" placeholder="请输入用户名" required>
        </div>

        <div class="form-group">
          <label>手机号</label>
          <div class="input-with-btn">
            <input v-model="phone" type="tel" placeholder="请输入手机号" required>
            <button type="button" @click="sendCode" :disabled="countdown > 0">
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </button>
          </div>
        </div>

        <div class="form-group">
          <label>验证码</label>
          <input v-model="code" type="text" placeholder="请输入验证码" required>
        </div>

        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="8-16位，包含数字、字母、符号中至少两种" required>
        </div>

        <div class="form-group">
          <label>确认密码</label>
          <input v-model="confirmPassword" type="password" placeholder="请再次输入密码" required>
          <p v-if="passwordError" class="error-hint">{{ passwordError }}</p>
        </div>

        <button type="submit" class="btn-register" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="register-footer">
        <p>已有账号？<router-link to="/login">立即登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const username = ref('')
    const phone = ref('')
    const code = ref('')
    const password = ref('')
    const confirmPassword = ref('')
    const passwordError = ref('')
    const countdown = ref(0)
    const loading = ref(false)

    watch([password, confirmPassword], () => {
      if (confirmPassword.value && password.value !== confirmPassword.value) {
        passwordError.value = '两次输入的密码不一致'
      } else {
        passwordError.value = ''
      }
    })

    const sendCode = async () => {
      if (!phone.value || !/^1\d{10}$/.test(phone.value)) {
        alert('请输入正确的手机号')
        return
      }

      try {
        const res = await api.sendSms(phone.value)
        if (res.success) {
          alert('验证码已发送')
          countdown.value = 60
          const timer = setInterval(() => {
            countdown.value--
            if (countdown.value <= 0) {
              clearInterval(timer)
            }
          }, 1000)
        } else {
          alert(res.error || '发送失败')
        }
      } catch (error) {
        alert(error.response?.data?.error || '发送失败')
      }
    }

    const handleRegister = async () => {
      if (password.value !== confirmPassword.value) {
        alert('两次输入的密码不一致')
        return
      }

      if (password.value.length < 8 || password.value.length > 16) {
        alert('密码长度需要8-16位')
        return
      }

      loading.value = true
      try {
        const res = await api.register(username.value, phone.value, password.value, code.value)
        if (res.success) {
          alert('注册成功，请登录')
          router.push('/login')
        } else {
          alert(res.error || '注册失败')
        }
      } catch (error) {
        alert(error.response?.data?.error || '注册失败')
      } finally {
        loading.value = false
      }
    }

    return {
      username,
      phone,
      code,
      password,
      confirmPassword,
      passwordError,
      countdown,
      loading,
      sendCode,
      handleRegister
    }
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
  background: rgba(255, 255, 255, 0.95);
  padding: 40px;
  border-radius: 20px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.brand-icon {
  font-size: 48px;
}

.register-header h1 {
  font-size: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 10px 0;
}

.register-header p {
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

.input-with-btn {
  display: flex;
  gap: 10px;
}

.input-with-btn input {
  flex: 1;
}

.input-with-btn button {
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  white-space: nowrap;
}

.input-with-btn button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-hint {
  color: #ff4757;
  font-size: 12px;
  margin-top: 5px;
}

.btn-register {
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

.btn-register:hover:not(:disabled) {
  transform: scale(1.02);
}

.btn-register:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.register-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}
</style>
