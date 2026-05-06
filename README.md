# 基于 Vue 的本地特色美食探店与分享系统

> 一款集探店笔记发布、LBS 附近店铺发现、商家入驻管理与内容审核于一体的本地美食社区平台。

## 项目简介

本系统是一个面向本地美食爱好者的探店分享平台，采用 **Spring Boot + Vue 3** 前后端分离架构。用户可以发布图文探店笔记、点赞收藏互动、发现附近美食店铺；商家可入驻管理店铺信息；管理员通过后台对内容和用户进行审核管理。系统核心亮点包括基于 **DFA 算法的敏感词过滤**、**百度地图 LBS 定位**、**RBAC 角色权限控制**等。

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.0 | 核心框架 |
| MyBatis | 3.0.3 | ORM 持久层框架 |
| MySQL | 8.x | 关系型数据库 |
| JWT (jjwt) | 0.11.5 | 用户认证与令牌管理 |
| Lombok | - | 简化实体类开发 |
| Java | 17 | 开发语言 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 渐进式前端框架 |
| Vue Router | 4.x | 路由管理 |
| Vuex | 4.x | 状态管理 |
| Element Plus | 2.13.x | UI 组件库 |
| Axios | 1.13.x | HTTP 请求库 |
| 百度地图 API | - | LBS 定位与地图展示 |

## 系统架构

```
┌──────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐│
│  │ 登录页   │ │ 首页     │ │ 发布页   │ │ 管理后台/商家中心││
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘│
└─────────────────────────────┬────────────────────────────────┘
                              │ HTTP / RESTful API
┌─────────────────────────────┴────────────────────────────────┐
│                     后端 (Spring Boot)                        │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    Controller 层                         │ │
│  │  UserController / NoteController / ShopController /     │ │
│  │  AdminController / CategoryController / FileUpload      │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    Service 层                            │ │
│  │  UserService / DishService / DFAFilterUtil / JwtUtil    │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    Mapper 层 (MyBatis)                   │ │
│  │  UserMapper / NoteMapper / ShopMapper / CategoryMapper  │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────┬────────────────────────────────┘
                              │ JDBC
                    ┌─────────┴─────────┐
                    │   MySQL (food_share)  │
                    └───────────────────┘
```

## 项目结构

```
food-sharing-system/
├── food-share/                    # 后端项目
│   └── food-share/
│       ├── src/main/java/com/foodshare/
│       │   ├── common/            # 公共类 (Result 统一响应)
│       │   ├── config/            # 配置类 (跨域、WebConfig)
│       │   ├── controller/        # 控制器层
│       │   │   ├── AdminController.java      # 管理员后台
│       │   │   ├── CategoryController.java   # 美食分类
│       │   │   ├── DishController.java       # 菜品管理
│       │   │   ├── FileUploadController.java # 文件上传
│       │   │   ├── NoteController.java       # 探店笔记
│       │   │   ├── SensitiveWordController.java # 敏感词管理
│       │   │   ├── ShopController.java       # 店铺管理
│       │   │   └── UserController.java       # 用户认证
│       │   ├── entity/            # 实体类
│       │   │   ├── User.java      # 用户
│       │   │   ├── Note.java      # 探店笔记
│       │   │   ├── Shop.java      # 店铺
│       │   │   ├── Comment.java   # 评论
│       │   │   ├── Category.java  # 美食分类
│       │   │   ├── Dish.java      # 菜品
│       │   │   ├── ShopReview.java # 店铺评价
│       │   │   └── SensitiveWord.java # 敏感词
│       │   ├── interceptor/       # 拦截器 (JWT 鉴权)
│       │   ├── mapper/            # MyBatis Mapper 接口
│       │   ├── service/           # 业务逻辑层
│       │   ├── utils/             # 工具类
│       │   │   ├── JwtUtil.java   # JWT 令牌工具
│       │   │   └── DFAFilterUtil.java # DFA 敏感词过滤
│       │   └── FoodShareApplication.java # 启动类
│       └── src/main/resources/
│           ├── application.properties # 配置文件
│           └── mapper/            # MyBatis XML 映射文件
│
├── food-share-front/              # 前端项目
│   ├── src/
│   │   ├── views/                 # 页面组件
│   │   │   ├── LoginView.vue      # 登录页
│   │   │   ├── HomeView.vue       # 首页 (笔记浏览 + LBS 搜索)
│   │   │   ├── PublishView.vue    # 发布笔记
│   │   │   ├── NoteDetailView.vue # 笔记详情
│   │   │   ├── ProfileView.vue    # 个人中心
│   │   │   ├── AdminView.vue      # 管理员后台
│   │   │   ├── MerchantView.vue   # 商家中心
│   │   │   └── ShopDetailView.vue # 店铺详情
│   │   ├── router/index.js        # 路由配置 + 权限守卫
│   │   ├── store/                 # Vuex 状态管理
│   │   ├── components/            # 公共组件
│   │   ├── assets/                # 静态资源
│   │   └── main.js                # 入口文件
│   └── vue.config.js              # Vue CLI 配置
│
└── README.md
```

