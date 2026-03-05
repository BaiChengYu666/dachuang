package org.example.backend.dto;

/**
 * 用户登录请求DTO
 */
public class LoginRequest {
    private String username;
    private String password;
    
    // 无参构造
    public LoginRequest() {
    }
    
    // 全参构造
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getter和Setter
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
