package org.example.backend.service;

import org.springframework.stereotype.Component;

/**
 * 健康指标阈值判定工具
 *
 * 取值约定（与 Unity 端 HealthStatusUtil 严格一致）：
 *   normal     - 正常
 *   attention  - 注意（轻度异常）
 *   warning    - 警告（中度异常）
 *   danger     - 危险（重度异常）
 *   unknown    - 未知（无数据）
 *
 * 严重程度排序：unknown(0) < normal(1) < attention(2) < warning(3) < danger(4)
 */
@Component
public class HealthStatusEvaluator {

    public static final String NORMAL    = "normal";
    public static final String ATTENTION = "attention";
    public static final String WARNING   = "warning";
    public static final String DANGER    = "danger";
    public static final String UNKNOWN   = "unknown";

    /** 心率：< 50 或 > 110 危险，< 60 或 > 100 警告，< 65 或 > 95 注意，否则正常 */
    public String evaluateHeartRate(Integer hr) {
        if (hr == null || hr <= 0) return UNKNOWN;
        if (hr < 50 || hr > 110) return DANGER;
        if (hr < 60 || hr > 100) return WARNING;
        if (hr < 65 || hr > 95)  return ATTENTION;
        return NORMAL;
    }

    /** 血压 */
    public String evaluateBloodPressure(Integer high, Integer low) {
        if (high == null || low == null || high <= 0 || low <= 0) return UNKNOWN;
        if (high >= 160 || high < 90  || low >= 100 || low < 50) return DANGER;
        if (high >= 140 || high < 100 || low >= 90  || low < 60) return WARNING;
        if (high >= 130 || low  >= 85)                            return ATTENTION;
        return NORMAL;
    }

    /** 血氧饱和度 */
    public String evaluateBloodOxygen(Integer spo2) {
        if (spo2 == null || spo2 <= 0) return UNKNOWN;
        if (spo2 < 90) return DANGER;
        if (spo2 < 93) return WARNING;
        if (spo2 < 95) return ATTENTION;
        return NORMAL;
    }

    /** 体温 */
    public String evaluateTemperature(Double t) {
        if (t == null || t <= 0) return UNKNOWN;
        if (t >= 39.0 || t < 35.5) return DANGER;
        if (t >= 38.0 || t < 36.0) return WARNING;
        if (t >= 37.3)             return ATTENTION;
        return NORMAL;
    }

    /** 血糖 (mmol/L) */
    public String evaluateBloodSugar(Double mmol) {
        if (mmol == null || mmol <= 0) return UNKNOWN;
        if (mmol >= 11.1 || mmol < 2.8) return DANGER;
        if (mmol >= 7.8  || mmol < 3.9) return WARNING;
        if (mmol >= 6.5)                 return ATTENTION;
        return NORMAL;
    }

    /** 取多个状态中最严重的（用于汇总 overall） */
    public String worstOf(String... statuses) {
        int worst = -1;
        for (String s : statuses) {
            int v = severity(s);
            if (v > worst) worst = v;
        }
        return statuses.length == 0 ? UNKNOWN : fromSeverity(worst);
    }

    public int severity(String s) {
        if (s == null) return 0;
        switch (s.toLowerCase()) {
            case NORMAL:    return 1;
            case ATTENTION: return 2;
            case WARNING:   return 3;
            case DANGER:    return 4;
            default:        return 0;
        }
    }

    public String fromSeverity(int v) {
        switch (v) {
            case 1: return NORMAL;
            case 2: return ATTENTION;
            case 3: return WARNING;
            case 4: return DANGER;
            default: return UNKNOWN;
        }
    }
}
