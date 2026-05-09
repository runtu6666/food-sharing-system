package com.foodshare.controller;

import com.foodshare.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内消息通知控制器
 * 用户查看自己的消息通知（点赞、评论、审核结果、系统公告）
 */
@RestController
@RequestMapping("/notification")
@CrossOrigin
public class NotificationController {

    @Autowired
    private DataSource dataSource;

    /**
     * 获取当前用户的所有通知
     * GET /notification/list?userId=1
     */
    @GetMapping("/list")
    public Result list(@RequestParam Long userId) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT id, type, title, content, target_id, is_read, create_time " +
                    "FROM notification WHERE user_id = ? ORDER BY create_time DESC LIMIT 50";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getLong("id"));
                row.put("type", rs.getInt("type"));
                row.put("title", rs.getString("title"));
                row.put("content", rs.getString("content"));
                row.put("targetId", rs.getLong("target_id"));
                row.put("isRead", rs.getInt("is_read"));
                row.put("createTime", rs.getString("create_time"));
                list.add(row);
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取通知失败：" + e.getMessage());
        }
    }

    /**
     * 查询未读消息数量
     * GET /notification/unreadCount?userId=1
     */
    @GetMapping("/unreadCount")
    public Result unreadCount(@RequestParam Long userId) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = 0");
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 标记单条通知为已读
     * POST /notification/read/{id}
     */
    @PostMapping("/read/{id}")
    public Result markRead(@PathVariable Long id) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE notification SET is_read = 1 WHERE id = ?");
            ps.setLong(1, id);
            ps.executeUpdate();
            return Result.success(null, "已标记已读");
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 全部标记为已读
     * POST /notification/readAll?userId=1
     */
    @PostMapping("/readAll")
    public Result markAllRead(@RequestParam Long userId) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE notification SET is_read = 1 WHERE user_id = ? AND is_read = 0");
            ps.setLong(1, userId);
            ps.executeUpdate();
            return Result.success(null, "全部已读");
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }
}
