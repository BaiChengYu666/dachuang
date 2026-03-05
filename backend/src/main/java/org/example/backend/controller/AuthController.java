package org.example.backend.controller;

import org.example.backend.dto.LoginRequest;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.dto.UserInfoDTO;
import org.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证Controller - 处理登录和注册
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            UserInfoDTO userInfo = userService.register(request);
            response.put("code", 200);
            response.put("message", "注册成功");
            response.put("data", userInfo);
        } catch (Exception e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            UserInfoDTO userInfo = userService.login(request);
            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", userInfo);
        } catch (Exception e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
}
