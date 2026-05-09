# AI交接文档 —— 本地特色美食探店系统毕业设计修改

## 一、项目基本信息

- **项目名称：** 基于Vue的本地特色美食探店与分享系统的设计与实现
- **作者：** 姚嘉全，山东管理学院，计算机科学与技术1班
- **指导教师：** 王小婷
- **技术栈：** 前端Vue.js + Element Plus + Axios + 百度地图API，后端Spring Boot 3.x + JDK 17 + MyBatis，数据库MySQL 8.0.28
- **数据库名：** food_share，用户root，密码1234，端口3306

## 二、项目文件路径

- **后端代码：** `D:\projects\food-sharing-system\food-share\food-share\src\main\java\com\foodshare\`
- **后端配置：** `D:\projects\food-sharing-system\food-share\food-share\src\main\resources\application.properties`
- **Mapper XML：** `D:\projects\food-sharing-system\food-share\food-share\src\main\resources\mapper\`（目前有NoteMapper.xml和ShopMapper.xml）
- **前端代码：** `D:\projects\food-sharing-system\food-share-front\src\`
- **论文原文件：** 上传的docx文件，约3.3万字

## 三、后端代码结构

```
com.foodshare/
├── common/
│   └── Result.java              # 统一返回结果封装类
├── config/
│   └── WebConfig.java           # Web配置（CORS、拦截器注册等）
├── controller/
│   ├── AdminController.java     # 管理员后台（审核、封禁、统计）
│   ├── CategoryController.java  # 美食分类CRUD
│   ├── DishController.java      # 菜品管理
│   ├── FileUploadController.java# 文件上传（图片存本地磁盘）
│   ├── NoteController.java      # 笔记CRUD、点赞、收藏、评论（最大的控制器）
│   ├── SensitiveWordController.java # 敏感词管理+DFA热更新
│   ├── ShopController.java      # 店铺管理、LBS附近搜索、评价管理
│   └── UserController.java      # 登录、注册、用户信息修改
├── entity/
│   ├── Category.java
│   ├── Comment.java
│   ├── Dish.java               # 用了@Data lombok注解
│   ├── Note.java
│   ├── SensitiveWord.java
│   ├── Shop.java
│   ├── ShopReview.java
│   └── User.java               # 用了@Data lombok注解
├── interceptor/
│   └── JwtInterceptor.java      # JWT鉴权拦截器
├── mapper/
│   ├── CategoryMapper.java      # 纯注解SQL
│   ├── DishMapper.java          # 纯注解SQL
│   ├── NoteMapper.java          # 注解SQL + XML混合（复杂查询在NoteMapper.xml）
│   ├── SensitiveWordMapper.java # 纯注解SQL
│   ├── ShopMapper.java          # 注解SQL + XML混合（LBS查询在ShopMapper.xml）
│   └── UserMapper.java          # 纯注解SQL（含大量管理员操作）
├── service/
│   ├── DishService.java
│   └── UserService.java
├── utils/
│   ├── DFAFilterUtil.java       # DFA敏感词过滤工具类
│   └── JwtUtil.java             # JWT生成与验证工具类
└── FoodShareApplication.java    # 启动类
```

## 四、数据库现状（已完成扩展，共16张表）

### 原有10张表：
1. **user** - 用户信息表（id, username, password, nickname, avatar, role, status, create_time）
2. **shop** - 美食店铺表（id, user_id, name, cover, address, longitude, latitude, phone, business_hours, category_id, status, create_time, legal_name, shop_images, reject_reason）
3. **note** - 探店笔记表（id, user_id, shop_id, title, content, images, score, like_count, collect_count, category_id, status, reject_reason, create_time）
4. **dish** - 菜品信息表（id, shop_id, name, price, discount_price, image, description, status, create_time）
5. **shop_review** - 店铺评价表（id, shop_id, user_id, rating, content, merchant_reply, create_time）
6. **comment** - 笔记评论表（id, note_id, user_id, content, reply, create_time）
7. **category** - 美食分类表（id, name, sort）
8. **sensitive_word** - 敏感词库表（id, word, create_time）
9. **likes** - 点赞记录表（id, user_id, note_id, create_time）
10. **collect** - 收藏记录表（id, user_id, note_id, create_time）

### 新增6张表（已在MySQL中建好，但Java代码还没写）：
11. **merchant_info** - 商家资质信息表（id, user_id, legal_name, id_card, license_number, license_image, create_time, update_time）—— 从shop表拆出，user_id有唯一索引
12. **search_history** - 用户搜索历史表（id, user_id, keyword, search_type, create_time）—— search_type: 1笔记搜索 2店铺搜索
13. **hot_search** - 热搜词统计表（id, keyword, search_count, update_time）—— keyword有唯一索引
14. **login_log** - 登录日志表（id, user_id, login_ip, device, login_time, status）—— status: 1成功 0失败
15. **announcement** - 系统公告表（id, title, content, admin_id, status, create_time, update_time）—— status: 0隐藏 1显示
16. **notification** - 站内消息通知表（id, user_id, type, title, content, target_id, is_read, create_time）—— type: 1点赞 2评论 3审核通过 4审核驳回 5系统公告

### 重要提醒：
- note.images 和 shop.shop_images 字段存储的是 **base64编码的图片数据**（单条可达2MB），不是URL！不能做拆分迁移。
- shop.legal_name 字段暂时保留未删除，已迁移到 merchant_info 表中。
- merchant_info 表已有2条数据（从shop表迁移过来的）。

## 五、导师修改意见（核心问题）

1. **E-R图画的不对** —— 需要重画，确保用标准的矩形(实体)、菱形(联系)、椭圆(属性)表示法，标注1:N/M:N基数
2. **数据库表太少** —— 原来10张，要求至少16张 → 已解决，现在16张
3. **有的功能描述太复杂，不像本科生** —— 论文语言太"高级"，大量使用"底层""物理引擎""状态机流转""行级封禁""熔断拦截"等词汇，需要简化为朴实的本科论文风格
4. **缺少搜索、分类、登录等基础功能描述** —— 需要在第5章把这些功能作为独立小节描述
5. **字数太多** —— 目前约3.3万字（纯中文约2.6万），导师要求1.7-1.8万字，需要大幅精简

## 六、还需要完成的工作（按优先级排序）

### 优先级1：Java代码修改（让新表能跑起来）

#### 6.1 新增Entity类（在 entity/ 目录下）
需要新建以下文件：
- `SearchHistory.java` - 字段：id(Long), userId(Long), keyword(String), searchType(Integer), createTime(LocalDateTime)
- `HotSearch.java` - 字段：id(Long), keyword(String), searchCount(Integer), updateTime(LocalDateTime)
- `LoginLog.java` - 字段：id(Long), userId(Long), loginIp(String), device(String), loginTime(LocalDateTime), status(Integer)
- `MerchantInfo.java` - 字段：id(Long), userId(Long), legalName(String), idCard(String), licenseNumber(String), licenseImage(String), createTime(LocalDateTime), updateTime(LocalDateTime)
- `Announcement.java` - 字段：id(Long), title(String), content(String), adminId(Long), status(Integer), createTime(LocalDateTime), updateTime(LocalDateTime)
- `Notification.java` - 字段：id(Long), userId(Long), type(Integer), title(String), content(String), targetId(Long), isRead(Integer), createTime(LocalDateTime)

#### 6.2 新增Mapper接口（在 mapper/ 目录下）
至少需要：
- `SearchHistoryMapper.java` - 插入搜索记录、按用户查询历史、删除历史
- `HotSearchMapper.java` - 更新搜索次数（ON DUPLICATE KEY UPDATE）、查询热搜TOP10
- `LoginLogMapper.java` - 插入登录日志、按用户查询登录记录

#### 6.3 修改现有Controller（最小改动）

**UserController.java 的 login 方法：**
在登录成功后（生成JWT token之后），加几行代码：
```java
// 记录登录日志
LoginLog log = new LoginLog();
log.setUserId(dbUser.getId());
log.setLoginIp(request.getRemoteAddr());  // 需要在方法参数加 HttpServletRequest request
log.setDevice(request.getHeader("User-Agent"));
log.setStatus(1);
loginLogMapper.insert(log);
```

**NoteController.java 的 list 方法：**
在查询笔记列表时，如果有keyword参数，加几行记录搜索历史：
```java
if (keyword != null && !keyword.trim().isEmpty()) {
    // 记录搜索历史
    searchHistoryMapper.insert(userId, keyword.trim(), 1);
    // 更新热搜词
    hotSearchMapper.upsert(keyword.trim());
}
```

**ShopController.java 的 nearby 方法和 search 方法：**
同样在有keyword时记录搜索历史，search_type传2表示店铺搜索。

#### 6.4 可选但不紧急的代码
- MerchantInfo 的 Controller 和完整 CRUD（论文里提一下就行，不一定要全部实现）
- Announcement 的 Controller（同上）
- Notification 的 Controller（同上）

### 优先级2：论文修改

#### 需要修改的章节：
1. **第4章 4.3.1 概念结构设计** —— 重画E-R图，加入新的6个实体
2. **第4章 4.3.2 逻辑结构设计** —— 补充6张新表的表结构描述（每张表一小段+一个表格）
3. **第5章** —— 新增两个小节："搜索历史与热搜推荐功能"、"登录安全与日志管理"
4. **全文精简** —— 删除大量修饰词（"极其""绝对""彻底""底层""物理"等），目标降到1.7-1.8万字
5. **第6章** —— 测试表格的描述精简化

#### 精简策略：
- 第1章绪论：砍一半
- 第2章技术简介：每个技术3-5句话，控制在1500字以内
- 第5章实现：每个功能只保留"功能说明→关键实现思路→界面截图"三段式，每个功能300-500字
- 第6章测试：删除测试前的大段铺垫，预期结果/实际结果列简明扼要
- 所有数据库表的描述：一两句话说明用途即可，删除大段修饰

## 七、现有代码风格说明（给下一个AI参考）

- 部分Entity用了lombok的@Data注解（Dish.java, User.java），其余手写getter/setter
- Mapper接口大部分用@Select/@Insert/@Update注解写SQL，复杂查询用XML
- Controller直接注入Mapper，部分有Service层（DishService, UserService），大部分没有Service层直接Controller→Mapper
- 所有Controller都加了@CrossOrigin注解
- 统一返回Result.java封装类
- MyBatis配置了map-underscore-to-camel-case=true（下划线自动转驼峰）
- 敏感词过滤用DFAFilterUtil，在NoteController的publish和update方法中调用
- 封禁检查用isBanned(userId)私有方法，在NoteController和ShopController中都有

## 八、时间约束

距离论文提交仅剩约4小时，优先保证：
1. Java代码能编译通过（新增Entity和Mapper）
2. 搜索历史和登录日志的核心代码改完（改3个Controller）
3. 论文第4章补充新表结构
4. 论文全文精简字数
