package org.example.backend.service;

import org.example.backend.config.DeepSeekConfig;
import org.example.backend.dto.DeepSeekChatRequest;
import org.example.backend.dto.DeepSeekChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek AI服务
 */
@Service
public class DeepSeekService {
    
    @Autowired
    private DeepSeekConfig config;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 发送聊天请求到DeepSeek
     */
    public String chat(String userMessage) {
        try {
            // 构建系统提示
            String systemPrompt = "你是一个专业的老年健康顾问AI助手。" +
                    "你需要用温暖、耐心、专业的语气回答老年人的健康问题。" +
                    "回答要简洁明了，避免使用过于专业的医学术语。" +
                    "重要提醒：如果涉及严重健康问题，一定要建议就医。";
            
            // 构建消息列表
            List<DeepSeekChatRequest.Message> messages = new ArrayList<>();
            messages.add(new DeepSeekChatRequest.Message("system", systemPrompt));
            messages.add(new DeepSeekChatRequest.Message("user", userMessage));
            
            // 构建请求
            DeepSeekChatRequest request = new DeepSeekChatRequest(config.getModel(), messages);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());
            
            HttpEntity<DeepSeekChatRequest> entity = new HttpEntity<>(request, headers);
            
            // 发送请求
            ResponseEntity<DeepSeekChatResponse> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    DeepSeekChatResponse.class
            );
            
            // 提取回复
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                DeepSeekChatResponse body = response.getBody();
                if (body.getChoices() != null && !body.getChoices().isEmpty()) {
                    return body.getChoices().get(0).getMessage().getContent();
                }
            }
            
            return "抱歉，我暂时无法回答您的问题，请稍后再试。";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，服务暂时不可用，请稍后再试。错误: " + e.getMessage();
        }
    }
    
    /**
     * 带上下文的聊天（支持多轮对话）
     */
    public String chatWithHistory(List<DeepSeekChatRequest.Message> conversationHistory) {
        try {
            // 添加系统提示（如果还没有）
            if (conversationHistory.isEmpty() || 
                !conversationHistory.get(0).getRole().equals("system")) {
                
                String systemPrompt = "你是一个专业的老年健康顾问AI助手。" +
                        "你需要用温暖、耐心、专业的语气回答老年人的健康问题。" +
                        "回答要简洁明了，避免使用过于专业的医学术语。" +
                        "重要提醒：如果涉及严重健康问题，一定要建议就医。";
                
                conversationHistory.add(0, new DeepSeekChatRequest.Message("system", systemPrompt));
            }
            
            // 构建请求
            DeepSeekChatRequest request = new DeepSeekChatRequest(
                    config.getModel(), 
                    conversationHistory
            );
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());
            
            HttpEntity<DeepSeekChatRequest> entity = new HttpEntity<>(request, headers);
            
            // 发送请求
            ResponseEntity<DeepSeekChatResponse> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    DeepSeekChatResponse.class
            );
            
            // 提取回复
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                DeepSeekChatResponse body = response.getBody();
                if (body.getChoices() != null && !body.getChoices().isEmpty()) {
                    return body.getChoices().get(0).getMessage().getContent();
                }
            }
            
            return "抱歉，我暂时无法回答您的问题，请稍后再试。";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，服务暂时不可用。";
        }
    }
}
