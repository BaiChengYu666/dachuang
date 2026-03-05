package org.example.backend.controller;

import org.example.backend.config.DeepSeekConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * DeepSeek诊断Controller
 */
@RestController
@RequestMapping("/api/ai")
public class DeepSeekDiagnosticController {
    
    @Autowired
    private DeepSeekConfig config;
    
    /**
     * 检查DeepSeek配置
     */
    @GetMapping("/diagnostic")
    public Map<String, Object> diagnostic() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String apiKey = config.getApiKey();
            String apiUrl = config.getApiUrl();
            String model = config.getModel();
            
            response.put("code", 200);
            response.put("message", "配置检查");
            response.put("data", Map.of(
                    "apiKeyConfigured", apiKey != null && !apiKey.isEmpty(),
                    "apiKeyPreview", apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "未配置",
                    "apiUrl", apiUrl,
                    "model", model,
                    "status", "配置已加载"
            ));
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "配置检查失败: " + e.getMessage());
        }
        
        return response;
    }
}
