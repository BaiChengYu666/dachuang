package org.example.backend.service;

import org.example.backend.dto.LoginRequest;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.dto.UserInfoDTO;
import org.example.backend.entity.User;
import org.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户服务（简化版）
 * 使用手机号作为唯一标识
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 用户注册
     * 
     * @param request 包含：phone, password, gender
     */
    @Transactional
    public UserInfoDTO register(RegisterRequest request) {
        // 检查手机号是否已注册
        if (userRepository.existsById(request.getPhone())) {
            throw new RuntimeException("该手机号已被注册");
        }
        
        // 验证手机号格式
        if (!isValidPhone(request.getPhone())) {
            throw new RuntimeException("手机号格式不正确");
        }
        
        // 验证密码长度
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("密码长度至少为6位");
        }
        
        // 验证性别
        if (!"male".equals(request.getGender()) && !"female".equals(request.getGender())) {
            throw new RuntimeException("性别必须为male或female");
        }
        
        // 创建用户
        User user = new User();
        user.setPhone(request.getPhone());  // 手机号作为主键
        user.setPassword(encryptPassword(request.getPassword()));
        user.setGender(request.getGender());
        user.setAvatarUrl("/static/logo.png"); // 设置默认头像
        user.setRole("USER");
        user.setStatus("ACTIVE");
        
        User savedUser = userRepository.save(user);
        
        return convertToDTO(savedUser);
    }
    
    /**
     * 用户登录
     * 
     * @param request 包含：phone（作为username）, password
     */
    public UserInfoDTO login(LoginRequest request) {
        // 使用手机号登录
        String phone = request.getUsername(); // 前端传的username实际是手机号
        
        Optional<User> userOpt = userRepository.findById(phone);
        
        if (!userOpt.isPresent()) {
            throw new RuntimeException("手机号未注册");
        }
        
        User user = userOpt.get();
        
        // 验证密码
        if (!verifyPassword(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 检查账号状态
        if ("DISABLED".equals(user.getStatus())) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        return convertToDTO(user);
    }
    
    /**
     * 获取用户信息（通过手机号）
     */
    public UserInfoDTO getUserInfo(String phone) {
        Optional<User> userOpt = userRepository.findById(phone);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        return convertToDTO(userOpt.get());
    }
    
    /**
     * 更新用户性别
     */
    @Transactional
    public UserInfoDTO updateGender(String phone, String gender) {
        Optional<User> userOpt = userRepository.findById(phone);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        if ("male".equals(gender) || "female".equals(gender)) {
            user.setGender(gender);
            User savedUser = userRepository.save(user);
            return convertToDTO(savedUser);
        } else {
            throw new RuntimeException("性别必须为male或female");
        }
    }
    
    /**
     * 转换为DTO（隐藏密码）
     */
    private UserInfoDTO convertToDTO(User user) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setPhone(user.getPhone());
        dto.setGender(user.getGender());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setNickname(user.getNickname());
        dto.setAge(user.getAge());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        // 兼容旧版本前端
        dto.setUsername(user.getPhone());
        return dto;
    }
    
    /**
     * 密码加密（简化版）
     * 实际应用应该使用 BCrypt
     */
    private String encryptPassword(String password) {
        // 简单加密，实际应该使用 BCrypt
        return "encrypted_" + password;
    }
    
    /**
     * 验证密码
     */
    private boolean verifyPassword(String rawPassword, String encryptedPassword) {
        return encryptedPassword.equals("encrypted_" + rawPassword);
    }
    
    /**
     * 更新用户资料（昵称、年龄）
     */
    @Transactional
    public UserInfoDTO updateProfile(String phone, java.util.Map<String, Object> fields) {
        Optional<User> userOpt = userRepository.findById(phone);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        User user = userOpt.get();
        if (fields.containsKey("nickname")) {
            user.setNickname((String) fields.get("nickname"));
        }
        if (fields.containsKey("age")) {
            Object age = fields.get("age");
            if (age instanceof Number) {
                user.setAge(((Number) age).intValue());
            }
        }
        return convertToDTO(userRepository.save(user));
    }

    /**
     * 上传头像
     */
    @Transactional
    public UserInfoDTO uploadAvatar(String phone, String avatarUrl) {
        Optional<User> userOpt = userRepository.findById(phone);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setAvatarUrl(avatarUrl);
        User savedUser = userRepository.save(user);
        
        return convertToDTO(savedUser);
    }
    
    /**
     * 验证手机号格式（简化版）
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return false;
        }
        // 简单验证：11位数字
        return phone.matches("^1[0-9]{10}$");
    }
}
