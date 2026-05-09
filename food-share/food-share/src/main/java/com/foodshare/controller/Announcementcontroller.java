package com.foodshare.controller;

import com.foodshare.common.Result;
import com.foodshare.entity.Announcement;
import org.apache.ibatis.annotations.*;
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
 * 系统公告控制器
 * 管理员发布/管理公告，用户首页展示显示中的公告
 */
@RestController
@RequestMapping("/announcement")
@CrossOrigin
public class AnnouncementController {

    @Autowired
    private DataSource dataSource;

    /**
     * 获取所有显示中的公告（用户首页调用）
     * GET /announcement/list
     */
    @GetMapping("/list")
    public Result list() {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT id, title, content, create_time FROM announcement WHERE status = 1 ORDER BY create_time DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getLong("id"));
                row.put("title", rs.getString("title"));
                row.put("content", rs.getString("content"));
                row.put("createTime", rs.getString("create_time"));
                list.add(row);
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取公告失败：" + e.getMessage());
        }
    }

    /**
     * 管理员获取所有公告（含隐藏的）
     * GET /announcement/admin/list
     */
    @GetMapping("/admin/list")
    public Result adminList() {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT id, title, content, status, create_time FROM announcement ORDER BY create_time DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getLong("id"));
                row.put("title", rs.getString("title"));
                row.put("content", rs.getString("content"));
                row.put("status", rs.getInt("status"));
                row.put("createTime", rs.getString("create_time"));
                list.add(row);
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取公告失败：" + e.getMessage());
        }
    }

    /**
     * 管理员发布新公告
     * POST /announcement/add
     * 请求体: { title, content, adminId }
     */
    @PostMapping("/add")
    public Result add(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        Object adminIdObj = body.get("adminId");
        if (title == null || title.trim().isEmpty()) {
            return Result.error("公告标题不能为空");
        }
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO announcement (title, content, admin_id, status, create_time, update_time) VALUES (?, ?, ?, 1, NOW(), NOW())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, title.trim());
            ps.setString(2, content != null ? content.trim() : "");
            ps.setObject(3, adminIdObj);
            ps.executeUpdate();
            return Result.success(null, "公告发布成功");
        } catch (Exception e) {
            return Result.error("发布失败：" + e.getMessage());
        }
    }

    /**
     * 管理员切换公告显示/隐藏状态
     * POST /announcement/toggleStatus
     * 请求体: { id, status }
     */
    @PostMapping("/toggleStatus")
    public Result toggleStatus(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        try (Connection conn = dataSource.getConnection()) {
            String sql = "UPDATE announcement SET status = ?, update_time = NOW() WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
            return Result.success(null, status == 1 ? "已设为显示" : "已设为隐藏");
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 管理员修改公告标题和内容
     * POST /announcement/update
     * 请求体: { id, title, content }
     */
    @PostMapping("/update")
    public Result update(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String title = (String) body.get("title");
        String content = (String) body.getOrDefault("content", "");
        if (title == null || title.trim().isEmpty()) return Result.error("公告标题不能为空");
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE announcement SET title=?, content=?, update_time=NOW() WHERE id=?");
            ps.setString(1, title.trim());
            ps.setString(2, content.trim());
            ps.setLong(3, id);
            ps.executeUpdate();
            return Result.success(null, "修改成功");
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 管理员删除公告
     * DELETE /announcement/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM announcement WHERE id = ?");
            ps.setLong(1, id);
            ps.executeUpdate();
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}