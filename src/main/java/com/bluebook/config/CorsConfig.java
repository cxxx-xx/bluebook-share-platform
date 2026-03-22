package com.bluebook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域资源共享(CORS)配置类
 * 
 * 技术说明：
 * - 使用Spring框架的CorsFilter实现CORS过滤
 * - 支持Vue.js前端开发服务器跨域访问后端API
 * 
 * CORS配置作用：
 * 1. 允许前端从不同域名/端口访问后端API
 * 2. 支持携带Cookie/Session进行身份验证
 * 3. 缓存预检请求结果，提高性能
 * 
 * @Configuration 标识为配置类，由Spring容器管理
 */
@Configuration
public class CorsConfig {

    /**
     * 创建CORS过滤器Bean
     * 
     * 配置内容：
     * 1. 允许的源：前端开发服务器地址（localhost:3000-3003）
     * 2. 允许的方法：所有HTTP方法（GET、POST、PUT、DELETE等）
     * 3. 允许的请求头：所有请求头
     * 4. 允许凭证：支持Cookie和Session
     * 5. 预检缓存：3600秒
     * 
     * @return CorsFilter CORS过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的前端域名（Vue.js开发服务器）
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:3001");
        config.addAllowedOrigin("http://localhost:3002");
        config.addAllowedOrigin("http://localhost:3003");
        config.addAllowedOrigin("http://127.0.0.1:3000");
        config.addAllowedOrigin("http://127.0.0.1:3001");
        config.addAllowedOrigin("http://127.0.0.1:3002");
        config.addAllowedOrigin("http://127.0.0.1:3003");
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://127.0.0.1:8080");
        
        // 允许所有HTTP请求方法
        config.addAllowedMethod("*");
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许携带凭证（Cookie、Session）
        // 必须设置，否则前端无法保持登录状态
        config.setAllowCredentials(true);
        
        // 预检请求(OPTIONS)的缓存时间（秒）
        config.setMaxAge(3600L);
        
        // 注册CORS配置到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
