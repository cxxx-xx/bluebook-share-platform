/**
 * 帖子实体类
 * 
 * 对应数据库表：posts
 * 
 * 技术说明：
 * - @Entity 标识为JPA实体类
 * - @Table 指定数据库表名
 * - @ManyToOne 多对一关系：多篇帖子属于一个用户
 * - @OneToMany 一对多关系：一篇帖子有多个点赞、收藏、踩、评论
 * - @JsonIgnore 序列化时忽略关联对象，避免循环引用
 * - CascadeType.ALL 级联操作：删除帖子时同时删除关联的点赞、收藏等
 * - FetchType.LAZY 懒加载：提高查询性能
 * 
 * 实体关系：
 * - 多对一关联User：帖子属于发布者
 * - 一对多关联Like：一篇帖子可以被多人点赞
 * - 一对多关联Collect：一篇帖子可以被多人收藏
 * - 一对多关联Dislike：一篇帖子可以被多人踩
 * - 一对多关联Comment：一篇帖子可以有多条评论
 * 
 * @author Bluebook Team
 * @version 1.0
 */
package com.bluebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {
    
    /** 帖子唯一标识ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 帖子发布者，多对一关联User */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 帖子标题 */
    @Column(nullable = false)
    private String title;

    /** 帖子内容，使用text类型存储长文本 */
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    /** 帖子图片URL */
    @Column(columnDefinition = "varchar(255) default ''")
    private String image;

    /**
     * 帖子分类
     * 可选值：food(美食)、travel(旅行)、digital(数码)、fashion(穿搭)、
     *        home(家居)、beauty(美妆)、fitness(健身)、other(其他)
     */
    @Column(columnDefinition = "varchar(50) default 'other'")
    private String category;

    /** 点赞数量 */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int likesCount;

    /** 收藏数量 */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int collectsCount;

    /** 踩数量 */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int dislikesCount;

    /** 评论数量 */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int commentsCount;

    /** 创建时间 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 点赞记录列表，一对多关系，懒加载 */
    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> likes;

    /** 收藏记录列表，一对多关系，懒加载 */
    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Collect> collects;

    /** 踩记录列表，一对多关系，懒加载 */
    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dislike> dislikes;

    /** 评论列表，一对多关系，懒加载 */
    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCollectsCount() {
        return collectsCount;
    }

    public void setCollectsCount(int collectsCount) {
        this.collectsCount = collectsCount;
    }

    public int getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(int dislikesCount) {
        this.dislikesCount = dislikesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public List<Collect> getCollects() {
        return collects;
    }

    public void setCollects(List<Collect> collects) {
        this.collects = collects;
    }

    public List<Dislike> getDislikes() {
        return dislikes;
    }

    public void setDislikes(List<Dislike> dislikes) {
        this.dislikes = dislikes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    /**
     * JPA生命周期回调 - 持久化前执行
     * 自动设置创建时间、更新时间和默认值
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (category == null) {
            category = "other";
        }
        if (image == null) {
            image = "";
        }
    }

    /**
     * JPA生命周期回调 - 更新前执行
     * 自动更新更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
