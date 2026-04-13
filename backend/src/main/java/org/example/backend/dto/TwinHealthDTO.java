package org.example.backend.dto;

/**
 * 数字孪生 Unity 端使用的健康数据 DTO
 *
 * 与 Unity 的 HealthPayload 类一一对应：
 *   people/Assets/Scripts/HealthDataModel.cs
 *
 * 字段含义：
 *   - 数值字段：心率/血压/血氧/体温/呼吸/血糖
 *   - 行为字段：activityType（Standing/Walking/Falling）
 *   - 状态字段：心脏/血压/血氧/体温/血糖/总体（由后端阈值判定后下发）
 *
 * 取值约定：normal | attention | warning | danger | unknown
 */
public class TwinHealthDTO {

    // ---- 生理数值 ----
    private Integer heartRate;
    private Integer bloodPressureHigh;
    private Integer bloodPressureLow;
    private Integer bloodOxygen;
    private Double  bodyTemperature;
    private Integer respiratoryRate;
    private Double  bloodSugar;

    // ---- 行为类型（Standing / Walking / Falling）----
    private String activityType;

    // ---- 各项状态（前端不再算阈值）----
    private String heartStatus;
    private String bloodPressureStatus;
    private String bloodOxygenStatus;
    private String temperatureStatus;
    private String bloodSugarStatus;
    private String overallStatus;

    // ---- 可选告警消息 ----
    private String alertMessage;

    // ---- Getter / Setter ----
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

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getHeartStatus() { return heartStatus; }
    public void setHeartStatus(String heartStatus) { this.heartStatus = heartStatus; }

    public String getBloodPressureStatus() { return bloodPressureStatus; }
    public void setBloodPressureStatus(String bloodPressureStatus) { this.bloodPressureStatus = bloodPressureStatus; }

    public String getBloodOxygenStatus() { return bloodOxygenStatus; }
    public void setBloodOxygenStatus(String bloodOxygenStatus) { this.bloodOxygenStatus = bloodOxygenStatus; }

    public String getTemperatureStatus() { return temperatureStatus; }
    public void setTemperatureStatus(String temperatureStatus) { this.temperatureStatus = temperatureStatus; }

    public String getBloodSugarStatus() { return bloodSugarStatus; }
    public void setBloodSugarStatus(String bloodSugarStatus) { this.bloodSugarStatus = bloodSugarStatus; }

    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }
}
