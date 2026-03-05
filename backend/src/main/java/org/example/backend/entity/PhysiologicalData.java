package org.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 生理数据实体
 */
@Entity
@Table(name = "physiological_data", indexes = {
    @Index(name = "idx_elderly_id", columnList = "elderly_id"),
    @Index(name = "idx_recorded_at", columnList = "recorded_at")
})
public class PhysiologicalData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elderly_id", nullable = false)
    private Long elderlyId;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "blood_pressure_high")
    private Integer bloodPressureHigh;

    @Column(name = "blood_pressure_low")
    private Integer bloodPressureLow;

    @Column(name = "blood_oxygen")
    private Integer bloodOxygen;

    @Column(name = "body_temperature")
    private Double bodyTemperature;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "blood_sugar")
    private Double bloodSugar;

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

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Integer getBloodPressureHigh() { return bloodPressureHigh; }
    public void setBloodPressureHigh(Integer bloodPressureHigh) { this.bloodPressureHigh = bloodPressureHigh; }

    public Integer getBloodPressureLow() { return bloodPressureLow; }
    public void setBloodPressureLow(Integer bloodPressureLow) { this.bloodPressureLow = bloodPressureLow; }

    public Integer getBloodOxygen() { return bloodOxygen; }
    public void setBloodOxygen(Integer bloodOxygen) { this.bloodOxygen = bloodOxygen; }

    public Double getBodyTemperature() { return bodyTemperature; }
    public void setBodyTemperature(Double bodyTemperature) { this.bodyTemperature = bodyTemperature; }

    public Integer getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(Integer respiratoryRate) { this.respiratoryRate = respiratoryRate; }

    public Double getBloodSugar() { return bloodSugar; }
    public void setBloodSugar(Double bloodSugar) { this.bloodSugar = bloodSugar; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
