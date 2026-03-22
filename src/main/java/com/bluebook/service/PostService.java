package com.bluebook.service;

import com.bluebook.model.Post;
import com.bluebook.model.User;
import com.bluebook.model.Like;
import com.bluebook.model.Collect;
import com.bluebook.model.Dislike;
import com.bluebook.model.Comment;
import com.bluebook.repository.PostRepository;
import com.bluebook.repository.UserRepository;
import com.bluebook.repository.LikeRepository;
import com.bluebook.repository.CollectRepository;
import com.bluebook.repository.DislikeRepository;
import com.bluebook.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 帖子服务类
 * 
 * 功能模块：
 * 1. 帖子管理 - createPost, getPostById, getUserPosts
 * 2. 帖子列表 - getAllPosts, searchPosts, getHotPosts, getRecommendedPosts, getPostsByCategory
 * 3. 点赞功能 - likePost, unlikePost
 * 4. 收藏功能 - collectPost, uncollectPost
 * 5. 踩功能 - dislikePost, undislikePost
 * 6. 评论功能 - addComment, deleteComment
 */
@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CollectRepository collectRepository;

    @Autowired
    private DislikeRepository dislikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RankingService rankingService;

    // ==================== 帖子管理模块 ====================

    /**
     * 创建帖子
     * 发布新帖子，更新用户的帖子计数和知名度
     * @param post 帖子实体
     */
    public void createPost(Post post) {
        Long userId = post.getUser().getId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        post.setUser(user);
        postRepository.save(post);
        user.setPostCount(user.getPostCount() + 1);
        userService.updateFame(user);
    }

    /**
     * 获取帖子详情
     * 同时增加帖子作者的搜索计数，并检查当前用户的交互状态
     * @param postId 帖子ID
     * @param currentUserId 当前用户ID
     * @return 帖子详情信息
     */
    public Map<String, Object> getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("帖子不存在"));
        
        // 增加搜索计数
        rankingService.incrementSearchCount(post.getUser().getId(), post.getCategory());
        
        Map<String, Object> result = convertPostToMap(post);
        
        // 检查当前用户的交互状态
        if (currentUserId != null) {
            result.put("isLiked", likeRepository.findByUserIdAndPostId(currentUserId, postId).isPresent());
            result.put("isCollected", collectRepository.findByUserIdAndPostId(currentUserId, postId).isPresent());
            result.put("isDisliked", dislikeRepository.findByUserIdAndPostId(currentUserId, postId).isPresent());
        } else {
            result.put("isLiked", false);
            result.put("isCollected", false);
            result.put("isDisliked", false);
        }
        
        // 获取评论列表
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
        List<Map<String, Object>> commentList = new ArrayList<>();
        for (Comment comment : comments) {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("id", comment.getId());
            commentMap.put("content", comment.getContent());
            commentMap.put("createdAt", comment.getCreatedAt());
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", comment.getUser().getId());
            userMap.put("username", comment.getUser().getUsername());
            userMap.put("avatar", comment.getUser().getAvatar());
            commentMap.put("user", userMap);
            commentList.add(commentMap);
        }
        result.put("comments", commentList);
        
        return result;
    }

    /**
     * 获取用户发布的帖子列表
     * @param userId 用户ID
     * @return 帖子列表
     */
    public List<Map<String, Object>> getUserPosts(Long userId) {
        List<Post> posts = postRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Post post : posts) {
            result.add(convertPostToMap(post));
        }
        return result;
    }

    // ==================== 帖子列表模块 ====================

    /**
     * 获取所有帖子（分页）
     * @param page 页码
     * @param size 每页数量
     * @return 帖子列表
     */
    public Map<String, Object> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return convertToResponse(postPage);
    }

    /**
     * 搜索帖子
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @return 搜索结果
     */
    public Map<String, Object> searchPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.searchByKeyword(keyword, pageable);
        return convertToResponse(postPage);
    }

    /**
     * 获取热门帖子
     * 按点赞数+收藏数排序
     * @param page 页码
     * @param size 每页数量
     * @return 热门帖子列表
     */
    public Map<String, Object> getHotPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findHotPosts(pageable);
        return convertToResponse(postPage);
    }

    /**
     * 获取推荐帖子
     * 综合热度、时间等因素
     * @param page 页码
     * @param size 每页数量
     * @return 推荐帖子列表
     */
    public Map<String, Object> getRecommendedPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findRecommendedPosts(pageable);
        return convertToResponse(postPage);
    }

    /**
     * 获取分类帖子
     * @param category 分类名称
     * @param page 页码
     * @param size 每页数量
     * @return 分类帖子列表
     */
    public Map<String, Object> getPostsByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        return convertToResponse(postPage);
    }

    // ==================== 点赞功能模块 ====================

    /**
     * 点赞帖子
     * 增加帖子点赞数，更新作者知名度，发送通知
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    public void likePost(Long userId, Long postId) {
        if (likeRepository.findByUserIdAndPostId(userId, postId).isPresent()) {
            throw new RuntimeException("已经点赞过");
        }

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("帖子不存在"));
        User user = post.getUser();
        User liker = userRepository.findById(userId).orElse(null);

        // 创建点赞记录
        Like like = new Like();
        like.setUser(new User());
        like.getUser().setId(userId);
        like.setPost(post);
        likeRepository.save(like);

        // 更新帖子点赞数
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);

        // 更新作者获赞数和知名度
        user.setLikesReceived(user.getLikesReceived() + 1);
        rankingService.incrementFame(user.getId(), 1, post.getCategory());
        userService.updateFame(user);
        
        // 发送通知
        if (liker != null && !user.getId().equals(userId)) {
            notificationService.notifyLike(user.getId(), postId, post.getTitle(), liker.getUsername());
        }
    }

    /**
     * 取消点赞
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    public void unlikePost(Long userId, Long postId) {
        Like like = likeRepository.findByUserIdAndPostId(userId, postId).orElseThrow(() -> new RuntimeException("未点赞"));
        Post post = like.getPost();
        User user = post.getUser();

        likeRepository.delete(like);

        post.setLikesCount(post.getLikesCount() - 1);
        postRepository.save(post);

        user.setLikesReceived(user.getLikesReceived() - 1);
        userService.updateFame(user);
    }

    // ==================== 收藏功能模块 ====================

    /**
     * 收藏帖子
     * 增加帖子收藏数，更新作者知名度，发送通知
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    public void collectPost(Long userId, Long postId) {
        if (collectRepository.findByUserIdAndPostId(userId, postId).isPresent()) {
            throw new RuntimeException("已经收藏过");
        }

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("帖子不存在"));
        User user = post.getUser();
        User collector = userRepository.findById(userId).orElse(null);

        // 创建收藏记录
        Collect collect = new Collect();
        collect.setUser(new User());
        collect.getUser().setId(userId);
        collect.setPost(post);
        collectRepository.save(collect);

        // 更新帖子收藏数
        post.setCollectsCount(post.getCollectsCount() + 1);
        postRepository.save(post);

        // 更新作者被收藏数和知名度
        user.setCollectsReceived(user.getCollectsReceived() + 1);
        rankingService.incrementFame(user.getId(), 2, post.getCategory());
        userService.updateFame(user);
        
        // 发送通知
        if (collector != null && !user.getId().equals(userId)) {
            notificationService.notifyCollect(user.getId(), postId, post.getTitle(), collector.getUsername());
        }
    }

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    public void uncollectPost(Long userId, Long postId) {
        Collect collect = collectRepository.findByUserIdAndPostId(userId, postId).orElseThrow(() -> new RuntimeException("未收藏"));
        Post post = collect.getPost();
        User user = post.getUser();

        collectRepository.delete(collect);

        post.setCollectsCount(post.getCollectsCount() - 1);
        postRepository.save(post);

        user.setCollectsReceived(user.getCollectsReceived() - 1);
        userService.updateFame(user);
    }

    // ==================== 踩功能模块 ====================

    /**
     * 踩帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    public void dislikePost(Long userId, Long postId) {
        if (dislikeRepository.findByUserIdAndPostId(userId, postId).isPresent()) {
            throw new RuntimeException("已经踩过");
        }

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("帖子不存在"));
        User user = post.getUser();

        Dislike dislike = new Dislike();
        dislike.setUser(new User());
        dislike.getUser().setId(userId);
        dislike.setPost(post);
        dislikeRepository.save(dislike);

        post.setDislikesCount(post.getDislikesCount() + 1);
        postRepository.save(post);

        user.setDislikesReceived(user.getDislikesReceived() + 1);
        userService.updateFame(user);
    }

    /**
     * 取消踩
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    public void undislikePost(Long userId, Long postId) {
        Dislike dislike = dislikeRepository.findByUserIdAndPostId(userId, postId).orElseThrow(() -> new RuntimeException("未踩过"));
        Post post = dislike.getPost();
        User user = post.getUser();

        dislikeRepository.delete(dislike);

        post.setDislikesCount(post.getDislikesCount() - 1);
        postRepository.save(post);

        user.setDislikesReceived(user.getDislikesReceived() - 1);
        userService.updateFame(user);
    }

    // ==================== 评论功能模块 ====================

    /**
     * 添加评论
     * @param userId 用户ID
     * @param postId 帖子ID
     * @param content 评论内容
     */
    public void addComment(Long userId, Long postId, String content) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("帖子不存在"));
        User commenter = userRepository.findById(userId).orElse(null);
        
        Comment comment = new Comment();
        comment.setUser(new User());
        comment.getUser().setId(userId);
        comment.setPost(post);
        comment.setContent(content);
        commentRepository.save(comment);
        
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
        
        // 发送通知给帖子作者
        User postAuthor = post.getUser();
        if (commenter != null && !postAuthor.getId().equals(userId)) {
            notificationService.notifyComment(postAuthor.getId(), postId, post.getTitle(), commenter.getUsername(), content);
        }
    }

    /**
     * 删除评论
     * @param userId 用户ID
     * @param commentId 评论ID
     */
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("评论不存在"));
        
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }
        
        Post post = comment.getPost();
        post.setCommentsCount(post.getCommentsCount() - 1);
        postRepository.save(post);
        
        commentRepository.delete(comment);
    }

    // ==================== 辅助方法 ====================

    /**
     * 将分页结果转换为响应格式
     */
    private Map<String, Object> convertToResponse(Page<Post> postPage) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> posts = new ArrayList<>();
        
        for (Post post : postPage.getContent()) {
            posts.add(convertPostToMap(post));
        }
        
        response.put("posts", posts);
        response.put("currentPage", postPage.getNumber());
        response.put("totalPages", postPage.getTotalPages());
        response.put("totalElements", postPage.getTotalElements());
        response.put("hasNext", postPage.hasNext());
        
        return response;
    }

    /**
     * 将帖子实体转换为Map格式
     */
    private Map<String, Object> convertPostToMap(Post post) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", post.getId());
        map.put("title", post.getTitle());
        map.put("content", post.getContent());
        map.put("image", post.getImage());
        map.put("category", post.getCategory());
        map.put("likesCount", post.getLikesCount());
        map.put("collectsCount", post.getCollectsCount());
        map.put("dislikesCount", post.getDislikesCount());
        map.put("commentsCount", post.getCommentsCount());
        map.put("createdAt", post.getCreatedAt());
        map.put("updatedAt", post.getUpdatedAt());
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", post.getUser().getId());
        userMap.put("username", post.getUser().getUsername());
        userMap.put("avatar", post.getUser().getAvatar());
        userMap.put("level", post.getUser().getLevel());
        map.put("user", userMap);
        
        return map;
    }

    /**
     * 获取用户点赞的帖子列表
     * @param userId 用户ID
     * @return 点赞的帖子列表
     */
    public List<Map<String, Object>> getUserLikedPosts(Long userId) {
        List<Like> likes = likeRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Like like : likes) {
            Post post = like.getPost();
            if (post != null) {
                result.add(convertPostToMap(post));
            }
        }
        
        return result;
    }
}
