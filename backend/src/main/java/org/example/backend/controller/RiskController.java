package org.example.backend.controller;

import org.example.backend.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI风险分析控制器
 */
@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "*")
public class RiskController {
    
    @Autowired
    private AIService aiService;
    
    /**
     * 获取健康风险分析
     * 
     * POST /api/risk/analyze
     * 
     * 请求体示例:
     * {
     *   "heartRate": 72,
     *   "bloodPressureHigh": 120,
     *   "bloodPressureLow": 80,
     *   "bloodOxygen": 98,
     *   "bodyTemperature": 36.5,
     *   "age": 75,
     *   "gender": "female"
     * }
     */
    @PostMapping("/analyze")
    public Map<String, Object> analyzeRisk(@RequestBody Map<String, Object> data) {
        
        try {
            // 提取参数
            double heartRate = getDoubleValue(data, "heartRate", 72.0);
            double bpHigh = getDoubleValue(data, "bloodPressureHigh", 120.0);
            double bpLow = getDoubleValue(data, "bloodPressureLow", 80.0);
            double oxygen = getDoubleValue(data, "bloodOxygen", 98.0);
            double temp = getDoubleValue(data, "bodyTemperature", 36.5);
            int age = getIntValue(data, "age", 75);
            String gender = (String) data.getOrDefault("gender", "female");
            
            // 调用AI服务
            return aiService.predictHealthRisk(
                heartRate, bpHigh, bpLow, oxygen, temp, age, gender
            );
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "风险分析失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 行为异常检测
     * 
     * POST /api/risk/behavior
     */
    @PostMapping("/behavior")
    public Map<String, Object> detectBehavior(@RequestBody Map<String, Object> data) {
        try {
            Object behaviorData = data.get("behaviorData");
            return aiService.detectBehaviorAnomaly(behaviorData);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "行为检测失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 检查AI服务状态
     * 
     * GET /api/risk/status
     */
    @GetMapping("/status")
    public Map<String, Object> getAIServiceStatus() {
        boolean available = aiService.isAIServiceAvailable();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        
        Map<String, Object> data = new HashMap<>();
        data.put("aiServiceAvailable", available);
        data.put("message", available ? "AI服务正常" : "AI服务不可用");
        
        response.put("data", data);
        return response;
    }
    
    // 辅助方法：安全获取double值
    private double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    // 辅助方法：安全获取int值
    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
