package org.example.backend.controller;

import org.example.backend.dto.ApiResponse;
import org.example.backend.dto.LoginRequest;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.dto.UserInfoDTO;
import org.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // 头像上传目录
    private static final String UPLOAD_DIR = "uploads/avatars/";
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<UserInfoDTO> register(@RequestBody RegisterRequest request) {
        try {
            UserInfoDTO userInfo = userService.register(request);
            return ApiResponse.success("注册成功", userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<UserInfoDTO> login(@RequestBody LoginRequest request) {
        try {
            UserInfoDTO userInfo = userService.login(request);
            return ApiResponse.success("登录成功", userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/{phone}")
    public ApiResponse<UserInfoDTO> getUserInfo(@PathVariable String phone) {
        try {
            UserInfoDTO userInfo = userService.getUserInfo(phone);
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户性别
     */
    @PutMapping("/{phone}/gender")
    public ApiResponse<UserInfoDTO> updateGender(
            @PathVariable String phone,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String gender = request.get("gender");
            UserInfoDTO updatedInfo = userService.updateGender(phone, gender);
            return ApiResponse.success("性别修改成功", updatedInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户资料（昵称、年龄）
     */
    @PutMapping("/{phone}/profile")
    public ApiResponse<UserInfoDTO> updateProfile(
            @PathVariable String phone,
            @RequestBody java.util.Map<String, Object> request) {
        try {
            UserInfoDTO updatedInfo = userService.updateProfile(phone, request);
            return ApiResponse.success("资料更新成功", updatedInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/{phone}/avatar")
    public ApiResponse<String> uploadAvatar(
            @PathVariable String phone,
            @RequestParam("file") MultipartFile file) {
        try {
            // 检查文件
            if (file.isEmpty()) {
                return ApiResponse.error("文件为空");
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("只能上传图片文件");
            }
            
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            Files.write(filePath, file.getBytes());
            
            // 生成访问URL
            String avatarUrl = "/uploads/avatars/" + filename;
            
            // 更新用户头像
            userService.uploadAvatar(phone, avatarUrl);
            
            return ApiResponse.success("头像上传成功", avatarUrl);
        } catch (IOException e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
