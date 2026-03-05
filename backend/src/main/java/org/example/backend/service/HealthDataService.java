package org.example.backend.service;

import org.example.backend.dto.HealthDataBatchDTO;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康数据采集服务
 */
@Service
public class HealthDataService {

    @Autowired
    private PhysiologicalDataRepository physiologicalDataRepository;

    @Autowired
    private BehaviorDataRepository behaviorDataRepository;

    @Autowired
    private EnvironmentDataRepository environmentDataRepository;

    /**
     * 批量保存多模态健康数据
     */
    @Transactional
    public Map<String, Object> saveBatchData(HealthDataBatchDTO batchDTO) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // 保存生理数据
        if (batchDTO.getPhysiological() != null) {
            PhysiologicalData physio = new PhysiologicalData();
            physio.setElderlyId(batchDTO.getElderlyId());
            physio.setHeartRate(batchDTO.getPhysiological().getHeartRate());
            physio.setBloodPressureHigh(batchDTO.getPhysiological().getBloodPressureHigh());
            physio.setBloodPressureLow(batchDTO.getPhysiological().getBloodPressureLow());
            physio.setBloodOxygen(batchDTO.getPhysiological().getBloodOxygen());
            physio.setBodyTemperature(batchDTO.getPhysiological().getBodyTemperature());
            physio.setRespiratoryRate(batchDTO.getPhysiological().getRespiratoryRate());
            physio.setBloodSugar(batchDTO.getPhysiological().getBloodSugar());
            physio.setRecordedAt(now);
            PhysiologicalData savedPhysio = physiologicalDataRepository.save(physio);
            result.put("physiologicalId", savedPhysio.getId());
        }

        // 保存行为数据
        if (batchDTO.getBehavior() != null) {
            BehaviorData behavior = new BehaviorData();
            behavior.setElderlyId(batchDTO.getElderlyId());
            behavior.setPositionX(batchDTO.getBehavior().getPositionX());
            behavior.setPositionY(batchDTO.getBehavior().getPositionY());
            behavior.setPositionZ(batchDTO.getBehavior().getPositionZ());
            behavior.setMovementSpeed(batchDTO.getBehavior().getMovementSpeed());
            behavior.setStayDuration(batchDTO.getBehavior().getStayDuration());
            behavior.setActivityType(batchDTO.getBehavior().getActivityType());
            behavior.setRecordedAt(now);
            BehaviorData savedBehavior = behaviorDataRepository.save(behavior);
            result.put("behaviorId", savedBehavior.getId());
        }

        // 保存环境数据
        if (batchDTO.getEnvironment() != null) {
            EnvironmentData env = new EnvironmentData();
            env.setElderlyId(batchDTO.getElderlyId());
            env.setRoomTemperature(batchDTO.getEnvironment().getRoomTemperature());
            env.setHumidity(batchDTO.getEnvironment().getHumidity());
            env.setAirQualityIndex(batchDTO.getEnvironment().getAirQualityIndex());
            env.setLightIntensity(batchDTO.getEnvironment().getLightIntensity());
            env.setNoiseLevel(batchDTO.getEnvironment().getNoiseLevel());
            env.setRecordedAt(now);
            EnvironmentData savedEnv = environmentDataRepository.save(env);
            result.put("environmentId", savedEnv.getId());
        }

        result.put("timestamp", now);
        return result;
    }

    /**
     * 获取老人最新的健康数据
     */
    public Map<String, Object> getLatestHealthData(Long elderlyId) {
        Map<String, Object> data = new HashMap<>();

        // 最新生理数据
        physiologicalDataRepository.findFirstByElderlyIdOrderByRecordedAtDesc(elderlyId)
            .ifPresent(physio -> data.put("physiological", physio));

        // 最新行为数据
        behaviorDataRepository.findFirstByElderlyIdOrderByRecordedAtDesc(elderlyId)
            .ifPresent(behavior -> data.put("behavior", behavior));

        // 最新环境数据
        environmentDataRepository.findFirstByElderlyIdOrderByRecordedAtDesc(elderlyId)
            .ifPresent(env -> data.put("environment", env));

        return data;
    }
}