## 核心功能

### 用户端

- **注册与登录**：基于 JWT 的用户认证，支持多角色（普通用户、商家、管理员）
- **探店笔记**：发布图文笔记，支持关联店铺、评分、分类标签
- **互动功能**：点赞、收藏、评论（支持多层回复）
- **LBS 附近店铺**：基于百度地图定位，搜索附近美食店铺，支持按分类和关键词筛选
- **内容搜索**：按关键词搜索笔记和店铺
- **个人中心**：查看我的笔记、收藏、评论

### 商家端

- **店铺入驻**：提交店铺信息（名称、地址、经纬度、营业时间、实景图片等）
- **店铺管理**：编辑店铺资料、查看关联笔记
- **评价管理**：查看用户评价

### 管理员端

- **数据概览**：用户数、店铺数、笔记数、待审核数统计
- **内容审核**：审核笔记（通过/驳回），支持填写驳回理由
- **用户管理**：查看用户列表、封禁/解封用户、删除用户
- **店铺审核**：审核商家入驻申请
- **敏感词管理**：基于 DFA 算法的敏感词库管理，支持热更新

## 核心技术亮点

### 1. DFA 敏感词过滤

采用 **确定有穷自动机 (DFA)** 算法实现高效的敏感词过滤：

- **O(N) 时间复杂度**：匹配速度仅与文本长度相关，不受词库大小影响
- **热更新机制**：管理员增删敏感词后，内存中的 Trie 树自动重建，无需重启服务
- **并发安全**：采用局部变量构建新字典树，构建过程不影响前台用户正常使用

### 2. LBS 地理位置服务

集成百度地图 API，实现基于地理位置的店铺发现：

- 前端通过百度地图 SDK 获取用户当前坐标
- 后端使用 MySQL 的 `ST_Distance_Sphere` 函数计算球面距离
- 支持按距离排序、按半径筛选、按分类和关键词联合查询

### 3. RBAC 权限控制

三层权限校验机制：

- **前端路由守卫**：`router.beforeEach` 拦截非法页面访问
- **后端拦截器**：`JwtInterceptor` 验证 API 请求的 JWT 令牌
- **数据权限**：不同角色（user / shop / admin）拥有不同的数据访问范围

### 4. JWT 无状态认证

- 登录成功后生成 JWT 令牌，包含用户 ID、用户名、角色信息
- 前端将 Token 存储在 localStorage，每次请求通过 Axios 拦截器自动携带
- 后端通过拦截器统一校验 Token 有效性

## 数据库设计

### 核心表结构

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `user` | 用户表 | id, username, password, nickname, avatar, role, status |
| `note` | 探店笔记 | id, user_id, shop_id, title, content, images, score, status |
| `shop` | 店铺表 | id, user_id, name, address, longitude, latitude, status |
| `comment` | 评论表 | id, note_id, user_id, content, reply |
| `category` | 美食分类 | id, name, icon |
| `sensitive_word` | 敏感词库 | id, word |

### 角色说明

| 角色 | 标识 | 权限 |
|------|------|------|
| 普通用户 | `user` | 浏览笔记、发布笔记、点赞收藏评论 |
| 商家 | `shop` | 管理店铺、查看评价 |
| 管理员 | `admin` | 内容审核、用户管理、敏感词管理、数据统计 |

## 快速开始

### 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 后端启动

