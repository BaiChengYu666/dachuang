package org.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Entity
@Table(name = "user")
public class User {
    
    /**
     * 手机号作为主键（唯一标识）
     */
    @Id
    @Column(nullable = false, unique = true, length = 11)
    private String phone;
    
    /**
     * 密码
     */
    @Column(nullable = false, length = 100)
    private String password;
    
    /**
     * 性别：male/female
     */
    @Column(nullable = false, length = 10)
    private String gender;
    
    /**
     * 头像地址
     */
    @Column(length = 500)
    private String avatarUrl;
    
    /**
     * 昵称
     */
    @Column(length = 50)
    private String nickname;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 用户角色
     */
    @Column(length = 20)
    private String role = "USER";
    
    /**
     * 账户状态
     */
    @Column(length = 20)
    private String status = "ACTIVE";
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLoginAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getter和Setter
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
