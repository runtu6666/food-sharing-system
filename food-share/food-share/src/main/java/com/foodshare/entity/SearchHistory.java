package com.foodshare.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户搜索历史实体类
 * 对应数据库表: search_history
 */
@Data
public class SearchHistory {

    /** 主键ID */
    private Long id;

    /** 用户ID，关联user表 */
    private Long userId;

    /** 搜索关键词 */
    private String keyword;

    /** 搜索类型：1-笔记搜索 2-店铺搜索 */
    private Integer searchType;

    /** 搜索时间 */
    private Date createTime;
}