```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE food_share DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 2. 导入数据表（根据项目中的 SQL 文件或实体类自行建表）

# 3. 修改数据库配置
# 编辑 food-share/food-share/src/main/resources/application.properties
# 修改数据库连接信息：
#   spring.datasource.url=jdbc:mysql://localhost:3306/food_share?...
#   spring.datasource.username=root
#   spring.datasource.password=你的密码

# 4. 启动后端
cd food-share/food-share
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 前端启动

```bash
# 1. 安装依赖
cd food-share-front
npm install

# 2. 启动开发服务器
npm run serve
```

前端默认运行在 `http://localhost:8081`

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 普通用户 | 根据数据库自行查看 | - |

## API 接口文档

### 用户模块 `/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/login` | 用户登录 |
| POST | `/user/register` | 用户注册 |

### 笔记模块 `/note`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/note/list` | 获取笔记列表（支持分类筛选、搜索、排序、分页） |
| GET | `/note/detail/{id}` | 获取笔记详情 |
| POST | `/note/publish` | 发布笔记 |
| POST | `/note/like` | 点赞/取消点赞 |
| POST | `/note/collect` | 收藏/取消收藏 |
| POST | `/note/comment` | 发表评论 |
| GET | `/note/comments/{id}` | 获取笔记评论列表 |

### 店铺模块 `/shop`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/shop/list` | 获取已审核店铺列表 |
| GET | `/shop/detail?id=` | 获取店铺详情 |
| GET | `/shop/search?keyword=` | 搜索店铺 |
| GET | `/shop/nearby?lng=&lat=&radius=` | LBS 附近店铺查询 |

### 管理员模块 `/admin`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/stats` | 数据概览 |
| GET | `/admin/notes/pending` | 待审核笔记列表 |
| POST | `/admin/notes/audit` | 审核笔记（通过/驳回） |
| GET | `/admin/users` | 用户列表 |
| POST | `/admin/users/status` | 封禁/解封用户 |

### 分类模块 `/category`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/category/list` | 获取所有美食分类 |

### 文件上传 `/file`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/file/upload` | 上传图片（最大 20MB） |

## 页面展示

### 用户角色

| 页面 | 路径 | 功能 |
|------|------|------|
| 登录页 | `/login` | 用户登录/注册 |
| 首页 | `/home` | 笔记瀑布流浏览、分类筛选、LBS 附近店铺搜索 |
| 发布页 | `/publish` | 发布探店笔记（图文） |
| 笔记详情 | `/note/:id` | 查看笔记、点赞、收藏、评论 |
| 个人中心 | `/profile` | 我的笔记、我的收藏、收到的评论 |

### 商家角色

| 页面 | 路径 | 功能 |
|------|------|------|
| 商家中心 | `/merchant` | 店铺管理、入驻申请 |
| 店铺详情 | `/shop/:id` | 店铺信息、关联笔记 |

### 管理员角色

| 页面 | 路径 | 功能 |
|------|------|------|
| 管理后台 | `/admin` | 数据统计、笔记审核、用户管理 |

## 配置说明

### application.properties

```properties
# 服务端口
server.port=8080

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/food_share?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=1234

# MyBatis 配置
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.configuration.map-underscore-to-camel-case=true

# 文件上传限制
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=50MB
```

## 项目特点总结

1. **前后端分离架构**：Spring Boot 提供 RESTful API，Vue 3 构建交互界面，职责清晰
2. **RBAC 权限模型**：三级角色权限控制，前端路由守卫 + 后端拦截器双重校验
3. **DFA 敏感词过滤**：O(N) 时间复杂度，支持数据库热更新，无需重启服务
4. **LBS 地理位置服务**：集成百度地图，基于球面距离计算实现附近店铺发现
5. **JWT 无状态认证**：安全高效的用户认证方案，支持分布式部署
6. **内容审核机制**：笔记发布后需管理员审核，保障平台内容质量

## 开发环境

- **操作系统**：Windows 11
- **后端 IDE**：IntelliJ IDEA
- **前端 IDE**：Visual Studio Code
- **数据库工具**：Navicat / MySQL Workbench
- **接口测试**：Postman / Apifox

## License

MIT License

## 作者

**姚嘉全** - [GitHub](https://github.com/runtu6666)

山东管理学院 · 2026 届本科毕业设计
