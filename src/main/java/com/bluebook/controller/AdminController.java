package com.bluebook.controller;

import com.bluebook.model.Post;
import com.bluebook.model.User;
import com.bluebook.model.Notification;
import com.bluebook.model.Comment;
import com.bluebook.repository.PostRepository;
import com.bluebook.repository.UserRepository;
import com.bluebook.repository.NotificationRepository;
import com.bluebook.repository.CommentRepository;
import com.bluebook.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 管理员控制器
 * 
 * API端点：
 * - GET /api/admin/users - 获取所有用户列表
 * - GET /api/admin/posts - 获取所有帖子列表
 * - POST /api/admin/user - 创建用户
 * - PUT /api/admin/user/{userId} - 更新用户信息
 * - DELETE /api/admin/user/{userId} - 删除用户
 * - DELETE /api/admin/post/{postId} - 删除帖子
 * - POST /api/admin/notification - 发送系统通知
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CommentRepository commentRepository;

    private boolean checkAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return false;
        User user = userService.getUserById(userId);
        return user != null && user.isAdmin();
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }

        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        List<User> allUsers = userRepository.findAll();
        List<Post> allPosts = postRepository.findAll();

        long newUsersToday = allUsers.stream()
                .filter(u -> u.getCreatedAt().isAfter(todayStart))
                .count();

        long newPostsToday = allPosts.stream()
                .filter(p -> p.getCreatedAt().isAfter(todayStart))
                .count();

        result.put("totalUsers", totalUsers);
        result.put("totalPosts", totalPosts);
        result.put("newUsersToday", newUsersToday);
        result.put("newPostsToday", newPostsToday);

        return result;
    }

    @GetMapping("/users")
    public Map<String, Object> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }

        Sort sort = order.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);

        result.put("users", userPage.getContent());
        result.put("totalElements", userPage.getTotalElements());
        result.put("totalPages", userPage.getTotalPages());
        result.put("currentPage", page);

        return result;
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            result.put("error", "用户不存在");
            return result;
        }

        if (user.isAdmin()) {
            result.put("error", "不能删除管理员账户");
            return result;
        }

        userRepository.delete(user);
        result.put("success", true);
        result.put("message", "用户已删除");

        return result;
    }

    @PutMapping("/users/{id}/admin")
    public Map<String, Object> toggleAdmin(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            result.put("error", "用户不存在");
            return result;
        }

        user.setAdmin(!user.isAdmin());
        userRepository.save(user);
        
        result.put("success", true);
        result.put("isAdmin", user.isAdmin());
        result.put("message", user.isAdmin() ? "已设为管理员" : "已取消管理员");

        return result;
    }

    @GetMapping("/posts")
    public Map<String, Object> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }

        Sort sort = order.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> postPage = postRepository.findAll(pageable);

        result.put("posts", postPage.getContent());
        result.put("totalElements", postPage.getTotalElements());
        result.put("totalPages", postPage.getTotalPages());
        result.put("currentPage", page);

        return result;
    }

    @DeleteMapping("/posts/{id}")
    public Map<String, Object> deletePost(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            result.put("error", "帖子不存在");
            return result;
        }

        postRepository.delete(post);
        result.put("success", true);
        result.put("message", "帖子已删除");

        return result;
    }

    @GetMapping("/search/users")
    public Map<String, Object> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }

        List<User> allUsers = userRepository.findAll();
        List<User> filtered = allUsers.stream()
                .filter(u -> u.getUsername().contains(keyword) || 
                            u.getPhone().contains(keyword))
                .toList();

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        
        result.put("users", filtered.subList(start, end));
        result.put("totalElements", filtered.size());
        result.put("totalPages", (int) Math.ceil((double) filtered.size() / size));
        result.put("currentPage", page);

        return result;
    }
    
    @PostMapping("/users")
    public Map<String, Object> createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String phone,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }
        
        if (userRepository.existsByUsername(username)) {
            result.put("error", "用户名已存在");
            return result;
        }
        
        if (userRepository.existsByPhone(phone)) {
            result.put("error", "手机号已被注册");
            return result;
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setAdmin(false);
        userService.adminCreateUser(user);
        
        result.put("success", true);
        result.put("message", "用户创建成功");
        
        return result;
    }
    
    @PutMapping("/user/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            result.put("error", "用户不存在");
            return result;
        }

        if (userData.containsKey("username")) {
            user.setUsername((String) userData.get("username"));
        }
        if (userData.containsKey("phone")) {
            user.setPhone((String) userData.get("phone"));
        }
        if (userData.containsKey("level")) {
            user.setLevel(((Number) userData.get("level")).intValue());
        }
        if (userData.containsKey("fame")) {
            user.setFame(((Number) userData.get("fame")).longValue());
        }
        if (userData.containsKey("password") && userData.get("password") != null && !((String) userData.get("password")).isEmpty()) {
            user.setPassword((String) userData.get("password"));
        }

        userRepository.save(user);
        
        result.put("success", true);
        result.put("message", "用户信息已更新");
        result.put("user", user);

        return result;
    }

    @PostMapping("/notification")
    public Map<String, Object> sendNotification(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "all") String sendType,
            @RequestParam(required = false) String userIds,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }
        
        if (title == null || title.trim().isEmpty()) {
            result.put("error", "标题不能为空");
            return result;
        }
        
        if (content == null || content.trim().isEmpty()) {
            result.put("error", "内容不能为空");
            return result;
        }
        
        List<User> targetUsers;
        
        if ("all".equals(sendType)) {
            targetUsers = userRepository.findAll();
        } else if ("selected".equals(sendType) && userIds != null && !userIds.isEmpty()) {
            List<Long> ids = Arrays.stream(userIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            targetUsers = userRepository.findAllById(ids);
        } else {
            result.put("error", "请选择发送对象");
            return result;
        }
        
        int sentCount = 0;
        for (User user : targetUsers) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType("SYSTEM");
            notification.setTitle(title);
            notification.setContent(content);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            notificationRepository.save(notification);
            sentCount++;
        }
        
        result.put("success", true);
        result.put("message", "通知发送成功");
        result.put("sentCount", sentCount);
        
        return result;
    }

    @GetMapping("/admins")
    public Map<String, Object> getAdmins(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }
        
        List<User> admins = userRepository.findByIsAdminTrue();
        result.put("admins", admins);
        
        return result;
    }

    @PostMapping("/admins")
    public Map<String, Object> createAdmin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String phone,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }
        
        if (userRepository.existsByUsername(username)) {
            result.put("error", "用户名已存在");
            return result;
        }
        
        if (userRepository.existsByPhone(phone)) {
            result.put("error", "手机号已被注册");
            return result;
        }
        
        userService.addAdmin(username, password, phone);
        
        result.put("success", true);
        result.put("message", "管理员创建成功");
        
        return result;
    }

    @DeleteMapping("/admins/{id}")
    public Map<String, Object> deleteAdmin(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }
        
        Long currentUserId = (Long) request.getSession().getAttribute("userId");
        if (id.equals(currentUserId)) {
            result.put("error", "不能删除自己的管理员权限");
            return result;
        }
        
        User admin = userRepository.findById(id).orElse(null);
        if (admin == null) {
            result.put("error", "用户不存在");
            return result;
        }
        
        admin.setAdmin(false);
        userRepository.save(admin);
        
        result.put("success", true);
        result.put("message", "已移除管理员权限");
        
        return result;
    }

    @GetMapping("/comments")
    public Map<String, Object> getComments(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }
        
        List<Comment> comments = commentRepository.findAllByOrderByCreatedAtDesc();
        result.put("comments", comments);
        
        return result;
    }

    @DeleteMapping("/comments/{id}")
    public Map<String, Object> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (!checkAdmin(request)) {
            result.put("error", "无权限");
            return result;
        }
        
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment == null) {
            result.put("error", "评论不存在");
            return result;
        }
        
        Post post = comment.getPost();
        if (post != null) {
            post.setCommentsCount(post.getCommentsCount() - 1);
            postRepository.save(post);
        }
        
        commentRepository.delete(comment);
        
        result.put("success", true);
        result.put("message", "评论已删除");
        
        return result;
    }
}
