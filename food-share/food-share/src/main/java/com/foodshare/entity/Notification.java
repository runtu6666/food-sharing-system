package com.foodshare.entity;

import lombok.Data;
import java.util.Date;

/**
 * 站内消息通知实体类
 * 对应数据库表: notification
 * 用于点赞、评论、审核结果、系统公告等消息推送
 */
@Data
public class Notification {

    /** 主键ID */
    private Long id;

    /** 接收消息的用户ID */
    private Long userId;

    /** 消息类型：1-点赞 2-评论 3-审核通过 4-审核驳回 5-系统公告 */
    private Integer type;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 关联目标ID（如笔记ID、店铺ID等） */
    private Long targetId;

    /** 是否已读：0-未读 1-已读 */
    private Integer isRead;

    /** 消息创建时间 */
    private Date createTime;
}