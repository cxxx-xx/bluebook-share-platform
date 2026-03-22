package com.bluebook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Spring Security安全配置类
 * 
 * 技术说明：
 * - 使用Spring Security 6.x版本
 * - 采用Lambda DSL配置方式（Spring Security 5.4+推荐）
 * - 基于Session的身份验证机制
 * 
 * 安全策略：
 * 1. 禁用CSRF保护（前后端分离架构，使用Session验证）
 * 2. 允许所有请求（API级别的权限控制在Controller层实现）
 * 3. 禁用默认表单登录和登出
 * 4. 配置会话管理：防止会话固定攻击，限制单用户单会话
 * 
 * @Configuration 标识为配置类
 * @EnableWebSecurity 启用Spring Security Web安全功能
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     * 
     * 安全配置详情：
     * - csrf().disable()：禁用CSRF保护（适用于RESTful API）
     * - authorizeHttpRequests()：配置URL访问权限
     * - formLogin().disable()：禁用默认登录页面
     * - sessionManagement()：配置会话管理策略
     * 
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护
            // 原因：前后端分离架构，使用Session进行身份验证
            .csrf(csrf -> csrf.disable())
            
            // 配置请求授权
            // 所有请求都允许访问，权限控制在Controller层通过Session判断
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().permitAll()
            )
            
            // 禁用默认表单登录
            .formLogin(form -> form.disable())
            
            // 禁用默认登出
            .logout(logout -> logout.disable())
            
            // 配置会话管理
            .sessionManagement(session -> session
                // 防止会话固定攻击
                .sessionFixation().migrateSession()
                // 限制每个用户只能有一个活跃会话
                .maximumSessions(1)
            );

        return http.build();
    }
    
    /**
     * 创建HTTP会话事件发布器
     * 
     * 作用：
     * - 监听会话创建和销毁事件
     * - 支持会话并发控制
     * - 与Spring Security的会话管理集成
     * 
     * @return HttpSessionEventPublisher 会话事件发布器
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
