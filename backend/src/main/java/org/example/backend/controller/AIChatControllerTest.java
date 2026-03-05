package org.example.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * AI聊天Controller - 测试版本
 * 先用模拟回复测试，确保接口通畅
 */
@RestController
@RequestMapping("/api/ai")
public class AIChatControllerTest {
    
    /**
     * 简单问答接口（测试版 - 不调用DeepSeek）
     */
    @PostMapping("/chat-test")
    public Map<String, Object> chatTest(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userMessage = request.get("message");
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                response.put("code", 400);
                response.put("message", "消息不能为空");
                return response;
            }
            
            // 模拟AI回复（不调用DeepSeek）
            String aiReply = generateMockReply(userMessage);
            
            response.put("code", 200);
            response.put("message", "成功");
            response.put("data", Map.of(
                    "reply", aiReply,
                    "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "服务器错误: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 生成模拟回复
     */
    private String generateMockReply(String question) {
        if (question.contains("心率")) {
            return "老年人的正常心率范围是60-100次/分钟。由于年龄增长，心脏功能有所变化，60-80次/分钟更为理想。如果静息心率持续超过100次或低于60次，建议及时就医检查。\n\n【这是测试回复，实际使用时将调用DeepSeek AI】";
        } else if (question.contains("血压")) {
            return "老年人正常血压一般在90-140/60-90 mmHg范围内。高血压患者饮食应注意：\n1. 低盐饮食\n2. 低脂低糖\n3. 多吃蔬菜水果\n4. 戒烟限酒\n5. 规律作息\n\n【这是测试回复】";
        } else if (question.contains("摔倒")) {
            return "预防老年人摔倒的措施：\n1. 居家环境改造，清除障碍物\n2. 穿防滑鞋\n3. 适当运动训练\n4. 定期检查视力\n5. 合理用药\n6. 补充钙质和维生素D\n\n【这是测试回复】";
        } else {
            return "您好！我收到了您的问题：\"" + question + "\"\n\n这是一个测试回复。实际使用时，我会通过DeepSeek AI给您专业的健康建议。\n\n请保持规律作息，均衡饮食，适量运动，定期体检。如有具体健康问题，建议咨询专业医生。";
        }
    }
}
