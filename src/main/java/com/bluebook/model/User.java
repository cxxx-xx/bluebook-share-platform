/**
 * 用户实体类
 * 
 * 对应数据库表：users
 * 
 * 技术说明：
 * - 使用JPA (Jakarta Persistence API) 进行对象关系映射
 * - @Entity 标识为JPA实体类
 * - @Table 指定对应的数据库表名
 * - @Id 标识主键字段
 * - @GeneratedValue 指定主键生成策略（自增）
 * - @Column 定义数据库列属性
 * - @ManyToOne/@OneToMany 定义实体关联关系
 * - @JsonIgnore 序列化时忽略该字段
 * - @JsonProperty 控制JSON序列化行为
 * 
 * 实体关系：
 * - 一对多关联Post：一个用户可以发布多篇帖子
 * - 一对多关联Follow：一个用户可以有多个关注者和被关注者
 * - 一对多关联Like：一个用户可以点赞多篇帖子
 * - 一对多关联Collect：一个用户可以收藏多篇帖子
 * - 一对多关联Dislike：一个用户可以踩多篇帖子
 * 
 * @author Bluebook Team
 * @version 1.0
 */
package com.bluebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    /**
     * 用户唯一标识ID
     * 主键，自增策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     * 唯一约束，不允许为空
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * 手机号
     * 唯一约束，用于注册和登录
     */
    @Column(unique = true, nullable = false)
    private String phone;

    /**
     * 密码
     * WRITE_ONLY：仅允许写入，序列化时不返回给前端（安全考虑）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    /**
     * 头像文件名
     * 默认值为default.jpg
     */
    @Column(nullable = false, columnDefinition = "varchar(255) default 'default.jpg'")
    private String avatar;

    /**
     * 是否为管理员
     * true: 管理员，可以访问后台管理功能
     * false: 普通用户
     */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isAdmin;

    /**
     * 知名度值
     * 综合评分，用于排行榜
     */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long fame;

    /**
     * 用户等级
     * 1-9级，根据知名度自动计算
     */
    @Column(nullable = false, columnDefinition = "int default 1")
    private int level;

    /**
     * 粉丝数量
     * 关注该用户的人数
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int fansCount;

    /**
     * 关注数量
     * 该用户关注的人数
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int followCount;

    /**
     * 发帖数量
     * 该用户发布的帖子总数
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int postCount;

    /**
     * 收到的点赞总数
     * 所有帖子被点赞的总数
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int likesReceived;

    /**
     * 收到的收藏总数
     * 所有帖子被收藏的总数
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int collectsReceived;

    /**
     * 收到的踩总数
     * 所有帖子被踩的总数
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int dislikesReceived;

    /**
     * 被搜索次数
     * 用户被其他用户搜索的总次数
     */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long searchCount;

    /**
     * 日知名度增长值
     * 用于日增长排行榜
     */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long dailyFameGrowth;

    /**
     * 周知名度增长值
     * 用于周增长排行榜
     */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long weeklyFameGrowth;

    /**
     * 月知名度增长值
     * 用于月增长排行榜
     */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long monthlyFameGrowth;

    /**
     * 年知名度增长值
     * 用于年增长排行榜
     */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long yearlyFameGrowth;

    // ==================== 分类统计字段 ====================
    // 每个分类包含两个统计值：搜索次数和知名度值

    /** 美食分类 - 搜索次数 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long foodSearchCount;

    /** 美食分类 - 知名度 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long foodFame;

    /** 旅行分类 - 搜索次数 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long travelSearchCount;

    /** 旅行分类 - 知名度 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long travelFame;

    /** 数码分类 - 搜索次数 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long digitalSearchCount;

    /** 数码分类 - 知名度 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long digitalFame;

    /** 穿搭分类 - 搜索次数 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long fashionSearchCount;

    /** 穿搭分类 - 知名度 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long fashionFame;

    /** 家居分类 - 搜索次数 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long homeSearchCount;

    /** 家居分类 - 知名度 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long homeFame;

    /** 美妆分类 - 搜索次数 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long beautySearchCount;

    /** 美妆分类 - 知名度 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long beautyFame;

    /** 健身分类 - 搜索次数 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long fitnessSearchCount;

    /** 健身分类 - 知名度 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long fitnessFame;

    /** 其他分类 - 搜索次数 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long otherSearchCount;

    /** 其他分类 - 知名度 */
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long otherFame;

    /** 创建时间 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ==================== 实体关联关系 ====================

    /**
     * 用户发布的帖子列表
     * 一对多关系，懒加载
     * @JsonIgnore 序列化时忽略，避免循环引用
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts;

    /**
     * 用户的关注列表
     * 一对多关系，该用户关注的人
     */
    @JsonIgnore
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> following;

    /**
     * 用户的粉丝列表
     * 一对多关系，关注该用户的人
     */
    @JsonIgnore
    @OneToMany(mappedBy = "followed", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> followers;

    /**
     * 用户的点赞记录
     * 一对多关系
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> likes;

    /**
     * 用户的收藏记录
     * 一对多关系
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Collect> collects;

    /**
     * 用户的踩记录
     * 一对多关系
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dislike> dislikes;

    // ==================== Getter和Setter方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * 获取管理员状态
     * @JsonProperty("isAdmin") 确保序列化时字段名为isAdmin
     */
    @JsonProperty("isAdmin")
    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public long getFame() {
        return fame;
    }

    public void setFame(long fame) {
        this.fame = fame;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFansCount() {
        return fansCount;
    }

    public void setFansCount(int fansCount) {
        this.fansCount = fansCount;
    }

    public int getFollowCount() {
        return followCount;
    }

    public void setFollowCount(int followCount) {
        this.followCount = followCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getLikesReceived() {
        return likesReceived;
    }

    public void setLikesReceived(int likesReceived) {
        this.likesReceived = likesReceived;
    }

    public int getCollectsReceived() {
        return collectsReceived;
    }

    public void setCollectsReceived(int collectsReceived) {
        this.collectsReceived = collectsReceived;
    }

    public int getDislikesReceived() {
        return dislikesReceived;
    }

    public void setDislikesReceived(int dislikesReceived) {
        this.dislikesReceived = dislikesReceived;
    }

    public long getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(long searchCount) {
        this.searchCount = searchCount;
    }

    public long getDailyFameGrowth() {
        return dailyFameGrowth;
    }

    public void setDailyFameGrowth(long dailyFameGrowth) {
        this.dailyFameGrowth = dailyFameGrowth;
    }

    public long getWeeklyFameGrowth() {
        return weeklyFameGrowth;
    }

    public void setWeeklyFameGrowth(long weeklyFameGrowth) {
        this.weeklyFameGrowth = weeklyFameGrowth;
    }

    public long getMonthlyFameGrowth() {
        return monthlyFameGrowth;
    }

    public void setMonthlyFameGrowth(long monthlyFameGrowth) {
        this.monthlyFameGrowth = monthlyFameGrowth;
    }

    public long getYearlyFameGrowth() {
        return yearlyFameGrowth;
    }

    public void setYearlyFameGrowth(long yearlyFameGrowth) {
        this.yearlyFameGrowth = yearlyFameGrowth;
    }

    // 分类统计Getter/Setter
    public long getFoodSearchCount() { return foodSearchCount; }
    public void setFoodSearchCount(long foodSearchCount) { this.foodSearchCount = foodSearchCount; }
    public long getFoodFame() { return foodFame; }
    public void setFoodFame(long foodFame) { this.foodFame = foodFame; }

    public long getTravelSearchCount() { return travelSearchCount; }
    public void setTravelSearchCount(long travelSearchCount) { this.travelSearchCount = travelSearchCount; }
    public long getTravelFame() { return travelFame; }
    public void setTravelFame(long travelFame) { this.travelFame = travelFame; }

    public long getDigitalSearchCount() { return digitalSearchCount; }
    public void setDigitalSearchCount(long digitalSearchCount) { this.digitalSearchCount = digitalSearchCount; }
    public long getDigitalFame() { return digitalFame; }
    public void setDigitalFame(long digitalFame) { this.digitalFame = digitalFame; }

    public long getFashionSearchCount() { return fashionSearchCount; }
    public void setFashionSearchCount(long fashionSearchCount) { this.fashionSearchCount = fashionSearchCount; }
    public long getFashionFame() { return fashionFame; }
    public void setFashionFame(long fashionFame) { this.fashionFame = fashionFame; }

    public long getHomeSearchCount() { return homeSearchCount; }
    public void setHomeSearchCount(long homeSearchCount) { this.homeSearchCount = homeSearchCount; }
    public long getHomeFame() { return homeFame; }
    public void setHomeFame(long homeFame) { this.homeFame = homeFame; }

    public long getBeautySearchCount() { return beautySearchCount; }
    public void setBeautySearchCount(long beautySearchCount) { this.beautySearchCount = beautySearchCount; }
    public long getBeautyFame() { return beautyFame; }
    public void setBeautyFame(long beautyFame) { this.beautyFame = beautyFame; }

    public long getFitnessSearchCount() { return fitnessSearchCount; }
    public void setFitnessSearchCount(long fitnessSearchCount) { this.fitnessSearchCount = fitnessSearchCount; }
    public long getFitnessFame() { return fitnessFame; }
    public void setFitnessFame(long fitnessFame) { this.fitnessFame = fitnessFame; }

    public long getOtherSearchCount() { return otherSearchCount; }
    public void setOtherSearchCount(long otherSearchCount) { this.otherSearchCount = otherSearchCount; }
    public long getOtherFame() { return otherFame; }
    public void setOtherFame(long otherFame) { this.otherFame = otherFame; }

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

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<Follow> getFollowing() {
        return following;
    }

    public void setFollowing(List<Follow> following) {
        this.following = following;
    }

    public List<Follow> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Follow> followers) {
        this.followers = followers;
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

    /**
     * JPA生命周期回调 - 持久化前执行
     * 自动设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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
