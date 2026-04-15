package com.foodshare.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 店铺实体类
 * 对应数据库表: shop
 * 作用: 封装店铺的所有信息,包括基本信息、地理位置、审核状态等
 */
public class Shop {

    /**
     * 店铺ID - 主键,自增
     */
    private Long id;

    /**
     * 商家用户ID - 外键关联user表
     * 说明: 一个商家(user_id)可以管理多个店铺
     */
    private Long userId;

    /**
     * 店铺名称
     * 示例: "老李烧烤", "甜蜜时光蛋糕店"
     */
    private String name;

    /**
     * 店铺封面图片URL
     * 示例: "http://xxx.com/shop/cover.jpg"
     */
    private String cover;

    /**
     * 店铺地址
     * 示例: "北京市朝阳区三里屯路19号"
     */
    private String address;

    /**
     * 经度 - 用于百度地图定位
     * 取值范围: -180 ~ 180
     * 精度: 小数点后7位(精确到厘米级)
     */
    private BigDecimal longitude;

    /**
     * 纬度 - 用于百度地图定位
     * 取值范围: -90 ~ 90
     * 精度: 小数点后7位(精确到厘米级)
     */
    private BigDecimal latitude;

    /**
     * 联系电话
     * 示例: "010-12345678", "13800138000"
     */
    private String phone;

    /**
     * 营业时间
     * 示例: "周一至周日 10:00-22:00", "每天 9:00-21:00"
     */
    private String businessHours;

    /**
     * 美食分类ID - 外键关联category表
     * 示例: 1-火锅, 2-烧烤, 3-甜点
     */
    private Integer categoryId;

    /**
     * 店铺状态
     * 0 - 待审核(商家刚提交入驻申请)
     * 1 - 审核通过(正常营业,用户可见)
     * 2 - 审核拒绝(商家需要重新提交)
     * 3 - 已封禁(违规店铺,管理员封禁)
     */
    private Integer status;

    /**
     * 法人姓名
     * 用于商家入驻资质审核
     */
    private String legalName;

    /**
     * 店铺实景图片(多张)
     * 存储格式: JSON数组或逗号分隔的URL列表
     * 示例: "url1,url2,url3" 或 ["url1","url2","url3"]
     */
    private String shopImages;

    /**
     * 驳回原因 - 用于记录审核不通过的具体理由
     * 对应数据库字段: reject_reason
     */
    private String rejectReason;

    /**
     * 创建时间 - 记录店铺入驻申请时间
     */
    private LocalDateTime createTime;

    // ==================== 关联查询字段(不存数据库) ====================

    /**
     * 分类名称 - 从category表关联查询
     * 用途: 前端展示时直接显示"火锅"而不是显示categoryId=1
     */
    private String categoryName;

    /**
     * 商家用户名 - 从user表关联查询
     * 用途: 管理后台显示"店铺所属商家"
     */
    private String username;

    /**
     * 当执行 LBS 搜索时，MySQL 的 ST_Distance_Sphere 函数会计算出距离（米），
     * 自动映射到这个字段上，方便直接返回给前端 Vue 页面渲染距离信息。
     */
    private Double distance;

    // ==================== 构造方法 ====================

    /**
     * 无参构造方法
     * MyBatis查询时需要通过无参构造创建对象,然后通过setter注入属性
     */
    public Shop() {
    }

    /**
     * 常用字段构造方法
     * 用途: 快速创建店铺对象,用于插入数据
     */
    public Shop(Long userId, String name, String address) {
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.status = 0; // 默认待审核
    }

    // ==================== Getter和Setter方法 ====================
    // MyBatis通过反射调用这些方法来设置/获取属性值

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
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

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getShopImages() {
        return shopImages;
    }

    public void setShopImages(String shopImages) {
        this.shopImages = shopImages;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getDistance() {
        return distance;
    }

    // 获取驳回原因
    public String getRejectReason() {
        return rejectReason;
    }

    // 设置驳回原因
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    // ==================== toString方法 ====================
    // 用于调试时打印对象信息

    @Override
    public String toString() {
        return "Shop{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", status=" + status +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
