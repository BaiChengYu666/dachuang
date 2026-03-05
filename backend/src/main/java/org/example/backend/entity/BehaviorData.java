package org.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 行为数据实体(UWB定位)
 */
@Entity
@Table(name = "behavior_data", indexes = {
    @Index(name = "idx_elderly_id", columnList = "elderly_id"),
    @Index(name = "idx_recorded_at", columnList = "recorded_at")
})
public class BehaviorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elderly_id", nullable = false)
    private Long elderlyId;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Column(name = "position_z")
    private Double positionZ;

    @Column(name = "movement_speed")
    private Double movementSpeed;

    @Column(name = "stay_duration")
    private Integer stayDuration;

    @Column(name = "activity_type", length = 50)
    private String activityType;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }

    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getElderlyId() { return elderlyId; }
    public void setElderlyId(Long elderlyId) { this.elderlyId = elderlyId; }

    public Double getPositionX() { return positionX; }
    public void setPositionX(Double positionX) { this.positionX = positionX; }

    public Double getPositionY() { return positionY; }
    public void setPositionY(Double positionY) { this.positionY = positionY; }

    public Double getPositionZ() { return positionZ; }
    public void setPositionZ(Double positionZ) { this.positionZ = positionZ; }

    public Double getMovementSpeed() { return movementSpeed; }
    public void setMovementSpeed(Double movementSpeed) { this.movementSpeed = movementSpeed; }

    public Integer getStayDuration() { return stayDuration; }
    public void setStayDuration(Integer stayDuration) { this.stayDuration = stayDuration; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
