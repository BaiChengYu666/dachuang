package org.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 健康风险记录实体
 */
@Entity
@Table(name = "health_risk_record", indexes = {
    @Index(name = "idx_elderly_id", columnList = "elderly_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_risk_level", columnList = "risk_level")
})
public class HealthRiskRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elderly_id", nullable = false)
    private Long elderlyId;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "status_label", length = 50)
    private String statusLabel;

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;

    @Column(name = "prediction_result", columnDefinition = "TEXT")
    private String predictionResult;

    @Column(name = "is_alerted")
    private Boolean isAlerted = false;

    @Column(name = "alert_sent_at")
    private LocalDateTime alertSentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getElderlyId() { return elderlyId; }
    public void setElderlyId(Long elderlyId) { this.elderlyId = elderlyId; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }

    public String getRiskFactors() { return riskFactors; }
    public void setRiskFactors(String riskFactors) { this.riskFactors = riskFactors; }

    public String getPredictionResult() { return predictionResult; }
    public void setPredictionResult(String predictionResult) { this.predictionResult = predictionResult; }

    public Boolean getIsAlerted() { return isAlerted; }
    public void setIsAlerted(Boolean isAlerted) { this.isAlerted = isAlerted; }

    public LocalDateTime getAlertSentAt() { return alertSentAt; }
    public void setAlertSentAt(LocalDateTime alertSentAt) { this.alertSentAt = alertSentAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
