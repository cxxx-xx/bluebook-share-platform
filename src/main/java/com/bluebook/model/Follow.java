/**
 * 关注关系实体类
 * 
 * 对应数据库表：follows
 * 
 * 技术说明：
 * - @Entity 标识为JPA实体类
 * - @Table 指定数据库表名和唯一约束
 * - @UniqueConstraint 唯一约束：follower_id + followed_id 组合唯一，防止重复关注
 * - @ManyToOne 多对一关系：关注关系关联两个用户
 * 
 * 实体关系：
 * - 多对一关联User(follower)：关注者
 * - 多对一关联User(followed)：被关注者
 * 
 * 业务规则：
 * - 一个用户可以关注多个用户
 * - 一个用户可以被多个用户关注
 * - 同一对关注关系只能存在一次（唯一约束）
 * 
 * @author Bluebook Team
 * @version 1.0
 */
package com.bluebook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followed_id"}))
public class Follow {
    
    /** 关注关系唯一标识ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关注者（发起关注的用户）
     * 多对一关联User
     */
    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    /**
     * 被关注者（被关注的用户）
     * 多对一关联User
     */
    @ManyToOne
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;

    /** 关注时间 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ==================== Getter和Setter方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowed() {
        return followed;
    }

    public void setFollowed(User followed) {
        this.followed = followed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * JPA生命周期回调 - 持久化前执行
     * 自动设置关注时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
