package com.bluebook.service;

import com.bluebook.model.Follow;
import com.bluebook.model.User;
import com.bluebook.repository.FollowRepository;
import com.bluebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;

/**
 * 关注服务类
 * 
 * 功能模块：
 * 1. 关注状态检查 - isFollowing, isMutualFollow
 * 2. 关注操作 - follow, unfollow
 * 3. 粉丝/关注列表 - getFollowers, getFollowing
 */
@Service
public class FollowService {
    
    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    // ==================== 关注状态检查模块 ====================

    /**
     * 检查是否关注了某用户
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 是否已关注
     */
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.findByFollowerIdAndFollowedId(followerId, followedId).isPresent();
    }

    /**
     * 检查是否互相关注
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 是否互关
     */
    public boolean isMutualFollow(Long userId1, Long userId2) {
        return isFollowing(userId1, userId2) && isFollowing(userId2, userId1);
    }

    /**
     * 获取互相关注的用户列表
     * @param userId 用户ID
     * @return 互关用户信息列表
     */
    public List<Map<String, Object>> getMutualFollows(Long userId) {
        List<Map<String, Object>> following = getFollowing(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> user : following) {
            Long otherUserId = (Long) user.get("id");
            if (isFollowing(otherUserId, userId)) {
                result.add(user);
            }
        }
        return result;
    }

    // ==================== 关注操作模块 ====================

    /**
     * 关注用户
     * 更新双方的关注/粉丝计数，发送通知
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    public void follow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new RuntimeException("不能关注自己");
        }

        if (isFollowing(followerId, followedId)) {
            throw new RuntimeException("已经关注过了");
        }

        User follower = userRepository.findById(followerId).orElseThrow(() -> new RuntimeException("用户不存在"));
        User followed = userRepository.findById(followedId).orElseThrow(() -> new RuntimeException("目标用户不存在"));

        // 创建关注记录
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        followRepository.save(follow);

        // 更新关注数和粉丝数
        follower.setFollowCount(follower.getFollowCount() + 1);
        followed.setFansCount(followed.getFansCount() + 1);
        userRepository.save(follower);
        userRepository.save(followed);
        
        // 发送通知
        notificationService.notifyFollow(followedId, followerId, follower.getUsername());
    }

    /**
     * 取消关注
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    public void unfollow(Long followerId, Long followedId) {
        Follow follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .orElseThrow(() -> new RuntimeException("未关注该用户"));

        User follower = follow.getFollower();
        User followed = follow.getFollowed();

        followRepository.delete(follow);

        // 更新关注数和粉丝数
        follower.setFollowCount(Math.max(0, follower.getFollowCount() - 1));
        followed.setFansCount(Math.max(0, followed.getFansCount() - 1));
        userRepository.save(follower);
        userRepository.save(followed);
    }

    // ==================== 粉丝/关注列表模块 ====================

    /**
     * 获取粉丝列表
     * @param userId 用户ID
     * @return 粉丝信息列表
     */
    public List<Map<String, Object>> getFollowers(Long userId) {
        List<Follow> follows = followRepository.findByFollowedId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Follow follow : follows) {
            Map<String, Object> userMap = new HashMap<>();
            User follower = follow.getFollower();
            userMap.put("id", follower.getId());
            userMap.put("username", follower.getUsername());
            userMap.put("avatar", follower.getAvatar());
            userMap.put("level", follower.getLevel());
            userMap.put("fame", follower.getFame());
            result.add(userMap);
        }
        return result;
    }

    /**
     * 获取关注列表
     * @param userId 用户ID
     * @return 关注用户信息列表
     */
    public List<Map<String, Object>> getFollowing(Long userId) {
        List<Follow> follows = followRepository.findByFollowerId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Follow follow : follows) {
            Map<String, Object> userMap = new HashMap<>();
            User followed = follow.getFollowed();
            userMap.put("id", followed.getId());
            userMap.put("username", followed.getUsername());
            userMap.put("avatar", followed.getAvatar());
            userMap.put("level", followed.getLevel());
            userMap.put("fame", followed.getFame());
            result.add(userMap);
        }
        return result;
    }
}
