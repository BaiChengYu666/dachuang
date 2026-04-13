using System;

// ============================================================
// 健康数据模型 —— Unity 端与后端 / 前端 JS 通信用的 JSON 结构
// 与 backend 的 PhysiologicalData 对应字段一致
// ============================================================

[Serializable]
public class HealthApiResponse
{
    public int code;
    public HealthPayload data;
}

[Serializable]
public class HealthPayload
{
    // 数值字段（与 backend PhysiologicalData 一致）
    public int    heartRate;          // 心率 bpm
    public int    bloodPressureHigh;  // 收缩压 mmHg
    public int    bloodPressureLow;   // 舒张压 mmHg
    public int    bloodOxygen;        // 血氧 %
    public float  bodyTemperature;    // 体温 ℃
    public int    respiratoryRate;    // 呼吸频率 次/分
    public float  bloodSugar;         // 血糖 mmol/L

    // 行为相关（用于跌倒触发整体变红）
    // 取值: "Standing" | "Walking" | "Falling"
    public string activityType;

    // 状态字段（后端阈值判定后下发，避免前端再算）
    // 取值: "normal" | "attention" | "warning" | "danger" | "unknown"
    public string heartStatus;
    public string bloodPressureStatus;
    public string bloodOxygenStatus;
    public string temperatureStatus;
    public string bloodSugarStatus;
    public string overallStatus;

    // 可选告警消息（用于浮层提示）
    public string alertMessage;
}

// ============================================================
// 健康状态枚举（C# 端使用，避免到处写魔法字符串）
// ============================================================
public enum HealthStatus
{
    Unknown   = 0,
    Normal    = 1,
    Attention = 2,
    Warning   = 3,
    Danger    = 4
}

public static class HealthStatusUtil
{
    public static HealthStatus Parse(string s)
    {
        if (string.IsNullOrEmpty(s)) return HealthStatus.Unknown;
        switch (s.ToLower())
        {
            case "normal":    return HealthStatus.Normal;
            case "attention": return HealthStatus.Attention;
            case "warning":   return HealthStatus.Warning;
            case "danger":    return HealthStatus.Danger;
            default:          return HealthStatus.Unknown;
        }
    }

    /// <summary>
    /// 当后端没有下发 status 字段时，根据数值兜底判定
    /// </summary>
    public static HealthStatus EvaluateHeartRate(int hr)
    {
        if (hr <= 0)  return HealthStatus.Unknown;
        if (hr < 50  || hr > 110) return HealthStatus.Danger;
        if (hr < 60  || hr > 100) return HealthStatus.Warning;
        if (hr < 65  || hr > 95)  return HealthStatus.Attention;
        return HealthStatus.Normal;
    }

    public static HealthStatus EvaluateBloodPressure(int high, int low)
    {
        if (high <= 0 || low <= 0) return HealthStatus.Unknown;
        if (high >= 160 || high < 90  || low >= 100 || low < 50) return HealthStatus.Danger;
        if (high >= 140 || high < 100 || low >= 90  || low < 60) return HealthStatus.Warning;
        if (high >= 130 || low  >= 85)                            return HealthStatus.Attention;
        return HealthStatus.Normal;
    }

    public static HealthStatus EvaluateBloodOxygen(int spo2)
    {
        if (spo2 <= 0) return HealthStatus.Unknown;
        if (spo2 < 90) return HealthStatus.Danger;
        if (spo2 < 93) return HealthStatus.Warning;
        if (spo2 < 95) return HealthStatus.Attention;
        return HealthStatus.Normal;
    }

    public static HealthStatus EvaluateTemperature(float t)
    {
        if (t <= 0) return HealthStatus.Unknown;
        if (t >= 39.0f || t < 35.5f) return HealthStatus.Danger;
        if (t >= 38.0f || t < 36.0f) return HealthStatus.Warning;
        if (t >= 37.3f)              return HealthStatus.Attention;
        return HealthStatus.Normal;
    }

    public static HealthStatus EvaluateBloodSugar(float mmol)
    {
        if (mmol <= 0) return HealthStatus.Unknown;
        if (mmol >= 11.1f || mmol < 2.8f) return HealthStatus.Danger;
        if (mmol >= 7.8f  || mmol < 3.9f) return HealthStatus.Warning;
        if (mmol >= 6.5f)                  return HealthStatus.Attention;
        return HealthStatus.Normal;
    }
}
