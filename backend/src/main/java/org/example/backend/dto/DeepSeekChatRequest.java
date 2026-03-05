package org.example.backend.dto;

import java.util.List;

/**
 * DeepSeek聊天请求
 */
public class DeepSeekChatRequest {
    
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer max_tokens;
    
    public DeepSeekChatRequest() {
    }
    
    public DeepSeekChatRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
        this.temperature = 0.7;
        this.max_tokens = 2000;
    }
    
    // Getters and Setters
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Integer getMax_tokens() {
        return max_tokens;
    }
    
    public void setMax_tokens(Integer max_tokens) {
        this.max_tokens = max_tokens;
    }
    
    /**
     * 消息对象
     */
    public static class Message {
        private String role; // system, user, assistant
        private String content;
        
        public Message() {
        }
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}
