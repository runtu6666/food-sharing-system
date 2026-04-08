package com.foodshare.interceptor;

import com.foodshare.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT拦截器
 * 职责: 拦截所有请求,验证JWT token的有效性
 *
 * 工作流程:
 * 1. 从请求头中获取token
 * 2. 验证token是否有效
 * 3. 如果有效,放行请求;如果无效,返回401错误
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 前置处理:在请求到达Controller之前执行
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器
     * @return true-放行, false-拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // OPTIONS请求直接放行(浏览器跨域预检请求)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 白名单:这些接口不需要token验证
        String uri = request.getRequestURI();
        if (isWhitelist(uri)) {
            return true;
        }

        // 从请求头中获取token
        String authorization = request.getHeader("Authorization");

        // 没有token
        if (authorization == null || authorization.isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return false;
        }

        // 提取token(去掉"Bearer "前缀)
        String token = authorization.replace("Bearer ", "");

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期,请重新登录\"}");
            return false;
        }

        // 将用户信息存入request,供Controller使用
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        // 放行
        return true;
    }

    /**
     * 判断URI是否在白名单中
     * 白名单中的接口不需要token验证
     *
     * @param uri 请求URI
     * @return true-在白名单中, false-不在白名单中
     */
    private boolean isWhitelist(String uri) {
        // 登录和注册接口不需要验证
        if (uri.equals("/user/login")) return true;
        if (uri.equals("/user/register")) return true;

        // 静态资源不需要验证
        if (uri.startsWith("/static/")) return true;
        if (uri.endsWith(".css")) return true;
        if (uri.endsWith(".js")) return true;
        if (uri.endsWith(".png")) return true;
        if (uri.endsWith(".jpg")) return true;

        return false;
    }
}