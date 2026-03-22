# 📚 蓝书 BlueBook

<div align="center">

一个基于 Spring Boot + Vue.js 的多品类好物分享社交平台

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

[功能特性](#-功能特性) • [技术栈](#-技术栈) • [快速开始](#-快速开始) • [项目结构](#-项目结构) • [API文档](#-api文档)

</div>

---

## 📖 项目简介

蓝书是一个现代化的好物分享社交平台，用户可以发布各类好物分享帖子，与其他用户互动交流。平台支持美食、旅行、数码、穿搭、家居、美妆、健身等多个分类，为用户提供丰富的分享和发现体验。

### 🌟 核心亮点

- 🔐 **完整的用户系统** - 注册登录、图形验证码、7天免登录
- 📝 **丰富的帖子功能** - 发布、浏览、点赞、收藏、踩、评论
- 👥 **社交互动** - 关注系统、粉丝管理、私信聊天
- 📊 **多维度排行榜** - 知名度、粉丝、获赞、收藏等排行
- 🎯 **分类体系** - 8大分类，精细化内容管理
- 🛡️ **管理员后台** - 用户管理、帖子管理、评论管理、系统通知

---

## ✨ 功能特性

### 用户模块
| 功能 | 描述 |
|------|------|
| 注册登录 | 手机号注册，图形验证码验证 |
| 7天免登录 | 可选记住登录状态，7天内免登录 |
| 个人主页 | 展示用户信息、帖子、收藏、点赞 |
| 头像上传 | 支持自定义头像上传 |

### 帖子模块
| 功能 | 描述 |
|------|------|
| 发布帖子 | 支持标题、内容、图片、分类 |
| 点赞/踩 | 对帖子进行点赞或踩操作 |
| 收藏 | 收藏喜欢的帖子 |
| 评论 | 对帖子发表评论 |
| 分类浏览 | 按8大分类筛选帖子 |
| 搜索 | 关键词搜索帖子 |

### 社交模块
| 功能 | 描述 |
|------|------|
| 关注/取关 | 关注感兴趣的用户 |
| 粉丝列表 | 查看关注者和粉丝 |
| 私信 | 与其他用户私聊（互关前最多3条消息） |
| 通知 | 系统通知、点赞、收藏、评论、关注通知 |

### 排行榜模块
| 排行类型 | 描述 |
|---------|------|
| 知名度榜 | 综合评分排行 |
| 粉丝榜 | 粉丝数量排行 |
| 获赞榜 | 获得点赞数排行 |
| 收藏榜 | 被收藏数排行 |
| 发帖榜 | 发帖数量排行 |
| 增长榜 | 日/周/月/年知名度增长排行 |
| 分类榜 | 各分类知名度排行 |

### 管理员模块
| 功能 | 描述 |
|------|------|
| 用户管理 | 查看、添加、编辑、删除用户 |
| 管理员管理 | 添加/删除管理员 |
| 帖子管理 | 查看、删除帖子 |
| 评论管理 | 查看、删除评论 |
| 系统通知 | 发送通知给全部/指定用户 |

---

## 🛠 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Data JPA | - | ORM框架 |
| Spring Security | 6.x | 安全框架 |
| Hibernate | - | JPA实现 |
| MySQL | 8.x | 数据库 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.x | 前端框架 |
| Vue Router | 4.x | 路由管理 |
| Axios | - | HTTP客户端 |
| Vite | 5.0 | 构建工具 |

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE bluebook CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改配置文件 `src/main/resources/application.properties`：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bluebook
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 后端启动

```bash
# 进入项目目录
cd bluebook

# 编译项目
mvn clean install

# 启动后端服务
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:3003` 启动

### 默认管理员账号

首次启动后，系统会自动创建默认管理员账号：
- 用户名：`admin`
- 密码：`admin123`

---

## 📁 项目结构

```
bluebook/
├── src/main/java/com/bluebook/
│   ├── BluebookApplication.java    # 主程序入口
│   ├── config/                      # 配置类
│   │   ├── CorsConfig.java         # CORS跨域配置
│   │   └── SecurityConfig.java     # Spring Security配置
│   ├── model/                       # 实体类
│   │   ├── User.java               # 用户实体
│   │   ├── Post.java               # 帖子实体
│   │   ├── Follow.java             # 关注关系实体
│   │   ├── Like.java               # 点赞实体
│   │   ├── Collect.java            # 收藏实体
│   │   ├── Dislike.java            # 踩实体
│   │   ├── Comment.java            # 评论实体
│   │   ├── Message.java            # 私信实体
│   │   └── Notification.java       # 通知实体
│   ├── repository/                  # 数据访问层
│   ├── service/                     # 业务逻辑层
│   ├── controller/                  # RESTful API控制器
│   └── utils/                       # 工具类
│
├── src/main/resources/
│   ├── application.properties       # 应用配置
│   └── static/                      # 静态资源
│       ├── avatars/                # 用户头像存储
│       └── uploads/                # 帖子图片存储
│
└── frontend/                        # Vue.js前端
    ├── src/
    │   ├── main.js                 # 应用入口
    │   ├── App.vue                 # 根组件
    │   ├── api/index.js            # API封装
    │   ├── router/index.js         # 路由配置
    │   ├── styles/main.css         # 全局样式
    │   └── views/                  # 页面组件
    │       ├── Home.vue            # 首页
    │       ├── Login.vue           # 登录页
    │       ├── Register.vue        # 注册页
    │       ├── Profile.vue         # 个人主页
    │       ├── Post.vue            # 帖子详情
    │       ├── Ranking.vue         # 排行榜
    │       ├── Notifications.vue   # 通知页
    │       ├── Messages.vue        # 私信页
    │       ├── Collections.vue     # 收藏页
    │       └── Admin.vue           # 管理员后台
    └── vite.config.js              # Vite配置
```

---

## 📡 API文档

### 用户模块 API

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/users/register` | POST | 用户注册 |
| `/api/users/login` | POST | 用户登录 |
| `/api/users/logout` | POST | 用户登出 |
| `/api/users/check-login` | GET | 检查登录状态 |
| `/api/users/info/{id}` | GET | 获取用户信息 |
| `/api/users/captcha` | GET | 获取图形验证码 |
| `/api/users/avatar` | POST | 上传头像 |

### 帖子模块 API

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/posts` | GET | 获取帖子列表 |
| `/api/posts` | POST | 创建帖子 |
| `/api/posts/{id}` | GET | 获取帖子详情 |
| `/api/posts/search` | GET | 搜索帖子 |
| `/api/posts/hot` | GET | 获取热门帖子 |
| `/api/posts/{id}/like` | POST/DELETE | 点赞/取消点赞 |
| `/api/posts/{id}/collect` | POST/DELETE | 收藏/取消收藏 |
| `/api/posts/{id}/dislike` | POST/DELETE | 踩/取消踩 |
| `/api/posts/{id}/comments` | POST | 添加评论 |

### 关注模块 API

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/follows/{id}` | POST/DELETE | 关注/取关用户 |
| `/api/follows/status/{id}` | GET | 获取关注状态 |
| `/api/follows/followers/{id}` | GET | 获取粉丝列表 |
| `/api/follows/following/{id}` | GET | 获取关注列表 |

### 消息模块 API

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/messages/send` | POST | 发送私信 |
| `/api/messages/conversations` | GET | 获取会话列表 |
| `/api/messages/conversation/{id}` | GET | 获取对话消息 |
| `/api/messages/unread-count` | GET | 获取未读消息数 |

### 排行榜模块 API

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/ranking/{type}` | GET | 获取排行榜 |
| `/api/ranking/category/{category}/{type}` | GET | 获取分类排行榜 |

### 管理员模块 API

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/admin/users` | GET/POST | 获取用户列表/创建用户 |
| `/api/admin/admins` | GET/POST | 获取管理员列表/创建管理员 |
| `/api/admin/posts` | GET | 获取帖子列表 |
| `/api/admin/comments` | GET | 获取评论列表 |
| `/api/admin/notification` | POST | 发送系统通知 |

---

## 📸 项目截图

### 首页
![首页](docs/screenshots/home.png)

### 登录页
![登录页](docs/screenshots/login.png)

### 个人主页
![个人主页](docs/screenshots/profile.png)

### 管理员后台
![管理员后台](docs/screenshots/admin.png)

---

## 🔒 安全特性

- 密码加密存储
- Session会话管理
- CSRF保护（可配置）
- 图形验证码验证
- 管理员权限验证
- 私信互关限制

---

## 📝 开发计划

- [ ] 添加Redis缓存支持
- [ ] 添加Elasticsearch全文搜索
- [ ] 添加消息推送（WebSocket）
- [ ] 添加文件上传到云存储
- [ ] 添加移动端适配
- [ ] 添加国际化支持

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 👥 作者

**Bluebook Team**

---

<div align="center">

如果这个项目对你有帮助，请给一个 ⭐️ Star 支持一下！

</div>
