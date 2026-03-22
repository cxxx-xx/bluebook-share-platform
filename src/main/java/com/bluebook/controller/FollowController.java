package com.bluebook.controller;

import com.bluebook.service.FollowService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关注控制器
 * 
 * API端点：
 * - POST /api/follows/{userId} - 关注用户
 * - DELETE /api/follows/{userId} - 取消关注
 * - GET /api/follows/status/{userId} - 获取关注状态
 * - GET /api/follows/followers/{userId} - 获取粉丝列表
 * - GET /api/follows/following/{userId} - 获取关注列表
 */
@RestController
@RequestMapping("/api/follows")
public class FollowController {
    @Autowired
    private FollowService followService;

    @PostMapping("/{userId}")
    public Map<String, Object> followUser(@PathVariable Long userId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) session.getAttribute("userId");
            if (currentUserId == null) {
                throw new RuntimeException("请先登录");
            }
            followService.follow(currentUserId, userId);
            result.put("success", true);
            result.put("message", "关注成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{userId}")
    public Map<String, Object> unfollowUser(@PathVariable Long userId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) session.getAttribute("userId");
            if (currentUserId == null) {
                throw new RuntimeException("请先登录");
            }
            followService.unfollow(currentUserId, userId);
            result.put("success", true);
            result.put("message", "取消关注成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/status/{userId}")
    public Map<String, Object> isFollowing(@PathVariable Long userId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) session.getAttribute("userId");
            if (currentUserId == null) {
                result.put("isFollowing", false);
            } else {
                result.put("isFollowing", followService.isFollowing(currentUserId, userId));
            }
        } catch (Exception e) {
            result.put("isFollowing", false);
        }
        return result;
    }

    @GetMapping("/followers/{userId}")
    public List<Map<String, Object>> getFollowers(@PathVariable Long userId) {
        return followService.getFollowers(userId);
    }

    @GetMapping("/following/{userId}")
    public List<Map<String, Object>> getFollowing(@PathVariable Long userId) {
        return followService.getFollowing(userId);
    }
}