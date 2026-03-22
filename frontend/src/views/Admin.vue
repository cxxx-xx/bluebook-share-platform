<template>
  <div class="admin-page">
    <h1>管理后台</h1>

    <div class="admin-tabs">
      <div :class="['tab', { active: activeTab === 'users' }]" @click="activeTab = 'users'">用户管理</div>
      <div :class="['tab', { active: activeTab === 'admins' }]" @click="activeTab = 'admins'">管理员管理</div>
      <div :class="['tab', { active: activeTab === 'posts' }]" @click="activeTab = 'posts'">帖子管理</div>
      <div :class="['tab', { active: activeTab === 'comments' }]" @click="activeTab = 'comments'">评论管理</div>
      <div :class="['tab', { active: activeTab === 'notification' }]" @click="activeTab = 'notification'">系统通知</div>
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === 'users'" class="admin-section">
      <div class="section-header">
        <h2>用户列表</h2>
        <button @click="showAddUser = true">添加用户</button>
      </div>
      
      <table class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>手机号</th>
            <th>等级</th>
            <th>知名度</th>
            <th>粉丝</th>
            <th>搜索</th>
            <th>角色</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.phone }}</td>
            <td>Lv.{{ user.level }}</td>
            <td>{{ formatNumber(user.fame) }}</td>
            <td>{{ formatNumber(user.fansCount) }}</td>
            <td>{{ formatNumber(user.searchCount) }}</td>
            <td>
              <span :class="['badge', user.isAdmin ? 'admin' : 'user']">
                {{ user.isAdmin ? '管理员' : '用户' }}
              </span>
            </td>
            <td>
              <button @click="editUser(user)">编辑</button>
              <button v-if="!user.isAdmin" @click="deleteUser(user.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 管理员管理 -->
    <div v-if="activeTab === 'admins'" class="admin-section">
      <div class="section-header">
        <h2>管理员列表</h2>
        <button @click="showAddAdmin = true">添加管理员</button>
      </div>
      
      <table class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>手机号</th>
            <th>等级</th>
            <th>知名度</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="admin in admins" :key="admin.id">
            <td>{{ admin.id }}</td>
            <td>{{ admin.username }}</td>
            <td>{{ admin.phone }}</td>
            <td>Lv.{{ admin.level }}</td>
            <td>{{ formatNumber(admin.fame) }}</td>
            <td>{{ formatDate(admin.createdAt) }}</td>
            <td>
              <button v-if="admin.id != currentUserId" @click="deleteAdmin(admin.id)">删除</button>
              <span v-else class="current-user-hint">当前用户</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 帖子管理 -->
    <div v-if="activeTab === 'posts'" class="admin-section">
      <h2>帖子列表</h2>
      
      <table class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>作者</th>
            <th>分类</th>
            <th>点赞</th>
            <th>收藏</th>
            <th>评论</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="post in posts" :key="post.id">
            <td>{{ post.id }}</td>
            <td>{{ post.title.substring(0, 20) }}...</td>
            <td>{{ post.user.username }}</td>
            <td>{{ getCategoryName(post.category) }}</td>
            <td>{{ post.likesCount }}</td>
            <td>{{ post.collectsCount }}</td>
            <td>{{ post.commentsCount }}</td>
            <td>{{ formatDate(post.createdAt) }}</td>
            <td>
              <button @click="viewPost(post.id)">查看</button>
              <button @click="deletePost(post.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 评论管理 -->
    <div v-if="activeTab === 'comments'" class="admin-section">
      <h2>评论列表</h2>
      
      <table class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>评论内容</th>
            <th>评论者</th>
            <th>所属帖子</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="comment in comments" :key="comment.id">
            <td>{{ comment.id }}</td>
            <td>{{ comment.content.substring(0, 30) }}...</td>
            <td>{{ comment.user?.username }}</td>
            <td>
              <a @click="viewPost(comment.post?.id)" class="post-link">{{ comment.post?.title?.substring(0, 15) }}...</a>
            </td>
            <td>{{ formatDate(comment.createdAt) }}</td>
            <td>
              <button @click="deleteComment(comment.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="comments.length === 0" class="empty-state">暂无评论</div>
    </div>

    <!-- 系统通知 -->
    <div v-if="activeTab === 'notification'" class="admin-section">
      <h2>发送系统通知</h2>
      
      <div class="notification-form">
        <div class="form-group">
          <label>发送范围</label>
          <div class="radio-group">
            <label class="radio-label">
              <input type="radio" v-model="notification.sendType" value="all">
              全部用户
            </label>
            <label class="radio-label">
              <input type="radio" v-model="notification.sendType" value="selected">
              选择用户
            </label>
          </div>
        </div>
        
        <div v-if="notification.sendType === 'selected'" class="form-group">
          <label>选择用户 (可多选)</label>
          <div class="user-select-container">
            <input 
              v-model="userSearchKeyword" 
              type="text" 
              placeholder="搜索用户..."
              @input="searchUsersForNotification"
            >
            <div class="user-select-list">
              <div 
                v-for="user in filteredUsersForNotification" 
                :key="user.id"
                :class="['user-select-item', { selected: selectedUsers.includes(user.id) }]"
                @click="toggleUserSelection(user.id)"
              >
                <input type="checkbox" :checked="selectedUsers.includes(user.id)" @click.stop>
                <span>{{ user.username }} (ID: {{ user.id }})</span>
              </div>
            </div>
            <div v-if="selectedUsers.length > 0" class="selected-users-info">
              已选择 {{ selectedUsers.length }} 个用户
            </div>
          </div>
        </div>
        
        <div class="form-group">
          <label>标题</label>
          <input v-model="notification.title" type="text">
        </div>
        <div class="form-group">
          <label>内容</label>
          <textarea v-model="notification.content" rows="4"></textarea>
        </div>
        <button @click="sendNotification" :disabled="sending">
          {{ sending ? '发送中...' : '发送通知' }}
        </button>
      </div>
    </div>

    <!-- 添加/编辑用户弹窗 -->
    <div v-if="showAddUser || editingUser" class="modal" @click.self="closeUserModal">
      <div class="modal-content">
        <h2>{{ editingUser ? '编辑用户' : '添加用户' }}</h2>
        
        <div class="form-group">
          <label>用户名</label>
          <input v-model="userForm.username" type="text">
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="userForm.phone" type="text">
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="userForm.password" type="password" placeholder="留空则不修改">
        </div>
        <div class="form-group">
          <label>等级</label>
          <select v-model="userForm.level">
            <option v-for="i in 9" :key="i" :value="i">Lv.{{ i }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>知名度</label>
          <input v-model.number="userForm.fame" type="number">
        </div>
        
        <div class="form-actions">
          <button @click="closeUserModal">取消</button>
          <button class="btn-primary" @click="saveUser">保存</button>
        </div>
      </div>
    </div>

    <!-- 添加管理员弹窗 -->
    <div v-if="showAddAdmin" class="modal" @click.self="showAddAdmin = false">
      <div class="modal-content">
        <h2>添加管理员</h2>
        
        <div class="form-group">
          <label>用户名</label>
          <input v-model="adminForm.username" type="text">
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="adminForm.phone" type="text">
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="adminForm.password" type="password">
        </div>
        
        <div class="form-actions">
          <button @click="showAddAdmin = false">取消</button>
          <button class="btn-primary" @click="saveAdmin">保存</button>
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
  name: 'Admin',
  setup() {
    const router = useRouter()
    const activeTab = ref('users')
    const users = ref([])
    const posts = ref([])
    const comments = ref([])
    const admins = ref([])
    const showAddUser = ref(false)
    const showAddAdmin = ref(false)
    const editingUser = ref(null)
    const currentUserId = ref(localStorage.getItem('userId'))
    
    const userForm = ref({
      username: '',
      phone: '',
      password: '',
      level: 1,
      fame: 0
    })
    
    const adminForm = ref({
      username: '',
      phone: '',
      password: ''
    })
    
    const notification = ref({
      title: '',
      content: '',
      sendType: 'all'
    })
    const sending = ref(false)
    const userSearchKeyword = ref('')
    const filteredUsersForNotification = ref([])
    const selectedUsers = ref([])

    const categories = {
      food: '美食', travel: '旅行', digital: '数码', fashion: '穿搭',
      home: '家居', beauty: '美妆', fitness: '健身', other: '其他'
    }

    const formatNumber = (num) => {
      if (num >= 100000000) return (num / 100000000).toFixed(1) + '亿'
      if (num >= 10000) return (num / 10000).toFixed(1) + '万'
      return num?.toString() || '0'
    }

    const formatDate = (date) => {
      if (!date) return ''
      return new Date(date).toLocaleDateString()
    }

    const getCategoryName = (key) => categories[key] || '其他'

    const loadUsers = async () => {
      try {
        const res = await api.getAdminUsers()
        users.value = res.users || []
      } catch (error) {
        console.error('Failed to load users:', error)
      }
    }

    const loadPosts = async () => {
      try {
        const res = await api.getAdminPosts()
        posts.value = res.posts || []
      } catch (error) {
        console.error('Failed to load posts:', error)
      }
    }

    const loadComments = async () => {
      try {
        const res = await api.getAdminComments()
        comments.value = res.comments || []
      } catch (error) {
        console.error('Failed to load comments:', error)
      }
    }

    const loadAdmins = async () => {
      try {
        const res = await api.getAdmins()
        admins.value = res.admins || []
      } catch (error) {
        console.error('Failed to load admins:', error)
      }
    }

    const editUser = (user) => {
      editingUser.value = user
      userForm.value = {
        username: user.username,
        phone: user.phone,
        password: '',
        level: user.level,
        fame: user.fame
      }
    }

    const closeUserModal = () => {
      showAddUser.value = false
      editingUser.value = null
      userForm.value = { username: '', phone: '', password: '', level: 1, fame: 0 }
    }

    const saveUser = async () => {
      try {
        if (editingUser.value) {
          await api.adminUpdateUser(editingUser.value.id, userForm.value)
        } else {
          await api.adminCreateUser(userForm.value)
        }
        closeUserModal()
        loadUsers()
      } catch (error) {
        alert(error.response?.data?.error || '操作失败')
      }
    }

    const saveAdmin = async () => {
      if (!adminForm.value.username || !adminForm.value.password || !adminForm.value.phone) {
        alert('请填写完整信息')
        return
      }
      
      try {
        await api.createAdmin(adminForm.value)
        alert('管理员创建成功')
        showAddAdmin.value = false
        adminForm.value = { username: '', phone: '', password: '' }
        loadAdmins()
      } catch (error) {
        alert(error.response?.data?.error || '创建失败')
      }
    }

    const deleteUser = async (userId) => {
      if (!confirm('确定删除该用户？')) return
      
      try {
        await api.adminDeleteUser(userId)
        users.value = users.value.filter(u => u.id !== userId)
      } catch (error) {
        alert(error.response?.data?.error || '删除失败')
      }
    }

    const deleteAdmin = async (adminId) => {
      if (!confirm('确定删除该管理员？')) return
      
      try {
        await api.deleteAdmin(adminId)
        admins.value = admins.value.filter(a => a.id !== adminId)
      } catch (error) {
        alert(error.response?.data?.error || '删除失败')
      }
    }

    const deleteComment = async (commentId) => {
      if (!confirm('确定删除该评论？')) return
      
      try {
        await api.adminDeleteComment(commentId)
        comments.value = comments.value.filter(c => c.id !== commentId)
      } catch (error) {
        alert(error.response?.data?.error || '删除失败')
      }
    }

    const viewPost = (id) => router.push(`/post/${id}`)

    const deletePost = async (postId) => {
      if (!confirm('确定删除该帖子？')) return
      
      try {
        await api.adminDeletePost(postId)
        posts.value = posts.value.filter(p => p.id !== postId)
      } catch (error) {
        alert(error.response?.data?.error || '删除失败')
      }
    }

    const sendNotification = async () => {
      if (!notification.value.title || !notification.value.content) {
        alert('请填写标题和内容')
        return
      }
      
      if (notification.value.sendType === 'selected' && selectedUsers.value.length === 0) {
        alert('请选择至少一个用户')
        return
      }
      
      sending.value = true
      try {
        const data = {
          title: notification.value.title,
          content: notification.value.content,
          sendType: notification.value.sendType,
          userIds: notification.value.sendType === 'selected' ? selectedUsers.value : null
        }
        await api.sendSystemNotification(data.title, data.content, data.sendType, data.userIds)
        alert('通知发送成功')
        notification.value = { title: '', content: '', sendType: 'all' }
        selectedUsers.value = []
        userSearchKeyword.value = ''
      } catch (error) {
        alert(error.response?.data?.error || '发送失败')
      } finally {
        sending.value = false
      }
    }

    const searchUsersForNotification = () => {
      const keyword = userSearchKeyword.value.toLowerCase()
      if (!keyword) {
        filteredUsersForNotification.value = users.value.slice(0, 20)
      } else {
        filteredUsersForNotification.value = users.value.filter(u => 
          u.username.toLowerCase().includes(keyword) || 
          u.id.toString().includes(keyword)
        ).slice(0, 20)
      }
    }

    const toggleUserSelection = (userId) => {
      const index = selectedUsers.value.indexOf(userId)
      if (index > -1) {
        selectedUsers.value.splice(index, 1)
      } else {
        selectedUsers.value.push(userId)
      }
    }

    onMounted(() => {
      loadUsers()
      loadPosts()
      loadComments()
      loadAdmins()
    })

    return {
      activeTab,
      users,
      posts,
      comments,
      admins,
      showAddUser,
      showAddAdmin,
      editingUser,
      userForm,
      adminForm,
      notification,
      sending,
      userSearchKeyword,
      filteredUsersForNotification,
      selectedUsers,
      currentUserId,
      formatNumber,
      formatDate,
      getCategoryName,
      editUser,
      closeUserModal,
      saveUser,
      saveAdmin,
      deleteUser,
      deleteAdmin,
      deleteComment,
      viewPost,
      deletePost,
      sendNotification,
      searchUsersForNotification,
      toggleUserSelection
    }
  }
}
</script>

<style scoped>
.admin-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.admin-page h1 {
  color: white;
  margin-bottom: 20px;
}

.admin-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tab {
  padding: 10px 25px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  cursor: pointer;
}

.tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.admin-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th,
.admin-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.admin-table th {
  background: #f5f5f5;
  font-weight: 600;
}

.badge {
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 12px;
}

.badge.admin {
  background: #ff4757;
  color: white;
}

.badge.user {
  background: #667eea;
  color: white;
}

button {
  padding: 6px 12px;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  margin-right: 5px;
}

.notification-form {
  max-width: 500px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
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
  max-width: 500px;
}

.radio-group {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-select-container {
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  overflow: hidden;
}

.user-select-container > input {
  width: 100%;
  padding: 10px;
  border: none;
  border-bottom: 1px solid #e0e0e0;
}

.user-select-list {
  max-height: 200px;
  overflow-y: auto;
}

.user-select-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.user-select-item:hover {
  background: rgba(102, 126, 234, 0.1);
}

.user-select-item.selected {
  background: rgba(102, 126, 234, 0.2);
}

.selected-users-info {
  padding: 10px;
  background: #f5f5f5;
  font-size: 14px;
  color: #666;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
