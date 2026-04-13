package org.example.backend.service;

import org.example.backend.entity.BehaviorData;
import org.example.backend.entity.PhysiologicalData;
import org.example.backend.repository.BehaviorDataRepository;
import org.example.backend.repository.PhysiologicalDataRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 实时健康数据模拟器
 * 模拟传感器每5秒采集一次数据并写入数据库
 * 数据基于老年人日常生理基线，叠加自然波动
 */
@Service
public class LiveDataSimulator {

    private final PhysiologicalDataRepository physioRepo;
    private final BehaviorDataRepository behaviorRepo;

    // ---- 生理基线值 ----
    private double baseHeartRate   = 72.0;
    private double baseSysBP       = 122.0;
    private double baseDiaBP       = 78.0;
    private double baseSpO2        = 97.5;
    private double baseTemp        = 36.45;
    private double baseRespRate    = 16.0;
    private double baseBloodSugar  = 5.3;

    // ---- 行为状态机 ----
    private String currentActivity = "Standing";
    private int    activityTicks   = 0;   // 当前行为已持续的 tick 数
    private int    activityDuration = 6;  // 当前行为持续多少个 tick（每 tick = 5s）

    // ---- 事件模拟 ----
    private int tickCount = 0;
    private boolean inEventMode = false;
    private String eventType = "";
    private int eventTicksLeft = 0;

    public LiveDataSimulator(PhysiologicalDataRepository physioRepo,
                             BehaviorDataRepository behaviorRepo) {
        this.physioRepo = physioRepo;
        this.behaviorRepo = behaviorRepo;
    }

    /**
     * 每5秒执行一次，模拟传感器数据采集
     */
    @Scheduled(fixedRate = 5000, initialDelay = 3000)
    public void generateData() {
        tickCount++;
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        // ============ 行为状态切换 ============
        activityTicks++;
        if (activityTicks >= activityDuration) {
            switchActivity(rng);
        }

        // ============ 事件触发（每120个tick≈10分钟触发一次小事件）============
        if (!inEventMode && tickCount % 120 == 0) {
            triggerEvent(rng);
        }
        if (inEventMode) {
            eventTicksLeft--;
            if (eventTicksLeft <= 0) {
                inEventMode = false;
                eventType = "";
                // 恢复基线
                baseHeartRate = 72.0;
                baseSysBP = 122.0;
                baseDiaBP = 78.0;
                baseSpO2 = 97.5;
                baseTemp = 36.45;
            }
        }

        // ============ 根据行为调整基线 ============
        double hrBase = baseHeartRate;
        double rrBase = baseRespRate;
        if ("Walking".equals(currentActivity)) {
            hrBase += 12;  // 走路心率偏高
            rrBase += 3;
        }

        // ============ 生成生理数据（基线 + 传感器噪声）============
        int heartRate       = clampInt(hrBase + rng.nextGaussian() * 3, 55, 130);
        int bpHigh          = clampInt(baseSysBP + rng.nextGaussian() * 4, 90, 170);
        int bpLow           = clampInt(baseDiaBP + rng.nextGaussian() * 3, 55, 105);
        int spO2            = clampInt(baseSpO2 + rng.nextGaussian() * 0.8, 88, 100);
        double temp         = clampDouble(baseTemp + rng.nextGaussian() * 0.15, 35.0, 39.5);
        int respRate        = clampInt(rrBase + rng.nextGaussian() * 1.5, 12, 25);
        double bloodSugar   = clampDouble(baseBloodSugar + rng.nextGaussian() * 0.3, 3.5, 10.0);

        // 写入生理数据
        PhysiologicalData physio = new PhysiologicalData();
        physio.setElderlyId(1L);
        physio.setHeartRate(heartRate);
        physio.setBloodPressureHigh(bpHigh);
        physio.setBloodPressureLow(bpLow);
        physio.setBloodOxygen(spO2);
        physio.setBodyTemperature(Math.round(temp * 10.0) / 10.0);
        physio.setRespiratoryRate(respRate);
        physio.setBloodSugar(Math.round(bloodSugar * 10.0) / 10.0);
        physio.setRecordedAt(LocalDateTime.now());
        physioRepo.save(physio);

        // 写入行为数据
        BehaviorData behavior = new BehaviorData();
        behavior.setElderlyId(1L);
        behavior.setActivityType(currentActivity);
        behavior.setMovementSpeed("Walking".equals(currentActivity) ? 0.8 + rng.nextDouble() * 0.4 : 0.0);
        behavior.setStayDuration(activityTicks * 5);
        behavior.setPositionX(rng.nextDouble() * 10);
        behavior.setPositionY(0.0);
        behavior.setPositionZ(rng.nextDouble() * 10);
        behavior.setRecordedAt(LocalDateTime.now());
        behaviorRepo.save(behavior);
    }

    /**
     * 切换行为状态
     */
    private void switchActivity(ThreadLocalRandom rng) {
        activityTicks = 0;
        if ("Standing".equals(currentActivity)) {
            currentActivity = "Walking";
            activityDuration = 4 + rng.nextInt(8);  // 走路 20-60 秒
        } else {
            currentActivity = "Standing";
            activityDuration = 6 + rng.nextInt(10); // 站立 30-80 秒
        }
    }

    /**
     * 触发健康事件（让评委看到数据有波动和异常）
     */
    private void triggerEvent(ThreadLocalRandom rng) {
        int eventIdx = rng.nextInt(3);
        inEventMode = true;
        eventTicksLeft = 8 + rng.nextInt(8); // 事件持续 40-80 秒

        switch (eventIdx) {
            case 0: // 心率偏高事件
                eventType = "tachycardia";
                baseHeartRate = 95 + rng.nextInt(15); // 95-110
                break;
            case 1: // 血压偏高事件
                eventType = "hypertension";
                baseSysBP = 140 + rng.nextInt(15);    // 140-155
                baseDiaBP = 90 + rng.nextInt(8);      // 90-98
                break;
            case 2: // 低血氧事件
                eventType = "hypoxia";
                baseSpO2 = 92 + rng.nextInt(3);       // 92-94
                baseHeartRate = 85 + rng.nextInt(10);  // 代偿性心率升高
                break;
        }
    }

    // ---- 工具方法 ----
    private int clampInt(double val, int min, int max) {
        return Math.max(min, Math.min(max, (int) Math.round(val)));
    }

    private double clampDouble(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
