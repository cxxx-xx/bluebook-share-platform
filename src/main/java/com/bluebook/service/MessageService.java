package com.bluebook.service;

import com.bluebook.model.Message;
import com.bluebook.model.User;
import com.bluebook.repository.MessageRepository;
import com.bluebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 私信服务类
 * 
 * 功能模块：
 * 1. 消息发送 - sendMessage, canSendMessage
 * 2. 会话管理 - getConversation, getConversations
 * 3. 未读消息 - getUnreadCount, markMessagesAsRead
 * 
 * 规则：互关前最多发送3条消息，互关后无限制
 */
@Service
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowService followService;

    @Autowired
    private NotificationService notificationService;

    /** 互关前最大消息数量 */
    private static final int MAX_MESSAGES_BEFORE_MUTUAL = 3;

    // ==================== 消息发送模块 ====================

    /**
     * 发送私信
     * 互关前最多发送3条消息，互关后无限制
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 消息内容
     */
    public void sendMessage(Long senderId, Long receiverId, String content) {
        // 检查是否可以发送消息
        if (!followService.isMutualFollow(senderId, receiverId)) {
            int sentCount = messageRepository.countMessagesBetweenUsers(senderId, receiverId);
            if (sentCount >= MAX_MESSAGES_BEFORE_MUTUAL) {
                throw new RuntimeException("互关前最多发送" + MAX_MESSAGES_BEFORE_MUTUAL + "条消息");
            }
        }

        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("发送者不存在"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("接收者不存在"));

        // 创建消息
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        messageRepository.save(message);
        
        // 发送通知
        notificationService.notifyMessage(receiverId, senderId, sender.getUsername());
    }

    /**
     * 检查是否可以发送消息
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @return 是否可以发送
     */
    public boolean canSendMessage(Long senderId, Long receiverId) {
        if (followService.isMutualFollow(senderId, receiverId)) {
            return true;
        }
        int sentCount = messageRepository.countMessagesBetweenUsers(senderId, receiverId);
        return sentCount < MAX_MESSAGES_BEFORE_MUTUAL;
    }

    /**
     * 获取已发送消息数量
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @return 已发送数量
     */
    public int getSentCount(Long senderId, Long receiverId) {
        return messageRepository.countMessagesBetweenUsers(senderId, receiverId);
    }

    // ==================== 会话管理模块 ====================

    /**
     * 获取与某用户的聊天记录
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 消息列表
     */
    public List<Map<String, Object>> getConversation(Long userId1, Long userId2) {
        List<Message> messages = messageRepository.findConversation(userId1, userId2);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Message message : messages) {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", message.getId());
            messageMap.put("content", message.getContent());
            messageMap.put("isRead", message.isRead());
            messageMap.put("createdAt", message.getCreatedAt());
            
            Map<String, Object> senderMap = new HashMap<>();
            senderMap.put("id", message.getSender().getId());
            senderMap.put("username", message.getSender().getUsername());
            senderMap.put("avatar", message.getSender().getAvatar());
            messageMap.put("sender", senderMap);
            
            result.add(messageMap);
        }
        
        // 标记消息为已读
        markMessagesAsRead(userId1, userId2);
        return result;
    }

    /**
     * 获取所有会话列表
     * 包含互关用户（无论是否有消息记录）和有消息记录的非互关用户
     * @param userId 用户ID
     * @return 会话列表
     */
    public List<Map<String, Object>> getConversations(Long userId) {
        Map<Long, Map<String, Object>> conversationMap = new HashMap<>();
        
        // 1. 获取互关用户列表
        List<Map<String, Object>> mutualFollows = followService.getMutualFollows(userId);
        for (Map<String, Object> mutual : mutualFollows) {
            Long otherUserId = (Long) mutual.get("id");
            Map<String, Object> conv = new HashMap<>();
            conv.put("id", otherUserId);
            conv.put("username", mutual.get("username"));
            conv.put("avatar", mutual.get("avatar"));
            conv.put("isMutualFollow", true);
            conv.put("lastMessage", "");
            conv.put("lastMessageTime", null);
            conversationMap.put(otherUserId, conv);
        }
        
        // 2. 获取有消息记录的用户
        List<Long> conversationUserIds = messageRepository.findConversationUserIds(userId);
        for (Long otherUserId : conversationUserIds) {
            User user = userRepository.findById(otherUserId).orElse(null);
            if (user == null) continue;
            
            boolean isMutual = followService.isMutualFollow(userId, otherUserId);
            
            // 如果已经在互关列表中，更新消息信息
            if (conversationMap.containsKey(otherUserId)) {
                Map<String, Object> conv = conversationMap.get(otherUserId);
                List<Message> messages = messageRepository.findConversation(userId, otherUserId);
                if (!messages.isEmpty()) {
                    Message lastMessage = messages.get(messages.size() - 1);
                    conv.put("lastMessage", lastMessage.getContent());
                    conv.put("lastMessageTime", lastMessage.getCreatedAt());
                }
            } else {
                // 非互关但有消息记录的用户
                Map<String, Object> conv = new HashMap<>();
                conv.put("id", user.getId());
                conv.put("username", user.getUsername());
                conv.put("avatar", user.getAvatar());
                conv.put("isMutualFollow", isMutual);
                
                List<Message> messages = messageRepository.findConversation(userId, otherUserId);
                if (!messages.isEmpty()) {
                    Message lastMessage = messages.get(messages.size() - 1);
                    conv.put("lastMessage", lastMessage.getContent());
                    conv.put("lastMessageTime", lastMessage.getCreatedAt());
                }
                conversationMap.put(otherUserId, conv);
            }
        }
        
        return new ArrayList<>(conversationMap.values());
    }

    // ==================== 未读消息模块 ====================

    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读数量
     */
    public int getUnreadCount(Long userId) {
        return messageRepository.countUnreadByReceiver(userId);
    }

    /**
     * 标记消息为已读
     * @param userId1 用户1ID（接收者）
     * @param userId2 用户2ID（发送者）
     */
    public void markMessagesAsRead(Long userId1, Long userId2) {
        List<Message> messages = messageRepository.findConversation(userId1, userId2);
        for (Message message : messages) {
            if (message.getReceiver().getId().equals(userId1) && !message.isRead()) {
                message.setRead(true);
            }
        }
        messageRepository.saveAll(messages);
    }
}
