 package com.bluebook.controller;

import com.bluebook.model.User;
import com.bluebook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * 用户控制器
 * 
 * API端点：
 * - POST /api/users/register - 用户注册
 * - POST /api/users/login - 用户登录
 * - POST /api/users/send-sms - 发送短信验证码
 * - POST /api/users/avatar - 上传头像
 * - GET /api/users/captcha - 获取图形验证码
 * - POST /api/users/verify-captcha - 验证图形验证码
 * - GET /api/users/check-login - 检查登录状态
 * - GET /api/users/info/{userId} - 获取用户信息
 * - GET /api/users/collections - 获取用户收藏
 * - POST /api/users/logout - 退出登录
 * - POST /api/users/admin/user - 管理员创建用户
 * - PUT /api/users/admin/user/{userId} - 管理员更新用户
 * - DELETE /api/users/admin/user/{userId} - 管理员删除用户
 * - GET /api/users/admin/users - 获取所有用户列表
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user, @RequestParam String code) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.register(user, code);
            result.put("success", true);
            result.put("message", "注册成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username, @RequestParam String password, @RequestParam String captcha, @RequestParam(defaultValue = "false") boolean remember, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            HttpSession session = request.getSession();
            String sessionCaptcha = (String) session.getAttribute("captcha");
            User user = userService.login(username, password, captcha, sessionCaptcha);
            session.setAttribute("userId", user.getId());
            
            // 设置记住我功能：7天免登录
            if (remember) {
                session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 7天
            } else {
                session.setMaxInactiveInterval(30 * 60); // 30分钟
            }
            
            result.put("success", true);
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("isAdmin", String.valueOf(user.isAdmin()));
        } catch (Exception e) {
            result.put("success", false);
                result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/send-sms")
    public Map<String, Object> sendSms(@RequestParam String phone) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.sendSmsCode(phone);
            result.put("success", true);
            result.put("message", "短信发送成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/avatar")
    public String uploadAvatar(@RequestParam Long userId, @RequestParam MultipartFile file) throws IOException {
        String path = "f:\\1\\bluebook\\src\\main\\resources\\static\\avatars\\";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + "." + file.getOriginalFilename().split("\\.")[1];
        file.transferTo(new File(path + fileName));

        // 更新用户头像
        userService.updateAvatar(userId, fileName);

        return "头像上传成功";
    }

    @PostMapping("/add-admin")
    public String addAdmin(@RequestParam String username, @RequestParam String password, @RequestParam String phone) {
        userService.addAdmin(username, password, phone);
        return "管理员添加成功";
    }

    @GetMapping("/captcha")
    public String getCaptcha(HttpServletRequest request) {
        // 生成点击验证的挑战
        Random random = new Random();
        // 生成4个随机位置的挑战点，确保不重复
        Set<String> usedPositions = new HashSet<>();
        int[][] positions = new int[4][2];
        
        for (int i = 0; i < 4; i++) {
            int row, col;
            do {
                row = random.nextInt(4); // 0-3
                col = random.nextInt(4); // 0-3
            } while (usedPositions.contains(row + "," + col));
            
            positions[i][0] = row;
            positions[i][1] = col;
            usedPositions.add(row + "," + col);
        }
        
        // 保存答案到session
        HttpSession session = request.getSession();
        session.setAttribute("captcha", positions[0][0] + "," + positions[0][1] + "," + 
                                          positions[1][0] + "," + positions[1][1] + "," + 
                                          positions[2][0] + "," + positions[2][1] + "," + 
                                          positions[3][0] + "," + positions[3][1]);
        
        // 返回挑战点位置
        return positions[0][0] + "," + positions[0][1] + "," + 
               positions[1][0] + "," + positions[1][1] + "," + 
               positions[2][0] + "," + positions[2][1] + "," + 
               positions[3][0] + "," + positions[3][1];
    }
    
    @PostMapping("/verify-captcha")
    public String verifyCaptcha(@RequestParam String userInput, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String correctAnswer = (String) session.getAttribute("captcha");
        
        if (correctAnswer != null && correctAnswer.equals(userInput)) {
            // 验证成功
            return "success";
        } else {
            // 验证失败
            return "failure";
        }
    }
    
    @GetMapping("/check-login")
    public Map<String, Object> checkLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();
        
        if (session == null) {
            result.put("error", "未登录");
            return result;
        }
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            result.put("error", "未登录");
            return result;
        }
        
        User user = userService.getUserById(userId);
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("avatar", user.getAvatar());
        result.put("level", user.getLevel());
        result.put("isAdmin", String.valueOf(user.isAdmin()));
        return result;
    }
    
    @GetMapping("/info/{userId}")
    public Map<String, Object> getUserInfo(@PathVariable Long userId) {
        return userService.getUserInfo(userId);
    }
    
    @GetMapping("/collections")
    public Map<String, Object> getUserCollections(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                throw new RuntimeException("请先登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            result.put("success", true);
            result.put("collections", userService.getUserCollections(userId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/debug/users")
    public Map<String, Object> debugUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("users", userService.getAllUsers());
        return result;
    }
    
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        return "退出成功";
    }
    
    @PostMapping("/admin/user")
    public Map<String, Object> adminCreateUser(@RequestBody User user, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                throw new RuntimeException("请先登录");
            }
            Long adminId = (Long) session.getAttribute("userId");
            if (adminId == null) {
                throw new RuntimeException("请先登录");
            }
            User admin = userService.getUserById(adminId);
            if (admin == null || !admin.isAdmin()) {
                throw new RuntimeException("无权限");
            }
            
            userService.adminCreateUser(user);
            result.put("success", true);
            result.put("message", "用户创建成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    @PutMapping("/admin/user/{userId}")
    public Map<String, Object> adminUpdateUser(@PathVariable Long userId, @RequestBody User user, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                throw new RuntimeException("请先登录");
            }
            Long adminId = (Long) session.getAttribute("userId");
            if (adminId == null) {
                throw new RuntimeException("请先登录");
            }
            User admin = userService.getUserById(adminId);
            if (admin == null || !admin.isAdmin()) {
                throw new RuntimeException("无权限");
            }
            
            userService.adminUpdateUser(userId, user);
            result.put("success", true);
            result.put("message", "用户更新成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    @DeleteMapping("/admin/user/{userId}")
    public Map<String, Object> adminDeleteUser(@PathVariable Long userId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                throw new RuntimeException("请先登录");
            }
            Long adminId = (Long) session.getAttribute("userId");
            if (adminId == null) {
                throw new RuntimeException("请先登录");
            }
            User admin = userService.getUserById(adminId);
            if (admin == null || !admin.isAdmin()) {
                throw new RuntimeException("无权限");
            }
            
            userService.adminDeleteUser(userId);
            result.put("success", true);
            result.put("message", "用户删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/admin/users")
    public Map<String, Object> adminGetAllUsers(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                throw new RuntimeException("请先登录");
            }
            Long adminId = (Long) session.getAttribute("userId");
            if (adminId == null) {
                throw new RuntimeException("请先登录");
            }
            User admin = userService.getUserById(adminId);
            if (admin == null || !admin.isAdmin()) {
                throw new RuntimeException("无权限");
            }
            
            result.put("success", true);
            result.put("users", userService.getAllUsersInfo());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}