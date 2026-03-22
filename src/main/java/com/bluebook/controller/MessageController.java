package com.bluebook.controller;

import com.bluebook.service.MessageService;
import com.bluebook.service.FollowService;
import com.bluebook.model.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信控制器
 * 
 * API端点：
 * - POST /api/messages/send - 发送私信
 * - GET /api/messages/conversation/{userId} - 获取与某用户的聊天记录
 * - GET /api/messages/conversations - 获取所有会话列表
 * - GET /api/messages/unread-count - 获取未读消息数量
 * - GET /api/messages/can-send/{userId} - 检查是否可以发送消息
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private FollowService followService;

    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestParam Long receiverId, @RequestParam String content, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long senderId = (Long) session.getAttribute("userId");
            if (senderId == null) {
                throw new RuntimeException("请先登录");
            }

            if (!messageService.canSendMessage(senderId, receiverId)) {
                boolean isMutual = followService.isMutualFollow(senderId, receiverId);
                if (isMutual) {
                    throw new RuntimeException("系统错误");
                } else {
                    throw new RuntimeException("互关前最多发送3条消息，请先互相关注");
                }
            }

            messageService.sendMessage(senderId, receiverId, content);
            result.put("success", true);
            result.put("message", "发送成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/conversation/{userId}")
    public Map<String, Object> getConversation(@PathVariable Long userId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) session.getAttribute("userId");
            if (currentUserId == null) {
                throw new RuntimeException("请先登录");
            }

            messageService.markMessagesAsRead(currentUserId, userId);
            
            result.put("success", true);
            result.put("messages", messageService.getConversation(currentUserId, userId));
            result.put("isMutualFollow", followService.isMutualFollow(currentUserId, userId));
            result.put("remainingMessages", messageService.canSendMessage(currentUserId, userId) ? 
                (followService.isMutualFollow(currentUserId, userId) ? -1 : 
                3 - messageService.getSentCount(currentUserId, userId)) : 0);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/conversations")
    public Map<String, Object> getConversations(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new RuntimeException("请先登录");
            }
            result.put("success", true);
            result.put("conversations", messageService.getConversations(userId));
            result.put("unreadCount", messageService.getUnreadCount(userId));
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
            result.put("count", messageService.getUnreadCount(userId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/can-send/{receiverId}")
    public Map<String, Object> canSendMessage(@PathVariable Long receiverId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long senderId = (Long) session.getAttribute("userId");
            if (senderId == null) {
                throw new RuntimeException("请先登录");
            }
            
            boolean canSend = messageService.canSendMessage(senderId, receiverId);
            boolean isMutual = followService.isMutualFollow(senderId, receiverId);
            int sentCount = messageService.getSentCount(senderId, receiverId);
            
            result.put("success", true);
            result.put("canSend", canSend);
            result.put("isMutualFollow", isMutual);
            result.put("sentCount", sentCount);
            result.put("remainingMessages", isMutual ? -1 : (3 - sentCount));
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
