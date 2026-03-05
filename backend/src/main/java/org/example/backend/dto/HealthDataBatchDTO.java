package org.example.backend.dto;

/**
 * 批量健康数据上传DTO
 */
public class HealthDataBatchDTO {
    
    private Long elderlyId;
    private PhysiologicalDataDTO physiological;
    private BehaviorDataDTO behavior;
    private EnvironmentDataDTO environment;

    // Getter and Setter
    public Long getElderlyId() { return elderlyId; }
    public void setElderlyId(Long elderlyId) { this.elderlyId = elderlyId; }

    public PhysiologicalDataDTO getPhysiological() { return physiological; }
    public void setPhysiological(PhysiologicalDataDTO physiological) { this.physiological = physiological; }

    public BehaviorDataDTO getBehavior() { return behavior; }
    public void setBehavior(BehaviorDataDTO behavior) { this.behavior = behavior; }

    public EnvironmentDataDTO getEnvironment() { return environment; }
    public void setEnvironment(EnvironmentDataDTO environment) { this.environment = environment; }

    public static class PhysiologicalDataDTO {
        private Integer heartRate;
        private Integer bloodPressureHigh;
        private Integer bloodPressureLow;
        private Integer bloodOxygen;
        private Double bodyTemperature;
        private Integer respiratoryRate;
        private Double bloodSugar;

        // Getter and Setter
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
    }

    public static class BehaviorDataDTO {
        private Double positionX;
        private Double positionY;
        private Double positionZ;
        private Double movementSpeed;
        private Integer stayDuration;
        private String activityType;

        // Getter and Setter
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
    }

    public static class EnvironmentDataDTO {
        private Double roomTemperature;
        private Integer humidity;
        private Integer airQualityIndex;
        private Integer lightIntensity;
        private Integer noiseLevel;

        // Getter and Setter
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
    }
}
