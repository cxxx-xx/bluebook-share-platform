package com.bluebook.service;

import com.bluebook.model.Notification;
import com.bluebook.model.User;
import com.bluebook.repository.NotificationRepository;
import com.bluebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 通知服务类
 * 
 * 功能模块：
 * 1. 通知创建 - createNotification
 * 2. 各类通知 - notifyLike, notifyCollect, notifyComment, notifyFollow, notifyMessage, notifySystem
 * 3. 批量通知 - notifyAllUsers
 * 4. 通知查询 - getUserNotifications, getUnreadCount
 * 5. 通知状态管理 - markAsRead, markAllAsRead, deleteNotification
 * 
 * 通知类型：
 * - LIKE: 点赞通知
 * - COLLECT: 收藏通知
 * - COMMENT: 评论通知
 * - FOLLOW: 关注通知
 * - MESSAGE: 私信通知
 * - SYSTEM: 系统通知
 */
@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== 通知创建模块 ====================

    /**
     * 创建通知
     * @param userId 用户ID
     * @param type 通知类型
     * @param title 标题
     * @param content 内容
     * @param relatedId 关联ID（帖子ID等）
     */
    public void createNotification(Long userId, String type, String title, String content, Long relatedId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notificationRepository.save(notification);
    }

    /**
     * 创建通知（带关联用户ID）
     */
    public void createNotification(Long userId, String type, String title, String content, Long relatedId, Long relatedUserId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedUserId(relatedUserId);
        notificationRepository.save(notification);
    }

    // ==================== 各类通知模块 ====================

    /**
     * 发送点赞通知
     */
    public void notifyLike(Long postAuthorId, Long postId, String postTitle, String likerName) {
        createNotification(postAuthorId, "LIKE", "收到新的点赞", 
            likerName + " 赞了你的帖子《" + postTitle + "》", postId);
    }

    /**
     * 发送收藏通知
     */
    public void notifyCollect(Long postAuthorId, Long postId, String postTitle, String collectorName) {
        createNotification(postAuthorId, "COLLECT", "收到新的收藏", 
            collectorName + " 收藏了你的帖子《" + postTitle + "》", postId);
    }

    /**
     * 发送评论通知
     */
    public void notifyComment(Long postAuthorId, Long postId, String postTitle, String commenterName, String commentContent) {
        createNotification(postAuthorId, "COMMENT", "收到新的评论", 
            commenterName + " 评论了《" + postTitle + "》：" + commentContent, postId);
    }

    /**
     * 发送关注通知
     */
    public void notifyFollow(Long followedUserId, Long followerId, String followerName) {
        createNotification(followedUserId, "FOLLOW", "有新的粉丝", 
            followerName + " 关注了你", null, followerId);
    }

    /**
     * 发送私信通知
     */
    public void notifyMessage(Long receiverId, Long senderId, String senderName) {
        createNotification(receiverId, "MESSAGE", "收到新私信", 
            senderName + " 给你发送了一条私信", null, senderId);
    }

    /**
     * 发送系统通知
     */
    public void notifySystem(Long userId, String title, String content) {
        createNotification(userId, "SYSTEM", title, content, null);
    }

    // ==================== 批量通知模块 ====================

    /**
     * 向所有用户发送系统通知
     */
    public void notifyAllUsers(String title, String content) {
        List<User> users = userRepository.findByIsAdminFalse();
        for (User user : users) {
            createNotification(user.getId(), "SYSTEM", title, content, null);
        }
    }

    // ==================== 通知查询模块 ====================

    /**
     * 获取用户通知列表
     */
    public List<Map<String, Object>> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Notification notification : notifications) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", notification.getId());
            map.put("type", notification.getType());
            map.put("title", notification.getTitle());
            map.put("content", notification.getContent());
            map.put("relatedId", notification.getRelatedId());
            map.put("relatedUserId", notification.getRelatedUserId());
            map.put("isRead", notification.isRead());
            map.put("createdAt", notification.getCreatedAt());
            result.add(map);
        }
        return result;
    }

    /**
     * 获取未读通知数量
     */
    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    // ==================== 通知状态管理模块 ====================

    /**
     * 标记通知为已读
     */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    /**
     * 标记所有通知为已读
     */
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findUnreadByUserId(userId);
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    /**
     * 删除通知
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
