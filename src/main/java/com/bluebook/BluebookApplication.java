/**
 * 蓝书 - 多品类好物分享平台
 * 
 * 技术栈：
 * - Spring Boot 3.2.0：核心框架，提供依赖注入、自动配置等功能
 * - Spring Data JPA：ORM框架，简化数据库操作
 * - Spring Security：安全框架，配置CSRF保护和会话管理
 * - MySQL：关系型数据库
 * - Hibernate：JPA实现，提供对象关系映射
 * 
 * 功能模块：
 * 1. 用户模块：注册、登录、个人信息管理
 * 2. 帖子模块：发帖、浏览、点赞、收藏、评论
 * 3. 关注模块：关注用户、粉丝管理
 * 4. 消息模块：私信、系统通知
 * 5. 排行榜模块：各类排行榜
 * 6. 管理员模块：用户管理、帖子管理、系统通知
 * 
 * @author Bluebook Team
 * @version 1.0
 */
package com.bluebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 蓝书应用主入口类
 * 
 * @SpringBootApplication 组合注解，包含：
 * - @Configuration：标识为配置类
 * - @EnableAutoConfiguration：启用Spring Boot自动配置
 * - @ComponentScan：自动扫描当前包及子包下的组件
 */
@SpringBootApplication
public class BluebookApplication {
    
    /**
     * 应用程序入口方法
     * 启动Spring Boot应用，初始化IoC容器和内嵌Tomcat服务器
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BluebookApplication.class, args);
    }
}
