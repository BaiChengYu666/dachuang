package org.example.backend.dto;

/**
 * 注册请求DTO（简化版）
 * 只需要：手机号、密码、性别
 */
public class RegisterRequest {
    
    /**
     * 手机号（作为唯一标识）
     */
    private String phone;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 性别：male/female
     */
    private String gender;
    
    // Getter和Setter
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
}
