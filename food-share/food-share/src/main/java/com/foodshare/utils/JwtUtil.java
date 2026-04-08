package com.foodshare.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 职责: 生成和验证JWT token,实现用户身份认证
 *
 * JWT格式: Header.Payload.Signature
 * Payload中包含: userId, username, role, 过期时间等
 */
@Component
public class JwtUtil {

    /**
     * 密钥(用于签名和验证token)
     * 注意: 生产环境应该放在配置文件中,并定期更换
     */
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * token有效期: 7天(单位:毫秒)
     */
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成JWT token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色(user/shop/admin)
     * @return JWT token字符串
     */
    public String generateToken(Long userId, String username, String role) {
        // 当前时间
        Date now = new Date();
        // 过期时间 = 当前时间 + 有效期
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        // 构建token的payload(载荷)
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        // 生成token
        return Jwts.builder()
                .setClaims(claims)                    // 设置载荷
                .setSubject(username)                 // 设置主题(用户名)
                .setIssuedAt(now)                     // 设置签发时间
                .setExpiration(expiryDate)            // 设置过期时间
                .signWith(SECRET_KEY)                 // 使用密钥签名
                .compact();                           // 生成最终的token字符串
    }

    /**
     * 从token中解析出Claims(载荷数据)
     *
     * @param token JWT token字符串
     * @return Claims对象,包含userId、username、role等信息
     * @throws Exception 如果token无效或过期,抛出异常
     */
    public Claims parseToken(String token) throws Exception {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)            // 使用密钥验证签名
                .build()
                .parseClaimsJws(token)                // 解析token
                .getBody();                           // 获取载荷
    }

    /**
     * 从token中获取用户ID
     *
     * @param token JWT token字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            // 注意: JWT默认将数字存为Integer,需要转换为Long
            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从token中获取用户名
     *
     * @param token JWT token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("username", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从token中获取用户角色
     *
     * @param token JWT token字符串
     * @return 用户角色(user/shop/admin)
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证token是否有效
     *
     * @param token JWT token字符串
     * @return true-有效, false-无效或过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            // token无效或过期
            return false;
        }
    }

    /**
     * 检查token是否即将过期(剩余时间小于1天)
     * 用于前端判断是否需要刷新token
     *
     * @param token JWT token字符串
     * @return true-即将过期, false-还有充足时间
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            long timeLeft = expiration.getTime() - System.currentTimeMillis();
            // 如果剩余时间小于1天,返回true
            return timeLeft < 24 * 60 * 60 * 1000;
        } catch (Exception e) {
            return true;
        }
    }
}
