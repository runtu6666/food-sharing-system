package com.foodshare.config;

import com.foodshare.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 职责: 配置跨域、拦截器以及静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                // 放行静态图片资源的路径，防止静态资源请求被JWT拦截器拦截返回401
                .excludePathPatterns("/images/**", "/uploads/**", "/static/**", "/login", "/register");
    }

    /**
     * 配置静态资源映射
     * 极其稳妥的相对路径映射法，直接指向项目根目录下的 uploads 文件夹
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // file:uploads/ 表示相对项目根目录(user.dir)下的 uploads 文件夹
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}