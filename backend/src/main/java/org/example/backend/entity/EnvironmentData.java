package org.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 环境数据实体
 */
@Entity
@Table(name = "environment_data", indexes = {
    @Index(name = "idx_elderly_id", columnList = "elderly_id"),
    @Index(name = "idx_recorded_at", columnList = "recorded_at")
})
public class EnvironmentData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elderly_id", nullable = false)
    private Long elderlyId;

    @Column(name = "room_temperature")
    private Double roomTemperature;

    private Integer humidity;

    @Column(name = "air_quality_index")
    private Integer airQualityIndex;

    @Column(name = "light_intensity")
    private Integer lightIntensity;

    @Column(name = "noise_level")
    private Integer noiseLevel;

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

    public Double getRoomTemperature() { return roomTemperature; }
    public void setRoomTemperature(Double roomTemperature) { this.roomTemperature = roomTemperature; }

    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }

    public Integer getAirQualityIndex() { return airQualityIndex; }
    public void setAirQualityIndex(Integer airQualityIndex) { this.airQualityIndex = airQualityIndex; }

    public Integer getLightIntensity() { return lightIntensity; }
    public void setLightIntensity(Integer lightIntensity) { this.lightIntensity = lightIntensity; }

    public Integer getNoiseLevel() { return noiseLevel; }
    public void setNoiseLevel(Integer noiseLevel) { this.noiseLevel = noiseLevel; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
