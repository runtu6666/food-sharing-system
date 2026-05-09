package com.foodshare.entity;

import lombok.Data;
import java.util.Date;

/**
 * 系统公告实体类
 * 对应数据库表: announcement
 */
@Data
public class Announcement {

    /** 主键ID */
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 发布管理员的用户ID */
    private Long adminId;

    /** 公告状态：0-隐藏 1-显示 */
    private Integer status;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
