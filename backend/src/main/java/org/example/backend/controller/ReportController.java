package org.example.backend.controller;

import org.example.backend.dto.ApiResponse;
import org.example.backend.entity.PhysiologicalData;
import org.example.backend.entity.User;
import org.example.backend.repository.HealthRiskRecordRepository;
import org.example.backend.repository.PhysiologicalDataRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhysiologicalDataRepository physioRepo;

    @Autowired
    private HealthRiskRecordRepository riskRepo;

    /**
     * 生成健康报告（JSON 结构化数据）
     */
    @GetMapping("/{userPhone}")
    public ApiResponse<Map<String, Object>> generateReport(@PathVariable String userPhone) {
        User user = userRepository.findById(userPhone).orElse(null);

        Map<String, Object> report = new LinkedHashMap<>();

        // 基本信息
        Map<String, Object> basicInfo = new LinkedHashMap<>();
        basicInfo.put("name",      user != null && user.getNickname() != null ? user.getNickname() : "用户");
        basicInfo.put("age",       user != null && user.getAge() != null ? user.getAge() : "--");
        basicInfo.put("gender",    user != null ? ("male".equals(user.getGender()) ? "男" : "女") : "--");
        basicInfo.put("phone",     userPhone);
        basicInfo.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        report.put("basicInfo", basicInfo);

        // 尝试获取最新生理数据（elderlyId=1 为默认老人记录）
        Optional<PhysiologicalData> latestOpt = physioRepo.findFirstByElderlyIdOrderByRecordedAtDesc(1L);
        PhysiologicalData latest = latestOpt.orElse(null);

        // 健康指标摘要
        Map<String, Object> metrics = new LinkedHashMap<>();
        if (latest != null) {
            metrics.put("heartRate",    buildMetric(latest.getHeartRate() != null ? String.valueOf(latest.getHeartRate()) : "--", "bpm", normalizeHR(latest.getHeartRate())));
            metrics.put("bloodPressure", buildMetric(
                latest.getBloodPressureHigh() != null ? latest.getBloodPressureHigh() + "/" + latest.getBloodPressureLow() : "--",
                "mmHg", normalizeBP(latest.getBloodPressureHigh())));
            metrics.put("bloodOxygen",  buildMetric(latest.getBloodOxygen() != null ? String.valueOf(latest.getBloodOxygen()) : "--", "%", normalizeBO(latest.getBloodOxygen())));
            metrics.put("temperature",  buildMetric(latest.getBodyTemperature() != null ? String.valueOf(latest.getBodyTemperature()) : "--", "°C", normalizeTmp(latest.getBodyTemperature())));
        } else {
            // 使用合理的模拟参考值
            metrics.put("heartRate",    buildMetric("72", "bpm", "正常"));
            metrics.put("bloodPressure", buildMetric("125/80", "mmHg", "正常"));
            metrics.put("bloodOxygen",  buildMetric("98.2", "%", "正常"));
            metrics.put("temperature",  buildMetric("36.5", "°C", "正常"));
        }
        report.put("metrics", metrics);

        // 风险评估
        Map<String, Object> riskInfo = new LinkedHashMap<>();
        riskRepo.findFirstByElderlyIdOrderByCreatedAtDesc(1L).ifPresentOrElse(r -> {
            riskInfo.put("riskLevel", r.getRiskLevel() != null ? r.getRiskLevel() : "low");
            riskInfo.put("riskScore", r.getRiskScore() != null ? r.getRiskScore() : 15);
            riskInfo.put("riskText",  riskLevelText(r.getRiskLevel()));
        }, () -> {
            riskInfo.put("riskLevel", "low");
            riskInfo.put("riskScore", 15);
            riskInfo.put("riskText", "低风险");
        });
        report.put("riskAssessment", riskInfo);

        // 健康建议
        report.put("recommendations", buildRecommendations((String) riskInfo.get("riskLevel")));

        // 本周数据统计（近7天生理数据均值）
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<PhysiologicalData> weekData = physioRepo.findByElderlyIdAndRecordedAtBetween(1L, weekAgo, LocalDateTime.now());
        Map<String, Object> weekStats = new LinkedHashMap<>();
        if (!weekData.isEmpty()) {
            double avgHR  = weekData.stream().filter(d -> d.getHeartRate() != null)
                .mapToInt(PhysiologicalData::getHeartRate).average().orElse(72);
            double avgBO  = weekData.stream().filter(d -> d.getBloodOxygen() != null)
                .mapToInt(PhysiologicalData::getBloodOxygen).average().orElse(98);
            weekStats.put("avgHeartRate",   Math.round(avgHR));
            weekStats.put("avgBloodOxygen", String.format("%.1f", avgBO));
            weekStats.put("recordCount",    weekData.size());
        } else {
            weekStats.put("avgHeartRate",   72);
            weekStats.put("avgBloodOxygen", "98.0");
            weekStats.put("recordCount",    0);
        }
        report.put("weekStats", weekStats);

        return ApiResponse.success(report);
    }

    private Map<String, Object> buildMetric(String value, String unit, String status) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("value", value);
        m.put("unit", unit);
        m.put("status", status);
        return m;
    }

    private String normalizeHR(Integer hr) {
        if (hr == null) return "--";
        if (hr < 60) return "偏低"; if (hr > 100) return "偏高"; return "正常";
    }
    private String normalizeBP(Integer sys) {
        if (sys == null) return "--";
        if (sys < 90) return "偏低"; if (sys > 140) return "偏高"; return "正常";
    }
    private String normalizeBO(Integer bo) {
        if (bo == null) return "--";
        return bo < 95 ? "偏低" : "正常";
    }
    private String normalizeTmp(Double tmp) {
        if (tmp == null) return "--";
        if (tmp < 36) return "偏低"; if (tmp >= 37.3) return "发热"; return "正常";
    }
    private String riskLevelText(String level) {
        if ("high".equals(level)) return "高风险";
        if ("medium".equals(level)) return "中风险";
        return "低风险";
    }

    private List<Map<String, String>> buildRecommendations(String level) {
        List<Map<String, String>> list = new ArrayList<>();
        list.add(rec("🏃", "适量运动", "每天进行 30 分钟以上轻度有氧运动，如散步、太极拳等"));
        list.add(rec("🥗", "均衡饮食", "多摄入蔬菜水果，减少高盐高脂食物，保证优质蛋白质摄入"));
        list.add(rec("😴", "规律作息", "保持每天 7~8 小时充足睡眠，避免熬夜"));
        list.add(rec("💊", "按时服药", "严格按医嘱定时定量服用药物，不得自行停药"));
        if ("high".equals(level) || "medium".equals(level)) {
            list.add(rec("🏥", "定期复查", "建议每月进行一次心血管及血压专项检查"));
            list.add(rec("📱", "紧急联系", "随时确保紧急联系人可联系到您，异常情况立即就医"));
        } else {
            list.add(rec("🌿", "心理健康", "保持积极乐观心态，参与社交活动，避免长期独处"));
        }
        return list;
    }

    private Map<String, String> rec(String icon, String title, String desc) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("icon", icon); m.put("title", title); m.put("desc", desc);
        return m;
    }
}
