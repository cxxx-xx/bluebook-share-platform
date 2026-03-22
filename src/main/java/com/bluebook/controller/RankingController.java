package com.bluebook.controller;

import com.bluebook.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 排行榜控制器
 * 
 * API端点：
 * - GET /api/ranking/search - 搜索量排行榜
 * - GET /api/ranking/fame - 知名度排行榜
 * - GET /api/ranking/daily - 日知名度增长榜
 * - GET /api/ranking/weekly - 周知名度增长榜
 * - GET /api/ranking/monthly - 月知名度增长榜
 * - GET /api/ranking/yearly - 年知名度增长榜
 * - GET /api/ranking/fans - 粉丝排行榜
 * - GET /api/ranking/likes - 获赞排行榜
 * - GET /api/ranking/posts - 分享排行榜
 * - GET /api/ranking/collects - 收藏排行榜
 * - GET /api/ranking/category/{category} - 板块帖子列表
 * - GET /api/ranking/category/{category}/search - 板块搜索量榜
 * - GET /api/ranking/category/{category}/fame - 板块知名度榜
 */
@RestController
@RequestMapping("/api/ranking")
public class RankingController {
    @Autowired
    private RankingService rankingService;

    @GetMapping("/search")
    public Map<String, Object> getSearchRank(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getSearchRank(page, size);
    }

    @GetMapping("/fame")
    public Map<String, Object> getFameRank(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getFameRank(page, size);
    }

    @GetMapping("/daily")
    public Map<String, Object> getDailyFameGrowthRank(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getDailyFameGrowthRank(page, size);
    }

    @GetMapping("/weekly")
    public Map<String, Object> getWeeklyFameGrowthRank(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getWeeklyFameGrowthRank(page, size);
    }

    @GetMapping("/monthly")
    public Map<String, Object> getMonthlyFameGrowthRank(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getMonthlyFameGrowthRank(page, size);
    }

    @GetMapping("/yearly")
    public Map<String, Object> getYearlyFameGrowthRank(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getYearlyFameGrowthRank(page, size);
    }

    @GetMapping("/fans")
    public Map<String, Object> getFansRank(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getFansRank(page, size);
    }

    @GetMapping("/likes")
    public Map<String, Object> getLikesRank(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getLikesRank(page, size);
    }

    @GetMapping("/posts")
    public Map<String, Object> getPostsRank(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getPostsRank(page, size);
    }

    @GetMapping("/collects")
    public Map<String, Object> getCollectsRank(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getCollectsRank(page, size);
    }

    @GetMapping("/category/{category}")
    public Map<String, Object> getCategoryRank(@PathVariable String category, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getCategoryRank(category, page, size);
    }

    @GetMapping("/category/{category}/search")
    public Map<String, Object> getCategorySearchRank(@PathVariable String category, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getCategorySearchRank(category, page, size);
    }

    @GetMapping("/category/{category}/fame")
    public Map<String, Object> getCategoryFameRank(@PathVariable String category, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return rankingService.getCategoryFameRank(category, page, size);
    }
}
