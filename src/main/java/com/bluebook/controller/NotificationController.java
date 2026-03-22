package com.bluebook.controller;

import com.bluebook.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 * 
 * API端点：
 * - GET /api/notifications - 获取用户通知列表
 * - GET /api/notifications/unread-count - 获取未读通知数量
 * - POST /api/notifications/{notificationId}/read - 标记通知为已读
 * - POST /api/notifications/read-all - 标记所有通知为已读
 * - DELETE /api/notifications/{notificationId} - 删除通知
 * - POST /api/notifications/system - 发送系统通知（管理员）
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Map<String, Object> getNotifications(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            result.put("success", true);
            result.put("notifications", notificationService.getUserNotifications(userId));
            result.put("unreadCount", notificationService.getUnreadCount(userId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            result.put("success", true);
            result.put("count", notificationService.getUnreadCount(userId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PutMapping("/{id}/read")
    public Map<String, Object> markAsRead(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            notificationService.markAsRead(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PutMapping("/read-all")
    public Map<String, Object> markAllAsRead(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            notificationService.markAllAsRead(userId);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteNotification(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            notificationService.deleteNotification(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
