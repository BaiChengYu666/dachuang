package org.example.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * AI服务调用类
 * 调用Python Flask AI服务进行健康风险预测
 */
@Service
public class AIService {
    
    private final String AI_SERVICE_URL = "http://localhost:5000";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 调用AI服务预测健康风险
     * 
     * @param heartRate 心率
     * @param bloodPressureHigh 高压
     * @param bloodPressureLow 低压
     * @param bloodOxygen 血氧
     * @param bodyTemperature 体温
     * @param age 年龄
     * @param gender 性别
     * @return AI预测结果
     */
    public Map<String, Object> predictHealthRisk(
        double heartRate,
        double bloodPressureHigh,
        double bloodPressureLow,
        double bloodOxygen,
        double bodyTemperature,
        int age,
        String gender
    ) {
        try {
            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("heartRate", heartRate);
            requestBody.put("bloodPressureHigh", bloodPressureHigh);
            requestBody.put("bloodPressureLow", bloodPressureLow);
            requestBody.put("bloodOxygen", bloodOxygen);
            requestBody.put("bodyTemperature", bodyTemperature);
            requestBody.put("age", age);
            requestBody.put("gender", gender);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 调用AI服务
            ResponseEntity<String> response = restTemplate.exchange(
                AI_SERVICE_URL + "/predict/risk",
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // 解析结果
            return objectMapper.readValue(response.getBody(), Map.class);
            
        } catch (Exception e) {
            // AI服务不可用时，返回默认值
            System.err.println("AI服务调用失败: " + e.getMessage());
            
            Map<String, Object> fallback = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("riskScore", 15);
            data.put("riskLevel", "low");
            data.put("advice", "AI服务暂不可用，使用默认评估");
            fallback.put("code", 200);
            fallback.put("data", data);
            return fallback;
        }
    }
    
    /**
     * 调用AI服务检测行为异常
     */
    public Map<String, Object> detectBehaviorAnomaly(Object behaviorData) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("behaviorData", behaviorData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                AI_SERVICE_URL + "/predict/behavior",
                HttpMethod.POST,
                entity,
                String.class
            );
            
            return objectMapper.readValue(response.getBody(), Map.class);
            
        } catch (Exception e) {
            System.err.println("行为检测失败: " + e.getMessage());
            
            Map<String, Object> fallback = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("fallRiskScore", 0);
            data.put("hasRisk", false);
            data.put("suggestion", "行为检测服务暂不可用");
            fallback.put("code", 200);
            fallback.put("data", data);
            return fallback;
        }
    }
    
    /**
     * 检查AI服务是否可用
     */
    public boolean isAIServiceAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                AI_SERVICE_URL + "/health",
                String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
