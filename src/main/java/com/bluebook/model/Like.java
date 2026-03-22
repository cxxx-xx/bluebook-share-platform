/**
 * 点赞实体类
 * 
 * 对应数据库表：likes
 * 
 * 技术说明：
 * - @Entity 标识为JPA实体类
 * - @Table 指定数据库表名和唯一约束
 * - @UniqueConstraint 唯一约束：user_id + post_id 组合唯一，防止重复点赞
 * - @ManyToOne 多对一关系：点赞关联用户和帖子
 * 
 * 实体关系：
 * - 多对一关联User：点赞者
 * - 多对一关联Post：被点赞的帖子
 * 
 * 业务规则：
 * - 一个用户对同一篇帖子只能点赞一次
 * - 点赞会增加帖子的点赞计数和用户的获赞计数
 * 
 * @author Bluebook Team
 * @version 1.0
 */
package com.bluebook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
public class Like {
    
    /** 点赞记录唯一标识ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 点赞用户
     * 多对一关联User
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 被点赞的帖子
     * 多对一关联Post
     */
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** 点赞时间 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ==================== Getter和Setter方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * JPA生命周期回调 - 持久化前执行
     * 自动设置点赞时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
