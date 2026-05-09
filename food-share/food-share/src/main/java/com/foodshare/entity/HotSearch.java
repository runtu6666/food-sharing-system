package com.foodshare.entity;

import lombok.Data;
import java.util.Date;

/**
 * 热搜词统计实体类
 * 对应数据库表: hot_search
 */
@Data
public class HotSearch {

    /** 主键ID */
    private Long id;

    /** 热搜关键词（唯一索引） */
    private String keyword;

    /** 搜索次数 */
    private Integer searchCount;

    /** 最后更新时间 */
    private Date updateTime;
}