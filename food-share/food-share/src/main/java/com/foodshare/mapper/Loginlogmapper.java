package com.foodshare.mapper;

import com.foodshare.entity.LoginLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 登录日志Mapper接口
 * 负责用户登录记录的数据库操作
 */
@Mapper
public interface LoginLogMapper {

    /**
     * 插入一条登录日志
     * @param log 登录日志实体
     */
    @Insert("INSERT INTO login_log (user_id, login_ip, device, login_time, status) " +
            "VALUES (#{userId}, #{loginIp}, #{device}, NOW(), #{status})")
    void insert(LoginLog log);

    /**
     * 查询某用户的登录记录（最近20条）
     * @param userId 用户ID
     * @return 登录记录列表
     */
    @Select("SELECT * FROM login_log WHERE user_id = #{userId} ORDER BY login_time DESC LIMIT 20")
    List<LoginLog> findByUserId(@Param("userId") Long userId);
}
