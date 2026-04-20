package com.foodshare.controller;

import com.foodshare.common.Result;
import com.foodshare.mapper.UserMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.foodshare.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.foodshare.mapper.ShopMapper shopMapper;

    // 数据概览
    @GetMapping("/stats")
    public Result stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.countUsers());
        data.put("shopCount", userMapper.countShops());
        data.put("noteCount", userMapper.countNotes());
        data.put("pendingCount", userMapper.countPendingNotes());
        return Result.success(data);
    }

    // 待审核笔记列表
    @GetMapping("/notes/pending")
    public Result pendingNotes() {
        return Result.success(userMapper.findPendingNotes());
    }

    // 审核笔记
    @PostMapping("/notes/audit")
    public Result auditNote(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String reason = params.getOrDefault("rejectReason", "").toString();
        userMapper.updateNoteStatus(id, status, reason);
        return Result.success("操作成功");
    }

    // 用户列表
    @GetMapping("/users")
    public Result users(@RequestParam(required = false) String keyword) {
        return Result.success(userMapper.findAllUsers(keyword));
    }

    // 封禁/解封用户
    @PostMapping("/users/status")
    public Result userStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        userMapper.updateUserStatus(id, status);
        return Result.success("操作成功");
    }

    // 删除用户（管理员操作）：会同时清理该用户相关的笔记、评论、点赞、收藏、店铺数据
    @PostMapping("/users/delete")
    public Result deleteUser(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        // 防止误删管理员账号（传入null获取全量列表进行校验）
        List<User> users = userMapper.findAllUsers(null);
        User target = null;
        for (User user : users) {
            if (user.getId().equals(id)) {
                target = user;
                break;
            }
        }
        if (target != null && "admin".equals(target.getRole())) {
            return Result.error("管理员账号不允许删除");
        }
        userMapper.deleteLikesByAuthorId(id);
        userMapper.deleteCollectsByAuthorId(id);
        userMapper.deleteCommentsByAuthorId(id);
        userMapper.deleteLikesByUserId(id);
        userMapper.deleteCollectsByUserId(id);
        userMapper.deleteCommentsByUserId(id);
        userMapper.deleteNotesByUserId(id);
        userMapper.deleteShopsByUserId(id);
        userMapper.deleteUserById(id);
        return Result.success("删除成功");
    }

    // 待审核商家
    @GetMapping("/shops/pending")
    public Result pendingShops() {
        return Result.success(userMapper.findPendingShops());
    }

    // 审批商家
    @PostMapping("/shops/approve")
    public Result approveShop(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        userMapper.updateShopStatus(id, 1, null);
        return Result.success("操作成功");
    }

    // 拒绝商家申请：将店铺状态改为-1（拒绝），并记录驳回原因
    @PostMapping("/shops/reject")
    public Result rejectShop(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        // 安全获取前端传来的驳回原因
        String rejectReason = params.get("rejectReason") != null ? params.get("rejectReason").toString() : "资料不符，请重新核对";

        // 调用刚才新建的带原因的更新方法
        userMapper.updateShopStatus(id, -1, rejectReason);
        return Result.success("已拒绝");
    }

    // 查询所有已审批通过的商家列表
    @GetMapping("/shops/all")
    public Result allShops(@RequestParam(required = false) String keyword) {
        return Result.success(userMapper.findAllShops(keyword));
    }

    // 封禁/解封商家
    @PostMapping("/shops/ban")
    public Result banShop(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());

        // 安全获取前端传来的封禁原因，如果是解封则默认为空字符串
        String rejectReason = params.get("rejectReason") != null ? params.get("rejectReason").toString() : "";

        // 更新店铺状态并同步记录原因
        userMapper.updateShopStatus(id, status, rejectReason);

        return Result.success("操作成功");
    }

    // 管理员屏蔽/解除屏蔽笔记
    @PostMapping("/note/ban")
    public Result banNote(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        userMapper.adminUpdateNoteStatus(id, status);
        return Result.success("操作成功");
    }

    // 管理员强制删除违规店铺
    @PostMapping("/shops/delete")
    public Result adminDeleteShop(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        // 解除该店铺相关的探店笔记绑定关系
        shopMapper.clearShopBinding(id);
        // 调用统一的清理方法，移除店铺下的所有相关评价数据
        shopMapper.deleteReviewsByShopId(id);
        // 执行店铺物理删除
        userMapper.deleteShopsByUserId(id); // 此处可优化为专门根据 shopId 删除的 mapper 方法
        return Result.success("店铺及相关评价已清理完毕");
    }
}
