/**
 * API接口封装模块
 * 
 * 技术说明：
 * - 使用Axios作为HTTP客户端
 * - 支持跨域请求（withCredentials）
 * - RESTful API设计
 * - 统一的错误处理
 * 
 * API分类：
 * 1. 用户模块：注册、登录、验证码、用户信息
 * 2. 帖子模块：帖子CRUD、点赞、收藏、评论
 * 3. 关注模块：关注/取关、粉丝列表
 * 4. 消息模块：私信、会话
 * 5. 通知模块：系统通知
 * 6. 排行榜模块：各类排行
 * 7. 管理员模块：用户管理、帖子管理
 * 
 * @author Bluebook Team
 * @version 1.0
 */

import axios from 'axios'

// API基础路径，通过Vite代理转发到后端
const BASE_URL = '/api'

// 配置axios默认携带cookie/session
// 必须设置，否则Session无法保持登录状态
axios.defaults.withCredentials = true

/**
 * API接口对象
 * 封装所有与后端通信的方法
 */
const api = {
  // ==================== 用户模块 ====================
  
  /**
   * 用户注册
   * @param {string} username - 用户名
   * @param {string} phone - 手机号
   * @param {string} password - 密码
   * @param {string} code - 短信验证码
   * @returns {Promise<Object>} 注册结果
   */
  async register(username, phone, password, code) {
    const res = await axios.post(`${BASE_URL}/users/register`, {
      username,
      phone,
      password
    }, {
      params: { code }
    })
    return res.data
  },

  /**
   * 用户登录
   * @param {string} username - 用户名
   * @param {string} password - 密码
   * @param {string} captcha - 图形验证码
   * @param {boolean} remember - 是否记住登录（7天免登录）
   * @returns {Promise<Object>} 登录结果，包含用户信息
   */
  async login(username, password, captcha, remember = false) {
    const res = await axios.post(`${BASE_URL}/users/login`, null, {
      params: { username, password, captcha, remember }
    })
    return res.data
  },

  /**
   * 发送短信验证码
   * @param {string} phone - 手机号
   * @returns {Promise<Object>} 发送结果
   */
  async sendSms(phone) {
    const res = await axios.post(`${BASE_URL}/users/send-sms`, null, {
      params: { phone }
    })
    return res.data
  },

  /**
   * 获取图形验证码
   * @returns {Promise<string>} 验证码位置字符串
   */
  async getCaptcha() {
    const res = await axios.get(`${BASE_URL}/users/captcha`)
    return res.data
  },

  /**
   * 检查登录状态
   * @returns {Promise<Object>} 用户信息或空
   */
  async checkLogin() {
    const res = await axios.get(`${BASE_URL}/users/check-login`)
    return res.data
  },

  /**
   * 用户登出
   * @returns {Promise<Object>} 登出结果
   */
  async logout() {
    const res = await axios.post(`${BASE_URL}/users/logout`)
    return res.data
  },

  /**
   * 获取用户信息
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 用户详细信息
   */
  async getUserInfo(userId) {
    const res = await axios.get(`${BASE_URL}/users/info/${userId}`)
    return res.data
  },

  /**
   * 上传用户头像
   * @param {number} userId - 用户ID
   * @param {File} file - 图片文件
   * @returns {Promise<Object>} 上传结果
   */
  async uploadAvatar(userId, file) {
    const formData = new FormData()
    formData.append('file', file)
    const res = await axios.post(`${BASE_URL}/users/avatar?userId=${userId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return res.data
  },

  /**
   * 获取用户收藏列表
   * @returns {Promise<Object>} 收藏列表
   */
  async getCollections() {
    const res = await axios.get(`${BASE_URL}/users/collections`)
    return res.data
  },

  // ==================== 帖子模块 ====================

  /**
   * 获取用户的帖子列表
   * @param {number} userId - 用户ID
   * @returns {Promise<Array>} 帖子列表
   */
  async getUserPosts(userId) {
    const res = await axios.get(`${BASE_URL}/posts/user/${userId}`)
    return res.data
  },

  /**
   * 获取用户点赞的帖子列表
   * @param {number} userId - 用户ID
   * @returns {Promise<Array>} 点赞的帖子列表
   */
  async getUserLikedPosts(userId) {
    const res = await axios.get(`${BASE_URL}/posts/liked/${userId}`)
    return res.data
  },

  /**
   * 获取帖子列表（分页）
   * @param {number} page - 页码（从0开始）
   * @param {number} size - 每页数量
   * @returns {Promise<Object>} 帖子列表和分页信息
   */
  async getPosts(page = 0, size = 20) {
    const res = await axios.get(`${BASE_URL}/posts`, { params: { page, size } })
    return res.data
  },

  /**
   * 搜索帖子
   * @param {string} keyword - 搜索关键词
   * @param {number} page - 页码
   * @param {number} size - 每页数量
   * @returns {Promise<Object>} 搜索结果
   */
  async searchPosts(keyword, page = 0, size = 20) {
    const res = await axios.get(`${BASE_URL}/posts/search`, { params: { keyword, page, size } })
    return res.data
  },

  /**
   * 获取热门帖子
   * @param {number} page - 页码
   * @param {number} size - 每页数量
   * @returns {Promise<Object>} 热门帖子列表
   */
  async getHotPosts(page = 0, size = 20) {
    const res = await axios.get(`${BASE_URL}/posts/hot`, { params: { page, size } })
    return res.data
  },

  /**
   * 获取推荐帖子
   * @param {number} page - 页码
   * @param {number} size - 每页数量
   * @returns {Promise<Object>} 推荐帖子列表
   */
  async getRecommendedPosts(page = 0, size = 20) {
    const res = await axios.get(`${BASE_URL}/posts/recommended`, { params: { page, size } })
    return res.data
  },

  /**
   * 按分类获取帖子
   * @param {string} category - 分类名称
   * @param {number} page - 页码
   * @param {number} size - 每页数量
   * @returns {Promise<Object>} 分类帖子列表
   */
  async getPostsByCategory(category, page = 0, size = 20) {
    const res = await axios.get(`${BASE_URL}/posts/category/${category}`, { params: { page, size } })
    return res.data
  },

  /**
   * 获取帖子详情
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 帖子详情
   */
  async getPost(postId) {
    const res = await axios.get(`${BASE_URL}/posts/${postId}`)
    return res.data
  },

  /**
   * 创建帖子
   * @param {Object} post - 帖子对象
   * @returns {Promise<Object>} 创建结果
   */
  async createPost(post) {
    const res = await axios.post(`${BASE_URL}/posts`, post)
    return res.data
  },

  /**
   * 点赞帖子
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 点赞结果
   */
  async likePost(postId) {
    const res = await axios.post(`${BASE_URL}/posts/${postId}/like`)
    return res.data
  },

  /**
   * 取消点赞
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 取消结果
   */
  async unlikePost(postId) {
    const res = await axios.delete(`${BASE_URL}/posts/${postId}/like`)
    return res.data
  },

  /**
   * 收藏帖子
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 收藏结果
   */
  async collectPost(postId) {
    const res = await axios.post(`${BASE_URL}/posts/${postId}/collect`)
    return res.data
  },

  /**
   * 取消收藏
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 取消结果
   */
  async uncollectPost(postId) {
    const res = await axios.delete(`${BASE_URL}/posts/${postId}/collect`)
    return res.data
  },

  /**
   * 踩帖子
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 踩结果
   */
  async dislikePost(postId) {
    const res = await axios.post(`${BASE_URL}/posts/${postId}/dislike`)
    return res.data
  },

  /**
   * 取消踩
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 取消结果
   */
  async undislikePost(postId) {
    const res = await axios.delete(`${BASE_URL}/posts/${postId}/dislike`)
    return res.data
  },

  /**
   * 添加评论
   * @param {number} postId - 帖子ID
   * @param {string} content - 评论内容
   * @returns {Promise<Object>} 评论结果
   */
  async addComment(postId, content) {
    const res = await axios.post(`${BASE_URL}/posts/${postId}/comments`, null, { params: { content } })
    return res.data
  },

  /**
   * 删除评论
   * @param {number} commentId - 评论ID
   * @returns {Promise<Object>} 删除结果
   */
  async deleteComment(commentId) {
    const res = await axios.delete(`${BASE_URL}/posts/comments/${commentId}`)
    return res.data
  },

  /**
   * 上传帖子图片
   * @param {File} file - 图片文件
   * @returns {Promise<Object>} 上传结果，包含图片URL
   */
  async uploadPostImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    const res = await axios.post(`${BASE_URL}/posts/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return res.data
  },

  // ==================== 关注模块 ====================

  /**
   * 关注用户
   * @param {number} userId - 被关注用户ID
   * @returns {Promise<Object>} 关注结果
   */
  async follow(userId) {
    const res = await axios.post(`${BASE_URL}/follows/${userId}`)
    return res.data
  },

  /**
   * 取消关注
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 取消结果
   */
  async unfollow(userId) {
    const res = await axios.delete(`${BASE_URL}/follows/${userId}`)
    return res.data
  },

  /**
   * 获取关注状态
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 关注状态
   */
  async getFollowStatus(userId) {
    const res = await axios.get(`${BASE_URL}/follows/status/${userId}`)
    return res.data
  },

  /**
   * 获取粉丝列表
   * @param {number} userId - 用户ID
   * @returns {Promise<Array>} 粉丝列表
   */
  async getFollowers(userId) {
    const res = await axios.get(`${BASE_URL}/follows/followers/${userId}`)
    return res.data
  },

  /**
   * 获取关注列表
   * @param {number} userId - 用户ID
   * @returns {Promise<Array>} 关注列表
   */
  async getFollowing(userId) {
    const res = await axios.get(`${BASE_URL}/follows/following/${userId}`)
    return res.data
  },

  // ==================== 消息模块 ====================

  /**
   * 发送私信
   * @param {number} receiverId - 接收者ID
   * @param {string} content - 消息内容
   * @returns {Promise<Object>} 发送结果
   */
  async sendMessage(receiverId, content) {
    const res = await axios.post(`${BASE_URL}/messages/send`, null, { params: { receiverId, content } })
    return res.data
  },

  /**
   * 获取与某用户的对话
   * @param {number} userId - 对方用户ID
   * @returns {Promise<Object>} 对话消息列表
   */
  async getConversation(userId) {
    const res = await axios.get(`${BASE_URL}/messages/conversation/${userId}`)
    return res.data
  },

  /**
   * 获取所有会话列表
   * @returns {Promise<Object>} 会话列表
   */
  async getConversations() {
    const res = await axios.get(`${BASE_URL}/messages/conversations`)
    return res.data
  },

  /**
   * 获取私信未读数量
   * @returns {Promise<Object>} 未读数量
   */
  async getUnreadCount() {
    const res = await axios.get(`${BASE_URL}/messages/unread-count`)
    return res.data
  },

  /**
   * 检查是否可以发送消息
   * @param {number} userId - 对方用户ID
   * @returns {Promise<Object>} 发送权限信息
   */
  async canSendMessage(userId) {
    const res = await axios.get(`${BASE_URL}/messages/can-send/${userId}`)
    return res.data
  },

  // ==================== 通知模块 ====================

  /**
   * 获取通知列表
   * @returns {Promise<Object>} 通知列表
   */
  async getNotifications() {
    const res = await axios.get(`${BASE_URL}/notifications`)
    return res.data
  },

  /**
   * 获取通知未读数量
   * @returns {Promise<Object>} 未读数量
   */
  async getNotificationUnreadCount() {
    const res = await axios.get(`${BASE_URL}/notifications/unread-count`)
    return res.data
  },

  /**
   * 标记通知为已读
   * @param {number} notificationId - 通知ID
   * @returns {Promise<Object>} 操作结果
   */
  async markNotificationRead(notificationId) {
    const res = await axios.put(`${BASE_URL}/notifications/${notificationId}/read`)
    return res.data
  },

  /**
   * 标记所有通知为已读
   * @returns {Promise<Object>} 操作结果
   */
  async markAllNotificationsRead() {
    const res = await axios.put(`${BASE_URL}/notifications/read-all`)
    return res.data
  },

  /**
   * 删除通知
   * @param {number} notificationId - 通知ID
   * @returns {Promise<Object>} 删除结果
   */
  async deleteNotification(notificationId) {
    const res = await axios.delete(`${BASE_URL}/notifications/${notificationId}`)
    return res.data
  },

  // ==================== 排行榜模块 ====================

  /**
   * 获取排行榜
   * @param {string} type - 排行类型（search/fame/fans/likes/collects）
   * @param {number} page - 页码
   * @param {number} size - 每页数量
   * @returns {Promise<Object>} 排行榜列表
   */
  async getRanking(type, page = 0, size = 20) {
    const res = await axios.get(`${BASE_URL}/ranking/${type}`, { params: { page, size } })
    return res.data
  },

  /**
   * 获取分类排行榜
   * @param {string} category - 分类名称
   * @param {string} type - 排行类型
   * @param {number} page - 页码
   * @param {number} size - 每页数量
   * @returns {Promise<Object>} 分类排行榜列表
   */
  async getCategoryRanking(category, type, page = 0, size = 20) {
    const res = await axios.get(`${BASE_URL}/ranking/category/${category}/${type}`, { params: { page, size } })
    return res.data
  },

  // ==================== 管理员模块 ====================

  /**
   * 获取用户列表（管理员）
   * @returns {Promise<Array>} 用户列表
   */
  async getAdminUsers() {
    const res = await axios.get(`${BASE_URL}/admin/users`)
    return res.data
  },

  /**
   * 获取帖子列表（管理员）
   * @param {number} page - 页码
   * @param {number} size - 每页数量
   * @returns {Promise<Object>} 帖子列表
   */
  async getAdminPosts(page = 0, size = 20) {
    const res = await axios.get(`${BASE_URL}/admin/posts`, { params: { page, size } })
    return res.data
  },

  /**
   * 创建用户（管理员）
   * @param {Object} user - 用户信息
   * @returns {Promise<Object>} 创建结果
   */
  async adminCreateUser(user) {
    const res = await axios.post(`${BASE_URL}/admin/users`, null, {
      params: {
        username: user.username,
        password: user.password,
        phone: user.phone
      }
    })
    return res.data
  },

  /**
   * 更新用户信息（管理员）
   * @param {number} userId - 用户ID
   * @param {Object} user - 用户信息
   * @returns {Promise<Object>} 更新结果
   */
  async adminUpdateUser(userId, user) {
    const res = await axios.put(`${BASE_URL}/admin/user/${userId}`, user)
    return res.data
  },

  /**
   * 删除用户（管理员）
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 删除结果
   */
  async adminDeleteUser(userId) {
    const res = await axios.delete(`${BASE_URL}/admin/users/${userId}`)
    return res.data
  },

  /**
   * 删除帖子（管理员）
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 删除结果
   */
  async adminDeletePost(postId) {
    const res = await axios.delete(`${BASE_URL}/admin/posts/${postId}`)
    return res.data
  },

  /**
   * 发送系统通知（管理员）
   * @param {string} title - 通知标题
   * @param {string} content - 通知内容
   * @param {string} sendType - 发送类型（all/selected）
   * @param {Array} userIds - 用户ID列表（选择用户时使用）
   * @returns {Promise<Object>} 发送结果
   */
  async sendSystemNotification(title, content, sendType = 'all', userIds = null) {
    const res = await axios.post(`${BASE_URL}/admin/notification`, null, { 
      params: { 
        title, 
        content,
        sendType,
        userIds: userIds ? userIds.join(',') : null
      } 
    })
    return res.data
  },

  async getAdmins() {
    const res = await axios.get(`${BASE_URL}/admin/admins`)
    return res.data
  },

  async createAdmin(user) {
    const res = await axios.post(`${BASE_URL}/admin/admins`, null, {
      params: {
        username: user.username,
        password: user.password,
        phone: user.phone
      }
    })
    return res.data
  },

  async deleteAdmin(adminId) {
    const res = await axios.delete(`${BASE_URL}/admin/admins/${adminId}`)
    return res.data
  },

  async getAdminComments() {
    const res = await axios.get(`${BASE_URL}/admin/comments`)
    return res.data
  },

  async adminDeleteComment(commentId) {
    const res = await axios.delete(`${BASE_URL}/admin/comments/${commentId}`)
    return res.data
  }
}

// 导出API对象
export { api }
