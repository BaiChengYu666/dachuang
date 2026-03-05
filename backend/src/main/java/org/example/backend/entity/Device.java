package org.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联用户手机号 */
    @Column(nullable = false, length = 11)
    private String userPhone;

    /** 设备名称 */
    @Column(nullable = false, length = 50)
    private String deviceName;

    /** 设备类型：手环/血压计/血氧仪/体温计/其他 */
    @Column(length = 20)
    private String deviceType;

    /** 设备序列号 */
    @Column(length = 100)
    private String deviceSn;

    /** 状态：online/offline/binding */
    @Column(length = 20)
    private String status = "binding";

    /** 绑定时间 */
    @Column(updatable = false)
    private LocalDateTime bindTime;

    /** 最近在线时间 */
    private LocalDateTime lastOnlineTime;

    @PrePersist
    protected void onCreate() {
        bindTime = LocalDateTime.now();
        lastOnlineTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getDeviceSn() { return deviceSn; }
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getBindTime() { return bindTime; }
    public void setBindTime(LocalDateTime bindTime) { this.bindTime = bindTime; }
    public LocalDateTime getLastOnlineTime() { return lastOnlineTime; }
    public void setLastOnlineTime(LocalDateTime lastOnlineTime) { this.lastOnlineTime = lastOnlineTime; }
}
