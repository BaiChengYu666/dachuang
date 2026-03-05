package org.example.backend.controller;

import org.example.backend.entity.Elderly;
import org.example.backend.entity.PhysiologicalData;
import org.example.backend.repository.ElderlyRepository;
import org.example.backend.repository.PhysiologicalDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 趋势预测控制器
 * 获取历史生理数据并调用AI服务生成24-48小时预测
 */
@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    @Autowired
    private PhysiologicalDataRepository physiologicalDataRepository;

    @Autowired
    private ElderlyRepository elderlyRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String AI_FORECAST_URL = "http://localhost:5000/predict/forecast";

    /**
     * GET /api/forecast/{elderlyId}?hours=24
     * 获取未来N小时的生理指标预测
     */
    @GetMapping("/{elderlyId}")
    public Map<String, Object> getForecast(
            @PathVariable Long elderlyId,
            @RequestParam(defaultValue = "24") int hours) {

        hours = Math.max(1, Math.min(hours, 48));

        try {
            // 获取老人基本信息
            int age = 68;
            String gender = "female";
            Optional<Elderly> elderlyOpt = elderlyRepository.findById(elderlyId);
            if (elderlyOpt.isPresent()) {
                Elderly elderly = elderlyOpt.get();
                if (elderly.getAge() != null) age = elderly.getAge();
                if (elderly.getGender() != null) gender = elderly.getGender();
            }

            // 取最近48小时历史生理数据作为上下文
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minusHours(48);
            List<PhysiologicalData> histData = physiologicalDataRepository
                    .findByElderlyIdAndRecordedAtBetween(elderlyId, start, end);

            // 按时间正序排列
            histData.sort(Comparator.comparing(PhysiologicalData::getRecordedAt));

            // 如果历史数据不足，扩展到全部数据
            if (histData.size() < 3) {
                List<PhysiologicalData> all = physiologicalDataRepository
                        .findByElderlyIdOrderByRecordedAtDesc(elderlyId);
                Collections.reverse(all);
                histData = all;
            }

            // 转换为AI服务格式
            List<Map<String, Object>> historicalList = histData.stream().map(d -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("heartRate", d.getHeartRate() != null ? d.getHeartRate() : 72);
                item.put("bloodPressureHigh", d.getBloodPressureHigh() != null ? d.getBloodPressureHigh() : 120);
                item.put("bloodPressureLow", d.getBloodPressureLow() != null ? d.getBloodPressureLow() : 80);
                item.put("bloodOxygen", d.getBloodOxygen() != null ? d.getBloodOxygen() : 98);
                item.put("bodyTemperature", d.getBodyTemperature() != null ? d.getBodyTemperature() : 36.5);
                item.put("timestamp", d.getRecordedAt().toString());
                return item;
            }).collect(Collectors.toList());

            // 调用AI服务
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("historicalData", historicalList);
            requestBody.put("age", age);
            requestBody.put("gender", gender);
            requestBody.put("hours", hours);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> aiResp = restTemplate.postForObject(AI_FORECAST_URL, entity, Map.class);

            if (aiResp != null && Integer.valueOf(200).equals(aiResp.get("code"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> aiData = (Map<String, Object>) aiResp.get("data");

                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("message", "预测成功");

                Map<String, Object> data = new HashMap<>();
                data.put("forecast", aiData.get("forecast"));
                data.put("attentionWeights", aiData.get("attentionWeights"));
                data.put("summary", aiData.get("summary"));
                data.put("historicalCount", histData.size());
                data.put("hours", hours);
                result.put("data", data);
                return result;
            }

            return buildFallback(hours);

        } catch (Exception e) {
            return buildFallback(hours);
        }
    }

    /** 当AI服务不可用时返回基于规则的模拟预测 */
    private Map<String, Object> buildFallback(int hours) {
        Random rng = new Random(42);
        int hr = 72, bph = 125, bpl = 80;
        double temp = 36.5;

        List<Map<String, Object>> forecast = new ArrayList<>();
        for (int h = 1; h <= hours; h++) {
            hr  = clamp(hr  + rng.nextInt(5) - 2, 60, 100);
            bph = clamp(bph + rng.nextInt(5) - 2, 110, 145);
            bpl = clamp(bpl + rng.nextInt(3) - 1, 65, 95);
            temp = Math.round((temp + (rng.nextDouble() - 0.5) * 0.1) * 10.0) / 10.0;
            temp = Math.max(36.0, Math.min(37.4, temp));
            int oxygen = 96 + rng.nextInt(4);
            double risk = bph > 140 ? 35.0 + rng.nextInt(15) : 18.0 + rng.nextInt(12);

            Map<String, Object> p = new LinkedHashMap<>();
            p.put("hour", h);
            p.put("heartRate", hr);
            p.put("bloodPressureHigh", bph);
            p.put("bloodPressureLow", bpl);
            p.put("bloodOxygen", oxygen);
            p.put("bodyTemperature", temp);
            p.put("riskScore", Math.round(risk * 10.0) / 10.0);
            p.put("riskLevel", bph > 140 ? "medium" : "low");
            forecast.add(p);
        }

        // Ascending attention weights (more recent = more important)
        List<Double> attn = new ArrayList<>();
        int n = Math.min(hours, 20);
        for (int i = 0; i < n; i++) {
            attn.add(Math.round((0.01 + i * 0.004) * 1000.0) / 1000.0);
        }
        double attnSum = attn.stream().mapToDouble(Double::doubleValue).sum();
        attn.replaceAll(v -> Math.round(v / attnSum * 1000.0) / 1000.0);

        Map<String, Object> summary = new HashMap<>();
        summary.put("hrTrend", "stable");
        summary.put("bpTrend", "stable");
        summary.put("maxRisk", 35.0);
        summary.put("avgRisk", 24.0);
        summary.put("highRiskHours", Collections.emptyList());

        Map<String, Object> data = new HashMap<>();
        data.put("forecast", forecast);
        data.put("attentionWeights", attn);
        data.put("summary", summary);
        data.put("historicalCount", 0);
        data.put("hours", hours);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "预测成功（模拟数据）");
        result.put("data", data);
        return result;
    }

    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
