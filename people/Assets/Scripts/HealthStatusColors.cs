using UnityEngine;

// ============================================================
// 健康状态颜色编码 —— 全项目统一色系
// 与小程序前端保持一致：绿/黄/橙/红/灰
// ============================================================

public static class HealthStatusColors
{
    // 主色（Albedo / BaseColor）
    public static readonly Color Normal    = HexToColor("#4CAF50"); // 柔和绿
    public static readonly Color Attention = HexToColor("#FFC107"); // 琥珀黄
    public static readonly Color Warning   = HexToColor("#FF9800"); // 橙红
    public static readonly Color Danger    = HexToColor("#F44336"); // 告警红
    public static readonly Color Unknown   = HexToColor("#9E9E9E"); // 灰

    // Emission 强度（让器官有"发光"效果，状态越严重发光越强）
    public const float EmissionNormal    = 0.3f;
    public const float EmissionAttention = 0.8f;
    public const float EmissionWarning   = 1.5f;
    public const float EmissionDanger    = 2.5f;
    public const float EmissionUnknown   = 0.0f;

    public static Color GetColor(HealthStatus status)
    {
        switch (status)
        {
            case HealthStatus.Normal:    return Normal;
            case HealthStatus.Attention: return Attention;
            case HealthStatus.Warning:   return Warning;
            case HealthStatus.Danger:    return Danger;
            default:                     return Unknown;
        }
    }

    public static float GetEmissionIntensity(HealthStatus status)
    {
        switch (status)
        {
            case HealthStatus.Normal:    return EmissionNormal;
            case HealthStatus.Attention: return EmissionAttention;
            case HealthStatus.Warning:   return EmissionWarning;
            case HealthStatus.Danger:    return EmissionDanger;
            default:                     return EmissionUnknown;
        }
    }

    /// <summary>
    /// 是否需要脉冲闪烁动画（仅 Danger 状态）
    /// </summary>
    public static bool ShouldPulse(HealthStatus status)
    {
        return status == HealthStatus.Danger;
    }

    private static Color HexToColor(string hex)
    {
        ColorUtility.TryParseHtmlString(hex, out Color c);
        return c;
    }
}
