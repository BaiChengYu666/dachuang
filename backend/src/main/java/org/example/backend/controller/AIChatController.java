package org.example.backend.controller;

import org.example.backend.dto.DeepSeekChatRequest;
import org.example.backend.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI聊天Controller
 */
@RestController
@RequestMapping("/api/ai")
public class AIChatController {
    
    @Autowired
    private DeepSeekService deepSeekService;
    
    /**
     * 简单问答接口（单次对话）
     * 
     * POST /api/ai/chat
     * {
     *   "message": "老年人正常心率是多少？"
     * }
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("收到AI问答请求: " + request);
            
            String userMessage = request.get("message");
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                response.put("code", 400);
                response.put("message", "消息不能为空");
                return response;
            }
            
            System.out.println("用户问题: " + userMessage);
            
            // 调用DeepSeek服务
            String aiReply = deepSeekService.chat(userMessage);
            
            System.out.println("AI回复: " + aiReply);
            
            response.put("code", 200);
            response.put("message", "成功");
            response.put("data", Map.of(
                    "reply", aiReply,
                    "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整堆栈
            response.put("code", 500);
            response.put("message", "服务器错误: " + e.getMessage());
            response.put("error", e.getClass().getName());
        }
        
        return response;
    }
    
    /**
     * 带上下文的对话接口（多轮对话）
     * 
     * POST /api/ai/chat-context
     * {
     *   "messages": [
     *     {"role": "user", "content": "我最近血压有点高"},
     *     {"role": "assistant", "content": "请问您的血压具体是多少？"},
     *     {"role": "user", "content": "140/90"}
     *   ]
     * }
     */
    @PostMapping("/chat-context")
    public Map<String, Object> chatWithContext(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messagesList = 
                    (List<Map<String, String>>) request.get("messages");
            
            if (messagesList == null || messagesList.isEmpty()) {
                response.put("code", 400);
                response.put("message", "消息列表不能为空");
                return response;
            }
            
            // 转换为DeepSeek消息格式
            List<DeepSeekChatRequest.Message> messages = messagesList.stream()
                    .map(m -> new DeepSeekChatRequest.Message(
                            m.get("role"), 
                            m.get("content")
                    ))
                    .toList();
            
            // 调用DeepSeek服务
            String aiReply = deepSeekService.chatWithHistory(messages);
            
            response.put("code", 200);
            response.put("message", "成功");
            response.put("data", Map.of(
                    "reply", aiReply,
                    "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器错误: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 健康状态检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "AI服务正常");
        response.put("data", Map.of(
                "service", "DeepSeek AI",
                "status", "running"
        ));
        return response;
    }
}
