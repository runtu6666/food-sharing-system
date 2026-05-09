package com.foodshare.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 登录日志实体类
 * 对应数据库表: login_log
 */
@Data
public class LoginLog {

    /** 主键ID */
    private Long id;

    /** 用户ID，关联user表 */
    private Long userId;

    /** 登录IP地址 */
    private String loginIp;

    /** 登录设备信息（User-Agent） */
    private String device;

    /** 登录时间，格式化为 yyyy-MM-dd HH:mm:ss，使用东八区时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date loginTime;

    /** 登录状态：1-成功 0-失败 */
    private Integer status;
}