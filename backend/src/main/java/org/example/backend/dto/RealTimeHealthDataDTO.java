package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 实时健康数据DTO(用于WebSocket推送)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeHealthDataDTO {

    private String type; // 消息类型:HEALTH_UPDATE/ALERT/RISK_WARNING
    private Long elderlyId;
    private LocalDateTime timestamp;
    private HealthData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthData {
        private Physiological physiological;
        private Behavior behavior;
        private Environment environment;
        private RiskStatus riskStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Physiological {
        private Integer heartRate;
        private String bloodPressure; // 格式: "120/80"
        private Integer bloodOxygen;
        private Double bodyTemperature;
        private Integer respiratoryRate;
        private Double bloodSugar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Behavior {
        private Position position;
        private String activityType;
        private Double movementSpeed;
        private Integer stayDuration;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private Double x;
        private Double y;
        private Double z;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Environment {
        private Double roomTemperature;
        private Integer humidity;
        private Integer airQualityIndex;
        private Integer lightIntensity;
        private Integer noiseLevel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskStatus {
        private Double riskScore;
        private String riskLevel;
        private String statusLabel;
    }
}
