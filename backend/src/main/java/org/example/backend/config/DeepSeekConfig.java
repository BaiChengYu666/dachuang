package org.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek API配置
 */
@Configuration
public class DeepSeekConfig {
    
    @Value("${deepseek.api.key:sk-7b15a546ab71473facc7cb502e92ad10}")
    private String apiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${deepseek.model:deepseek-chat}")
    private String model;
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public String getModel() {
        return model;
    }
}
