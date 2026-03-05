package org.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 预警记录实体
 */
@Entity
@Table(name = "alert_record", indexes = {
    @Index(name = "idx_elderly_id", columnList = "elderly_id"),
    @Index(name = "idx_is_handled", columnList = "is_handled"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class AlertRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elderly_id", nullable = false)
    private Long elderlyId;

    @Column(name = "alert_type", length = 50)
    private String alertType;

    @Column(name = "alert_level", length = 20)
    private String alertLevel;

    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String alertMessage;

    @Column(name = "is_handled")
    private Boolean isHandled = false;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "handler", length = 50)
    private String handler;

    @Column(name = "handle_note", columnDefinition = "TEXT")
    private String handleNote;

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

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

    public Boolean getIsHandled() { return isHandled; }
    public void setIsHandled(Boolean isHandled) { this.isHandled = isHandled; }

    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }

    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }

    public String getHandleNote() { return handleNote; }
    public void setHandleNote(String handleNote) { this.handleNote = handleNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
