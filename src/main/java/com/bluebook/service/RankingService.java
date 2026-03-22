package com.bluebook.service;

import com.bluebook.model.User;
import com.bluebook.model.Post;
import com.bluebook.repository.UserRepository;
import com.bluebook.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 排行榜服务类
 * 
 * 功能模块：
 * 1. 总榜排名 - getSearchRank, getFameRank
 * 2. 知名度增长榜 - getDailyFameGrowthRank, getWeeklyFameGrowthRank, getMonthlyFameGrowthRank, getYearlyFameGrowthRank
 * 3. 互动榜排名 - getFansRank, getLikesRank, getPostsRank, getCollectsRank
 * 4. 板块排名 - getCategorySearchRank, getCategoryFameRank, getCategoryRank
 * 5. 统计增量 - incrementSearchCount, incrementFame
 */
@Service
public class RankingService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    // ==================== 总榜排名模块 ====================

    /**
     * 获取搜索量排行榜
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getSearchRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findSearchRank(pageable);
        return convertToResponse(userPage, "search", null);
    }

    /**
     * 获取知名度排行榜
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getFameRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findFameRank(pageable);
        return convertToResponse(userPage, "fame", null);
    }

    // ==================== 知名度增长榜模块 ====================

    /**
     * 获取日知名度增长排行榜
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getDailyFameGrowthRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findDailyFameGrowthRank(pageable);
        return convertToResponse(userPage, "daily", null);
    }

    /**
     * 获取周知名度增长排行榜
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getWeeklyFameGrowthRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findWeeklyFameGrowthRank(pageable);
        return convertToResponse(userPage, "weekly", null);
    }

    /**
     * 获取月知名度增长排行榜
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getMonthlyFameGrowthRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findMonthlyFameGrowthRank(pageable);
        return convertToResponse(userPage, "monthly", null);
    }

    /**
     * 获取年知名度增长排行榜
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getYearlyFameGrowthRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findYearlyFameGrowthRank(pageable);
        return convertToResponse(userPage, "yearly", null);
    }

    // ==================== 互动榜排名模块 ====================

    /**
     * 获取粉丝数排行榜
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getFansRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findFansRank(pageable);
        return convertToResponse(userPage, "fans", null);
    }

    /**
     * 获取获赞数排行榜
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getLikesRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findLikesRank(pageable);
        return convertToResponse(userPage, "likes", null);
    }

    /**
     * 获取分享数排行榜
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getPostsRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findPostsRank(pageable);
        return convertToResponse(userPage, "posts", null);
    }

    /**
     * 获取被收藏数排行榜
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getCollectsRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findCollectsRank(pageable);
        return convertToResponse(userPage, "collects", null);
    }

    // ==================== 板块排名模块 ====================

    /**
     * 获取板块搜索量排行榜
     * @param category 板块名称
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getCategorySearchRank(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findCategorySearchRank(category, pageable);
        return convertToResponse(userPage, "search", category);
    }

    /**
     * 获取板块知名度排行榜
     * @param category 板块名称
     * @param page 页码
     * @param size 每页数量
     * @return 排行榜数据
     */
    public Map<String, Object> getCategoryFameRank(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findCategoryFameRank(category, pageable);
        return convertToResponse(userPage, "fame", category);
    }

    /**
     * 获取板块帖子列表
     * @param category 板块名称
     * @param page 页码
     * @param size 每页数量
     * @return 帖子列表数据
     */
    public Map<String, Object> getCategoryRank(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> posts = new ArrayList<>();
        
        for (Post post : postPage.getContent()) {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", post.getId());
            postMap.put("title", post.getTitle());
            postMap.put("likesCount", post.getLikesCount());
            postMap.put("collectsCount", post.getCollectsCount());
            
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", post.getUser().getId());
            userMap.put("username", post.getUser().getUsername());
            userMap.put("avatar", post.getUser().getAvatar());
            userMap.put("fame", post.getUser().getFame());
            postMap.put("user", userMap);
            
            posts.add(postMap);
        }
        
        response.put("posts", posts);
        response.put("currentPage", postPage.getNumber());
        response.put("totalPages", postPage.getTotalPages());
        response.put("totalElements", postPage.getTotalElements());
        
        return response;
    }

    // ==================== 统计增量模块 ====================

    /**
     * 增加用户搜索计数
     * 同时更新总搜索计数和板块搜索计数
     * @param userId 用户ID
     * @param category 板块名称
     */
    public void incrementSearchCount(Long userId, String category) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && !user.isAdmin()) {
            user.setSearchCount(user.getSearchCount() + 1);
            incrementCategorySearchCount(user, category);
            userRepository.save(user);
        }
    }

    /**
     * 增加板块搜索计数
     */
    private void incrementCategorySearchCount(User user, String category) {
        if (category == null) return;
        switch (category) {
            case "food": user.setFoodSearchCount(user.getFoodSearchCount() + 1); break;
            case "travel": user.setTravelSearchCount(user.getTravelSearchCount() + 1); break;
            case "digital": user.setDigitalSearchCount(user.getDigitalSearchCount() + 1); break;
            case "fashion": user.setFashionSearchCount(user.getFashionSearchCount() + 1); break;
            case "home": user.setHomeSearchCount(user.getHomeSearchCount() + 1); break;
            case "beauty": user.setBeautySearchCount(user.getBeautySearchCount() + 1); break;
            case "fitness": user.setFitnessSearchCount(user.getFitnessSearchCount() + 1); break;
            case "other": user.setOtherSearchCount(user.getOtherSearchCount() + 1); break;
        }
    }

    /**
     * 增加用户知名度
     * 同时更新总知名度和板块知名度
     * @param userId 用户ID
     * @param amount 增量值
     * @param category 板块名称
     */
    public void incrementFame(Long userId, int amount, String category) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && !user.isAdmin()) {
            user.setFame(user.getFame() + amount);
            incrementCategoryFame(user, category, amount);
            userRepository.save(user);
        }
    }

    /**
     * 增加板块知名度
     */
    private void incrementCategoryFame(User user, String category, int amount) {
        if (category == null) return;
        switch (category) {
            case "food": user.setFoodFame(user.getFoodFame() + amount); break;
            case "travel": user.setTravelFame(user.getTravelFame() + amount); break;
            case "digital": user.setDigitalFame(user.getDigitalFame() + amount); break;
            case "fashion": user.setFashionFame(user.getFashionFame() + amount); break;
            case "home": user.setHomeFame(user.getHomeFame() + amount); break;
            case "beauty": user.setBeautyFame(user.getBeautyFame() + amount); break;
            case "fitness": user.setFitnessFame(user.getFitnessFame() + amount); break;
            case "other": user.setOtherFame(user.getOtherFame() + amount); break;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 将分页结果转换为响应格式
     * 过滤掉分数为0的用户
     */
    private Map<String, Object> convertToResponse(Page<User> userPage, String rankType, String category) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> users = new ArrayList<>();
        
        int rank = userPage.getNumber() * userPage.getSize() + 1;
        for (User user : userPage.getContent()) {
            long scoreValue = getScoreValue(user, rankType, category);
            if (scoreValue <= 0) continue;
            
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("rank", rank++);
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("avatar", user.getAvatar());
            userMap.put("level", user.getLevel());
            userMap.put("fame", user.getFame());
            userMap.put("fansCount", user.getFansCount());
            userMap.put("postCount", user.getPostCount());
            userMap.put("likesReceived", user.getLikesReceived());
            userMap.put("collectsReceived", user.getCollectsReceived());
            userMap.put("searchCount", user.getSearchCount());
            userMap.put("dailyFameGrowth", user.getDailyFameGrowth());
            userMap.put("weeklyFameGrowth", user.getWeeklyFameGrowth());
            userMap.put("monthlyFameGrowth", user.getMonthlyFameGrowth());
            userMap.put("yearlyFameGrowth", user.getYearlyFameGrowth());
            userMap.put("scoreValue", scoreValue);
            users.add(userMap);
        }
        
        response.put("users", users);
        response.put("currentPage", userPage.getNumber());
        response.put("totalPages", userPage.getTotalPages());
        response.put("totalElements", userPage.getTotalElements());
        response.put("rankType", rankType);
        if (category != null) {
            response.put("category", category);
        }
        
        return response;
    }

    /**
     * 根据排名类型和板块获取分数值
     */
    private long getScoreValue(User user, String rankType, String category) {
        if (category != null) {
            // 板块排名
            switch (category) {
                case "food":
                    return "search".equals(rankType) ? user.getFoodSearchCount() : user.getFoodFame();
                case "travel":
                    return "search".equals(rankType) ? user.getTravelSearchCount() : user.getTravelFame();
                case "digital":
                    return "search".equals(rankType) ? user.getDigitalSearchCount() : user.getDigitalFame();
                case "fashion":
                    return "search".equals(rankType) ? user.getFashionSearchCount() : user.getFashionFame();
                case "home":
                    return "search".equals(rankType) ? user.getHomeSearchCount() : user.getHomeFame();
                case "beauty":
                    return "search".equals(rankType) ? user.getBeautySearchCount() : user.getBeautyFame();
                case "fitness":
                    return "search".equals(rankType) ? user.getFitnessSearchCount() : user.getFitnessFame();
                case "other":
                    return "search".equals(rankType) ? user.getOtherSearchCount() : user.getOtherFame();
                default:
                    return 0;
            }
        } else {
            // 总榜排名
            switch (rankType) {
                case "search": return user.getSearchCount();
                case "fame": return user.getFame();
                case "daily": return user.getDailyFameGrowth();
                case "weekly": return user.getWeeklyFameGrowth();
                case "monthly": return user.getMonthlyFameGrowth();
                case "yearly": return user.getYearlyFameGrowth();
                case "fans": return user.getFansCount();
                case "likes": return user.getLikesReceived();
                case "posts": return user.getPostCount();
                case "collects": return user.getCollectsReceived();
                default: return 0;
            }
        }
    }
}
