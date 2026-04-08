package com.foodshare.controller;

import com.foodshare.common.Result;
import com.foodshare.entity.User;
import com.foodshare.mapper.UserMapper;
import com.foodshare.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 职责: 处理用户登录、注册等业务逻辑
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     *
     * 接口地址: POST /user/login
     * 请求参数:
     * {
     *   "username": "admin",
     *   "password": "123456"
     * }
     *
     * 返回数据:
     * {
     *   "code": 200,
     *   "message": "登录成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "userInfo": {
     *       "id": 1,
     *       "username": "admin",
     *       "nickname": "管理员",
     *       "role": "admin"
     *     }
     *   }
     * }
     *
     * @param user 用户对象(包含username和password)
     * @return 登录结果(包含token和用户基本信息)
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        // 根据用户名查询用户
        User dbUser = userMapper.findByUsername(user.getUsername());

        // 用户不存在
        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        // 密码错误
        if (!dbUser.getPassword().equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        // 账号被封禁：提供明确的申诉邮箱，形成完整的用户体验闭环
        if (dbUser.getStatus() != null && dbUser.getStatus() == 1) {
            return Result.error("账号已被封禁！请联系管理员申诉: 15154797270@163.com");
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(
                dbUser.getId(),
                dbUser.getUsername(),
                dbUser.getRole()
        );

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        // 返回用户基本信息(不包含密码)
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", dbUser.getId());
        userInfo.put("username", dbUser.getUsername());
        userInfo.put("nickname", dbUser.getNickname());
        userInfo.put("avatar", dbUser.getAvatar());
        userInfo.put("role", dbUser.getRole());
        data.put("userInfo", userInfo);

        return Result.success(data, "登录成功");
    }

    /**
     * 用户注册
     *
     * 接口地址: POST /user/register
     * 请求参数:
     * {
     *   "username": "testuser",
     *   "password": "123456",
     *   "nickname": "测试用户",
     *   "role": "user"
     * }
     *
     * @param user 用户对象
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        // 检查用户名是否已存在
        User existUser = userMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }

        // 校验必填字段
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 默认角色为普通用户
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("user");
        }

        // 默认昵称为用户名
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            user.setNickname(user.getUsername());
        }

        // 插入用户数据
        userMapper.insert(user);

        return Result.success("注册成功");
    }

    /**
     * 获取当前登录用户信息
     *
     * 接口地址: GET /user/info
     * 请求头: Authorization: Bearer {token}
     *
     * 用途: 前端刷新页面时,通过token获取用户信息
     *
     * @param authorization 请求头中的Authorization字段
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result getUserInfo(@RequestHeader("Authorization") String authorization) {
        // 提取token(去掉"Bearer "前缀)
        String token = authorization.replace("Bearer ", "");

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            return Result.error("token无效或已过期", 401);
        }

        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 查询用户信息
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 返回用户基本信息(不包含密码)
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole());

        return Result.success(userInfo);
    }

    /**
     * 修改用户资料
     *
     * 接口地址: POST /user/update
     * 请求参数:
     * {
     *   "id": 1,
     *   "nickname": "新昵称",
     *   "avatar": "头像URL"
     * }
     *
     * @param user 用户对象
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody User user) {
        userMapper.update(user);
        return Result.success("修改成功");
    }
}