package org.example.backend.dto;

/**
 * 用户信息DTO（简化版）
 */
public class UserInfoDTO {
    
    /**
     * 手机号（主键）
     */
    private String phone;
    
    /**
     * 性别
     */
    private String gender;
    
    /**
     * 头像地址
     */
    private String avatarUrl;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 用户名（兼容旧版本前端，实际就是手机号）
     */
    private String username;
    
    // Getter和Setter
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
        this.username = phone; // 同步设置username
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}
