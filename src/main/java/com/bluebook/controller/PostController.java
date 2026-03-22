package com.bluebook.controller;

import com.bluebook.model.Post;
import com.bluebook.service.PostService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 帖子控制器
 * 
 * API端点：
 * - POST /api/posts - 创建帖子
 * - GET /api/posts - 获取帖子列表（分页）
 * - GET /api/posts/search - 搜索帖子
 * - GET /api/posts/hot - 获取热门帖子
 * - GET /api/posts/recommended - 获取推荐帖子
 * - GET /api/posts/category/{category} - 获取分类帖子
 * - GET /api/posts/{postId} - 获取帖子详情
 * - POST /api/posts/{postId}/like - 点赞帖子
 * - DELETE /api/posts/{postId}/like - 取消点赞
 * - POST /api/posts/{postId}/collect - 收藏帖子
 * - DELETE /api/posts/{postId}/collect - 取消收藏
 * - POST /api/posts/{postId}/dislike - 踩帖子
 * - DELETE /api/posts/{postId}/dislike - 取消踩
 * - POST /api/posts/{postId}/comments - 添加评论
 * - DELETE /api/posts/comments/{commentId} - 删除评论
 * - POST /api/posts/upload - 上传帖子图片
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping
    public Map<String, Object> createPost(@RequestBody Post post, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            postService.createPost(post);
            result.put("success", true);
            result.put("message", "帖子创建成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping
    public Map<String, Object> getAllPosts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return postService.getAllPosts(page, size);
    }

    @GetMapping("/search")
    public Map<String, Object> searchPosts(@RequestParam String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return postService.searchPosts(keyword, page, size);
    }

    @GetMapping("/hot")
    public Map<String, Object> getHotPosts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return postService.getHotPosts(page, size);
    }

    @GetMapping("/recommended")
    public Map<String, Object> getRecommendedPosts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return postService.getRecommendedPosts(page, size);
    }

    @GetMapping("/category/{category}")
    public Map<String, Object> getPostsByCategory(@PathVariable String category, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return postService.getPostsByCategory(category, page, size);
    }

    @GetMapping("/{postId}")
    public Map<String, Object> getPostById(@PathVariable Long postId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return postService.getPostById(postId, userId);
    }

    @PostMapping("/upload-image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            
            if (file.isEmpty()) {
                throw new RuntimeException("请选择文件");
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            
            String uploadPath = "f:\\1\\bluebook\\src\\main\\resources\\static\\uploads\\";
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            file.transferTo(new File(uploadPath + fileName));
            
            result.put("success", true);
            result.put("url", "/uploads/" + fileName);
            result.put("fileName", fileName);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/{postId}/like")
    public Map<String, Object> likePost(@PathVariable Long postId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            postService.likePost(userId, postId);
            result.put("success", true);
            result.put("message", "点赞成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{postId}/like")
    public Map<String, Object> unlikePost(@PathVariable Long postId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            postService.unlikePost(userId, postId);
            result.put("success", true);
            result.put("message", "取消点赞成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/{postId}/collect")
    public Map<String, Object> collectPost(@PathVariable Long postId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            postService.collectPost(userId, postId);
            result.put("success", true);
            result.put("message", "收藏成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{postId}/collect")
    public Map<String, Object> uncollectPost(@PathVariable Long postId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            postService.uncollectPost(userId, postId);
            result.put("success", true);
            result.put("message", "取消收藏成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/{postId}/dislike")
    public Map<String, Object> dislikePost(@PathVariable Long postId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            postService.dislikePost(userId, postId);
            result.put("success", true);
            result.put("message", "踩成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{postId}/dislike")
    public Map<String, Object> undislikePost(@PathVariable Long postId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            postService.undislikePost(userId, postId);
            result.put("success", true);
            result.put("message", "取消踩成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/{postId}/comments")
    public Map<String, Object> addComment(@PathVariable Long postId, @RequestParam String content, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            postService.addComment(userId, postId, content);
            result.put("success", true);
            result.put("message", "评论成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/comments/{commentId}")
    public Map<String, Object> deleteComment(@PathVariable Long commentId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            postService.deleteComment(userId, commentId);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getUserPosts(@PathVariable Long userId) {
        return postService.getUserPosts(userId);
    }

    @GetMapping("/liked/{userId}")
    public List<Map<String, Object>> getUserLikedPosts(@PathVariable Long userId) {
        return postService.getUserLikedPosts(userId);
    }
}