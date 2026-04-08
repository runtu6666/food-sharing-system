package com.foodshare.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 菜品实体类
 * 对应数据库表: dish
 */
@Data
public class Dish {

    /**
     * 菜品主键ID
     */
    private Integer id;

    /**
     * 关联的店铺ID，用于区分该菜品属于哪个商家
     */
    private Integer shopId;

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 菜品价格（使用 BigDecimal 保证金额计算的精度，避免精度丢失）
     */
    private BigDecimal price;

    /**
     * 菜品特价/优惠价（非必填，有值代表参与打折）
     */
    private BigDecimal discountPrice;

    /**
     * 菜品图片的外链URL
     */
    private String image;

    /**
     * 菜品的详细描述或特色介绍
     */
    private String description;

    /**
     * 菜品售卖状态：1 代表上架可售，0 代表下架停售
     */
    private Integer status;

    /**
     * 菜品记录的创建时间
     */
    private Date createTime;
}
