package com.foodshare.service;

import com.foodshare.entity.User;
import com.foodshare.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) return null;
        if (!user.getPassword().equals(password)) return null;
        if (user.getStatus() == 1) return null; // 封号
        return user;
    }

    // 按用户名查询用户：用于登录时区分“密码错误”和“账号被封禁”提示
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    // 注册用户：根据传入的role区分普通用户(user)和商家(shop)两种角色
    public boolean register(String username, String password, String nickname, String role) {
        User exist = userMapper.findByUsername(username);
        if (exist != null) return false; // 用户名已存在
        // 前端可能没有显式传角色，这里做一层兜底，默认按普通用户处理
        if (role == null || role.isEmpty()) {
            role = "user";
        }
        // 只允许 user / shop 两种取值，避免出现脏数据
        if (!"user".equals(role) && !"shop".equals(role)) {
            role = "user";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setRole(role);
        userMapper.insert(user);
        return true;
    }

    // 修改个人资料：0=成功，1=用户名重复，2=昵称重复，3=参数不完整
    public int updateProfile(Long userId, String username, String nickname) {
        if (username == null || username.trim().isEmpty() || nickname == null || nickname.trim().isEmpty()) {
            return 3;
        }
        username = username.trim();
        nickname = nickname.trim();

        // 用户名唯一性：如果查到的是别人账号，则不允许修改
        User existByUsername = userMapper.findByUsername(username);
        if (existByUsername != null && !existByUsername.getId().equals(userId)) {
            return 1;
        }

        // 昵称唯一性：如果查到的是别人账号，则不允许修改
        User existByNickname = userMapper.findByNickname(nickname);
        if (existByNickname != null && !existByNickname.getId().equals(userId)) {
            return 2;
        }

        userMapper.updateProfile(userId, username, nickname);
        return 0;
    }
}
