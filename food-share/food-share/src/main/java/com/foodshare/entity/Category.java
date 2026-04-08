package com.foodshare.entity;

/**
 * 美食分类实体类
 * 对应数据库表: category
 * 作用: 封装美食分类信息,用于笔记和店铺的分类管理
 */
public class Category {

    /**
     * 分类ID - 主键,自增
     */
    private Integer id;

    /**
     * 分类名称
     * 示例: "火锅", "烧烤", "甜点", "川菜"
     * 唯一性: 应该设置UNIQUE约束,避免重复分类
     */
    private String name;

    /**
     * 排序序号
     * 说明: 数字越小越靠前
     * 用途: 前端展示分类列表时按此字段排序
     * 示例: 火锅(1) > 烧烤(2) > 甜点(3)
     */
    private Integer sort;

    // ==================== 构造方法 ====================

    /**
     * 无参构造方法
     * MyBatis查询时需要
     */
    public Category() {
    }

    /**
     * 带参数构造方法
     * 用途: 快速创建分类对象
     */
    public Category(String name, Integer sort) {
        this.name = name;
        this.sort = sort;
    }

    // ==================== Getter和Setter方法 ====================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sort=" + sort +
                '}';
    }
}
