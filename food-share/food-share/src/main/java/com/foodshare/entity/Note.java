package com.foodshare.entity;

import java.time.LocalDateTime;

/**
 * 探店笔记实体类
 * 对应数据库表: note
 * 作用: 封装用户发布的探店笔记信息,是平台的核心内容载体
 */
public class Note {

    /**
     * 笔记ID - 主键,自增
     */
    private Long id;

    /**
     * 发布用户ID - 外键关联user表
     * 说明: 记录是哪个用户发布的这篇笔记
     */
    private Long userId;

    /**
     * 关联店铺ID - 外键关联shop表
     * 说明: 笔记可以关联一个店铺,也可以不关联(纯美食分享)
     * 允许为NULL
     */
    private Long shopId;

    /**
     * 笔记标题
     * 示例: "三里屯必吃烧烤!", "这家甜品店太治愈了"
     */
    private String title;

    /**
     * 笔记内容(正文)
     * 类型: TEXT,支持长文本
     * 示例: 详细的探店体验、菜品描述、环境评价等
     */
    private String content;

    /**
     * 笔记配图(多张)
     * 存储格式: JSON数组或逗号分隔的URL列表
     * 示例: "url1,url2,url3" 或 ["url1","url2","url3"]
     * 类型: LONGTEXT,支持存储多张图片的base64或URL
     */
    private String images;

    /**
     * 用户评分
     * 取值范围: 1-5分(整数)
     * 默认值: 5分
     * 用途: 用户对店铺或美食的评分
     */
    private Integer score;

    /**
     * 点赞数
     * 说明: 冗余字段,用于快速查询热门笔记
     * 更新时机: 用户点赞/取消点赞时同步更新
     */
    private Integer likeCount;

    /**
     * 收藏数
     * 说明: 冗余字段,用于快速查询热门笔记
     * 更新时机: 用户收藏/取消收藏时同步更新
     */
    private Integer collectCount;

    /**
     * 美食分类ID - 外键关联category表
     * 示例: 1-火锅, 2-烧烤, 3-甜点
     * 用途: 用户浏览时可以按分类筛选笔记
     */
    private Integer categoryId;

    /**
     * 笔记状态
     * 0 - 待审核(用户刚发布,等待管理员审核)
     * 1 - 审核通过(正常显示,其他用户可见)
     * 2 - 审核拒绝(不显示,用户可看到拒绝理由)
     * 3 - 已屏蔽(违规内容,管理员屏蔽)
     */
    private Integer status;

    /**
     * 驳回理由
     * 说明: 当status=2(审核拒绝)时,管理员填写的拒绝原因
     * 示例: "内容包含敏感词", "图片不清晰", "涉嫌广告"
     * 允许为NULL
     */
    private String rejectReason;

    /**
     * 创建时间 - 笔记发布时间
     */
    private LocalDateTime createTime;

    // ==================== 关联查询字段(不存数据库) ====================

    /**
     * 发布用户昵称 - 从user表关联查询
     * 用途: 前端展示"作者:张三"
     */
    private String nickname;

    /**
     * 发布用户头像 - 从user表关联查询
     * 用途: 前端展示作者头像
     */
    private String avatar;

    /**
     * 关联店铺名称 - 从shop表关联查询
     * 用途: 前端展示"探店:老李烧烤"
     */
    private String shopName;

    /**
     * 分类名称 - 从category表关联查询
     * 用途: 前端展示"分类:火锅"
     */
    private String categoryName;

    /**
     * 当前用户是否已点赞
     * 用途: 前端显示点赞按钮的高亮状态
     * 查询逻辑: LEFT JOIN likes表判断当前用户ID是否存在
     */
    private Boolean isLiked;

    /**
     * 当前用户是否已收藏
     * 用途: 前端显示收藏按钮的高亮状态
     * 查询逻辑: LEFT JOIN collect表判断当前用户ID是否存在
     */
    private Boolean isCollected;

    // ==================== 构造方法 ====================

    /**
     * 无参构造方法
     * MyBatis查询时需要
     */
    public Note() {
    }

    /**
     * 常用字段构造方法
     * 用途: 快速创建笔记对象
     */
    public Note(Long userId, String title, String content) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.score = 5; // 默认5分
        this.likeCount = 0;
        this.collectCount = 0;
        this.status = 0; // 默认待审核
    }

    // ==================== Getter和Setter方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }

    public Boolean getIsCollected() {
        return isCollected;
    }

    public void setIsCollected(Boolean isCollected) {
        this.isCollected = isCollected;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", likeCount=" + likeCount +
                ", collectCount=" + collectCount +
                '}';
    }
}
