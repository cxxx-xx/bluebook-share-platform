package com.bluebook.service;

import com.bluebook.model.User;
import com.bluebook.model.Collect;
import com.bluebook.repository.UserRepository;
import com.bluebook.repository.CollectRepository;
import com.bluebook.utils.PasswordUtils;
import com.bluebook.utils.SmsUtils;
import com.bluebook.utils.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务类
 * 
 * 功能模块：
 * 1. 用户信息管理 - getUserById, getUserInfo, getAllUsersInfo, updateAvatar
 * 2. 用户注册登录 - register, login, sendSmsCode
 * 3. 用户收藏管理 - getUserCollections
 * 4. 知名度等级计算 - updateFame
 * 5. 管理员功能 - addAdmin, adminCreateUser, adminUpdateUser, adminDeleteUser
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private MemoryStore memoryStore;

    @Autowired
    private CollectRepository collectRepository;
    
    // ==================== 用户信息管理模块 ====================
    
    /**
     * 根据ID获取用户实体
     * @param id 用户ID
     * @return 用户实体，不存在则返回null
     */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 获取用户详细信息（用于前端展示）
     * @param userId 用户ID
     * @return 包含用户信息的Map
     */
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("phone", user.getPhone());
        result.put("avatar", user.getAvatar());
        result.put("fame", user.getFame());
        result.put("level", user.getLevel());
        result.put("fansCount", user.getFansCount());
        result.put("followCount", user.getFollowCount());
        result.put("postCount", user.getPostCount());
        result.put("likesReceived", user.getLikesReceived());
        result.put("collectsReceived", user.getCollectsReceived());
        result.put("createdAt", user.getCreatedAt());
        result.put("isAdmin", user.isAdmin());
        return result;
    }
    
    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatar 头像文件名
     */
    public void updateAvatar(Long userId, String avatar) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatar(avatar);
        userRepository.save(user);
    }
    
    // ==================== 用户收藏管理模块 ====================
    
    /**
     * 获取用户收藏的帖子列表
     * @param userId 用户ID
     * @return 收藏的帖子信息列表
     */
    public List<Map<String, Object>> getUserCollections(Long userId) {
        List<Collect> collects = collectRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Collect collect : collects) {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", collect.getPost().getId());
            postMap.put("title", collect.getPost().getTitle());
            postMap.put("content", collect.getPost().getContent());
            postMap.put("image", collect.getPost().getImage());
            postMap.put("likesCount", collect.getPost().getLikesCount());
            postMap.put("collectsCount", collect.getPost().getCollectsCount());
            postMap.put("createdAt", collect.getPost().getCreatedAt());
            
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", collect.getPost().getUser().getId());
            userMap.put("username", collect.getPost().getUser().getUsername());
            userMap.put("avatar", collect.getPost().getUser().getAvatar());
            postMap.put("user", userMap);
            
            result.add(postMap);
        }
        return result;
    }
    
    // ==================== 用户注册登录模块 ====================
    
    /**
     * 用户注册
     * 验证短信验证码、密码格式、用户名和手机号唯一性
     * @param user 用户实体
     * @param code 短信验证码
     */
    public void register(User user, String code) {
        System.out.println("=== 注册请求 ===");
        System.out.println("用户名: " + user.getUsername());
        System.out.println("手机号: " + user.getPhone());
        System.out.println("验证码: " + code);
        
        // 验证短信验证码
        String storedCode = memoryStore.get("sms:code:" + user.getPhone());
        System.out.println("存储的验证码: " + storedCode);
        
        if (storedCode == null || !storedCode.equals(code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 验证密码格式：必须包含数字、字母、特殊符号中的至少两种，长度8-16位
        if (!passwordUtils.validatePassword(user.getPassword())) {
            throw new RuntimeException("密码必须包含数字、字母、特殊符号中的至少两种，长度8-16位");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        // 检查手机号是否已注册
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已注册");
        }

        // 加密密码并设置默认值
        user.setPassword(passwordUtils.encodePassword(user.getPassword()));
        user.setAvatar("default.jpg");
        user.setAdmin(false);
        user.setFame(0);
        user.setLevel(1);
        user.setFansCount(0);
        user.setFollowCount(0);
        user.setPostCount(0);
        user.setLikesReceived(0);
        user.setCollectsReceived(0);
        user.setDislikesReceived(0);

        User savedUser = userRepository.save(user);
        System.out.println("注册成功! 用户ID: " + savedUser.getId() + ", 手机号: " + savedUser.getPhone());
        
        // 注册成功后删除验证码
        memoryStore.delete("sms:code:" + user.getPhone());
    }

    /**
     * 用户登录
     * 支持管理员用户名登录和普通用户手机号登录
     * 使用图形验证码进行验证（不验证顺序）
     * @param account 账号（用户名或手机号）
     * @param password 密码
     * @param captcha 用户点击的验证码位置
     * @param sessionCaptcha 正确的验证码位置
     * @return 登录成功的用户实体
     */
    public User login(String account, String password, String captcha, String sessionCaptcha) {
        // 验证图形验证码
        if (captcha == null || sessionCaptcha == null) {
            throw new RuntimeException("验证码错误");
        }
        
        String[] userClicks = captcha.split(",");
        String[] correctPositions = sessionCaptcha.split(",");
        
        if (userClicks.length != 8 || correctPositions.length != 8) {
            throw new RuntimeException("验证码错误");
        }
        
        // 将用户点击的位置转换为集合（不验证顺序）
        Set<String> userClickSet = new HashSet<>();
        for (int i = 0; i < userClicks.length; i += 2) {
            userClickSet.add(userClicks[i].trim() + "," + userClicks[i + 1].trim());
        }
        
        Set<String> correctSet = new HashSet<>();
        for (int i = 0; i < correctPositions.length; i += 2) {
            correctSet.add(correctPositions[i].trim() + "," + correctPositions[i + 1].trim());
        }
        
        // 检查用户点击的格子是否与正确的格子匹配（不验证顺序）
        if (!userClickSet.equals(correctSet)) {
            throw new RuntimeException("验证码错误，请点击正确的格子");
        }
        
        // 尝试用户名登录（管理员）
        User user = userRepository.findByUsername(account);
        
        if (user != null && user.isAdmin()) {
            if (!passwordUtils.matches(password, user.getPassword())) {
                throw new RuntimeException("密码错误");
            }
            return user;
        }
        
        // 尝试手机号登录（普通用户）
        user = userRepository.findByPhone(account);
        if (user == null) {
            System.out.println("登录失败: 未找到手机号为 " + account + " 的用户");
            System.out.println("数据库中所有用户:");
            userRepository.findAll().forEach(u -> System.out.println("  - 手机号: " + u.getPhone() + ", 用户名: " + u.getUsername()));
            throw new RuntimeException("用户不存在");
        }
        if (!passwordUtils.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    /**
     * 发送短信验证码
     * 生成6位随机验证码并通过短信发送
     * @param phone 手机号
     */
    public void sendSmsCode(String phone) throws Exception {
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 发送短信
        boolean success = smsUtils.sendSms(phone, code, 60);
        if (!success) {
            throw new RuntimeException("短信发送失败");
        }

        // 将验证码存入Redis，有效期1小时
        memoryStore.set("sms:code:" + phone, code, 1, TimeUnit.HOURS);
    }

    // ==================== 知名度等级计算模块 ====================

    /**
     * 更新用户知名度和等级
     * 知名度计算公式：获赞数 + 收藏数*5 + 粉丝数*10
     * 等级划分：
     * - Lv.1 布衣无名: 知名度 < 50
     * - Lv.2 里闾崭露: 知名度 >= 50
     * - Lv.3 一技鸣乡: 知名度 >= 500
     * - Lv.4 名动郡邑: 知名度 >= 10,000
     * - Lv.5 誉满江湖: 知名度 >= 100,000
     * - Lv.6 声闻天下: 知名度 >= 10,000,000
     * - Lv.7 名冠当世: 知名度 >= 100,000,000
     * - Lv.8 千古流芳: 知名度 >= 1,000,000,000
     * - Lv.9 万世不朽: 知名度 >= 10,000,000,000
     * @param user 用户实体
     */
    public void updateFame(User user) {
        // 计算知名度
        long fame = user.getLikesReceived() + user.getCollectsReceived() * 5 + user.getFansCount() * 10;
        user.setFame(fame);

        // 根据知名度确定等级
        int level = 1;
        if (fame >= 10000000000L) {
            level = 9;
        } else if (fame >= 1000000000L) {
            level = 8;
        } else if (fame >= 100000000L) {
            level = 7;
        } else if (fame >= 10000000L) {
            level = 6;
        } else if (fame >= 100000L) {
            level = 5;
        } else if (fame >= 10000L) {
            level = 4;
        } else if (fame >= 500L) {
            level = 3;
        } else if (fame >= 50L) {
            level = 2;
        }
        user.setLevel(level);

        userRepository.save(user);
    }

    // ==================== 管理员功能模块 ====================

    /**
     * 添加管理员账户
     * @param username 用户名
     * @param password 密码
     * @param phone 手机号
     */
    public void addAdmin(String username, String password, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordUtils.encodePassword(password));
        user.setPhone(phone);
        user.setAvatar("default.jpg");
        user.setAdmin(true);
        user.setFame(0);
        user.setLevel(1);
        user.setFansCount(0);
        user.setFollowCount(0);
        user.setPostCount(0);
        user.setLikesReceived(0);
        user.setCollectsReceived(0);
        user.setDislikesReceived(0);
        userRepository.save(user);
    }

    /**
     * 管理员创建用户
     * @param user 用户实体
     */
    public void adminCreateUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已注册");
        }
        
        // 设置密码，默认为12345678
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordUtils.encodePassword(user.getPassword()));
        } else {
            user.setPassword(passwordUtils.encodePassword("12345678"));
        }
        
        // 设置默认值
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar("default.jpg");
        }
        if (user.getLevel() == 0) {
            user.setLevel(1);
        }
        
        userRepository.save(user);
    }
    
    /**
     * 管理员更新用户信息
     * @param userId 用户ID
     * @param userData 更新的用户数据
     */
    public void adminUpdateUser(Long userId, User userData) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新用户名
        if (userData.getUsername() != null && !userData.getUsername().isEmpty()) {
            if (!user.getUsername().equals(userData.getUsername()) && userRepository.existsByUsername(userData.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(userData.getUsername());
        }
        
        // 更新手机号
        if (userData.getPhone() != null && !userData.getPhone().isEmpty()) {
            if (!user.getPhone().equals(userData.getPhone()) && userRepository.existsByPhone(userData.getPhone())) {
                throw new RuntimeException("手机号已注册");
            }
            user.setPhone(userData.getPhone());
        }
        
        // 更新密码
        if (userData.getPassword() != null && !userData.getPassword().isEmpty()) {
            user.setPassword(passwordUtils.encodePassword(userData.getPassword()));
        }
        
        // 更新其他字段
        if (userData.getLevel() > 0) {
            user.setLevel(userData.getLevel());
        }
        if (userData.getFame() >= 0) {
            user.setFame(userData.getFame());
        }
        if (userData.getFansCount() >= 0) {
            user.setFansCount(userData.getFansCount());
        }
        if (userData.getFollowCount() >= 0) {
            user.setFollowCount(userData.getFollowCount());
        }
        if (userData.getPostCount() >= 0) {
            user.setPostCount(userData.getPostCount());
        }
        if (userData.getLikesReceived() >= 0) {
            user.setLikesReceived(userData.getLikesReceived());
        }
        if (userData.getCollectsReceived() >= 0) {
            user.setCollectsReceived(userData.getCollectsReceived());
        }
        if (userData.getDislikesReceived() >= 0) {
            user.setDislikesReceived(userData.getDislikesReceived());
        }
        if (userData.getSearchCount() >= 0) {
            user.setSearchCount(userData.getSearchCount());
        }
        if (userData.getDailyFameGrowth() >= 0) {
            user.setDailyFameGrowth(userData.getDailyFameGrowth());
        }
        if (userData.getWeeklyFameGrowth() >= 0) {
            user.setWeeklyFameGrowth(userData.getWeeklyFameGrowth());
        }
        if (userData.getMonthlyFameGrowth() >= 0) {
            user.setMonthlyFameGrowth(userData.getMonthlyFameGrowth());
        }
        if (userData.getYearlyFameGrowth() >= 0) {
            user.setYearlyFameGrowth(userData.getYearlyFameGrowth());
        }
        if (userData.getAvatar() != null && !userData.getAvatar().isEmpty()) {
            user.setAvatar(userData.getAvatar());
        }
        
        userRepository.save(user);
    }
    
    /**
     * 管理员删除用户
     * @param userId 用户ID
     */
    public void adminDeleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.isAdmin()) {
            throw new RuntimeException("不能删除管理员账户");
        }
        userRepository.delete(user);
    }
    
    /**
     * 获取所有用户信息（用于管理员界面）
     * @return 所有用户信息列表
     */
    public List<Map<String, Object>> getAllUsersInfo() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("phone", user.getPhone());
            userMap.put("avatar", user.getAvatar());
            userMap.put("level", user.getLevel());
            userMap.put("fame", user.getFame());
            userMap.put("fansCount", user.getFansCount());
            userMap.put("followCount", user.getFollowCount());
            userMap.put("postCount", user.getPostCount());
            userMap.put("likesReceived", user.getLikesReceived());
            userMap.put("collectsReceived", user.getCollectsReceived());
            userMap.put("dislikesReceived", user.getDislikesReceived());
            userMap.put("searchCount", user.getSearchCount());
            userMap.put("dailyFameGrowth", user.getDailyFameGrowth());
            userMap.put("weeklyFameGrowth", user.getWeeklyFameGrowth());
            userMap.put("monthlyFameGrowth", user.getMonthlyFameGrowth());
            userMap.put("yearlyFameGrowth", user.getYearlyFameGrowth());
            userMap.put("isAdmin", user.isAdmin());
            userMap.put("createdAt", user.getCreatedAt());
            result.add(userMap);
        }
        return result;
    }
}
