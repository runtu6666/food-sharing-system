package com.foodshare.controller;

import com.foodshare.common.Result;
import com.foodshare.mapper.LoginLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志控制器
 * 管理员查看所有用户登录记录，支持按用户查询
 */
@RestController
@RequestMapping("/loginLog")
@CrossOrigin
public class LoginLogController {

    @Autowired
    private LoginLogMapper loginLogMapper;

    /**
     * 查询某用户的登录记录（最近20条）
     * GET /loginLog/list?userId=1
     */
    @GetMapping("/list")
    public Result list(@RequestParam Long userId) {
        return Result.success(loginLogMapper.findByUserId(userId));
    }
}
