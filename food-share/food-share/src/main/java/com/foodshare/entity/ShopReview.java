package com.foodshare.entity;

import java.time.LocalDateTime;

/**
 * 店铺评价实体类
 */
public class ShopReview {
    private Long id;
    private Long shopId;        // 店铺ID
    private Long userId;        // 评价用户ID
    private Integer rating;     // 评分1-5
    private String content;     // 评价内容
    private String merchantReply; // 商家回复
    private LocalDateTime createTime;

    // 关联查询字段(不存数据库)
    private String username;    // 评价用户昵称
    private String avatar;      // 评价用户头像

    // 无参构造
    public ShopReview() {}

    // 全参构造
    public ShopReview(Long shopId, Long userId, Integer rating, String content) {
        this.shopId = shopId;
        this.userId = userId;
        this.rating = rating;
        this.content = content;
    }

    // Getter和Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMerchantReply() {
        return merchantReply;
    }

    public void setMerchantReply(String merchantReply) {
        this.merchantReply = merchantReply;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
