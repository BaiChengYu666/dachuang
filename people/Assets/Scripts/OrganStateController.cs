using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// ============================================================
// OrganStateController —— 控制单个器官的视觉状态
//
// 用法：
//   1. 把这个脚本挂到任意一个器官 GameObject 上（如 Heart / Lung / Brain）
//   2. 在 Inspector 里指定该器官包含的所有 Renderer（自动收集子物体）
//   3. 调用 SetStatus(HealthStatus) 即可改变颜色 + 是否脉冲
//
// 与 GenderModelManager 解耦：每个器官独立持有自己的 controller
// ============================================================
public class OrganStateController : MonoBehaviour
{
    [Header("器官标识（用于日志/调试）")]
    public string organName = "Heart";

    [Header("是否在 Start 时自动收集子 Renderer")]
    public bool autoCollectRenderers = true;

    [Header("是否启用 Danger 状态的缩放脉冲动画")]
    [Tooltip("默认关闭。开启后器官在 Danger 状态会上下缩放闪烁；关闭只变颜色")]
    public bool enableDangerPulse = false;

    [Header("脉冲动画参数（仅 enableDangerPulse=true 时生效）")]
    [Tooltip("脉冲缩放幅度（占基准缩放比例）")]
    [Range(0f, 0.5f)] public float pulseAmplitude = 0.08f;
    [Tooltip("脉冲速度（每秒周期数）")]
    [Range(0.1f, 5f)] public float pulseFrequency = 1.5f;

    // ---- 内部 ----
    private List<Renderer> _renderers = new List<Renderer>();
    private List<Material> _materialInstances = new List<Material>(); // 实例化材质，避免共用
    private Vector3 _baseScale;
    private Coroutine _pulseCoroutine;
    private HealthStatus _currentStatus = HealthStatus.Unknown;

    void Awake()
    {
        _baseScale = transform.localScale;

        if (autoCollectRenderers)
            _renderers.AddRange(GetComponentsInChildren<Renderer>(true));

        // 实例化每个材质，防止修改影响其他器官
        foreach (var r in _renderers)
        {
            foreach (var sharedMat in r.sharedMaterials)
            {
                // r.materials 会自动实例化
            }
            foreach (var mat in r.materials)
                _materialInstances.Add(mat);
        }

        // 初始置为未知态
        ApplyVisual(HealthStatus.Unknown);
    }

    /// <summary>
    /// 主入口：设置器官状态。供 TwinDataReceiver 调用。
    /// </summary>
    public void SetStatus(HealthStatus status)
    {
        if (status == _currentStatus) return;
        _currentStatus = status;
        Debug.Log($"[Organ:{organName}] 状态变化 → {status}");

        ApplyVisual(status);

        // 脉冲控制 —— 默认关闭，避免器官跟着 scale 抖动
        if (enableDangerPulse && HealthStatusColors.ShouldPulse(status))
            StartPulse();
        else
            StopPulse();
    }

    /// <summary>
    /// 字符串入口（方便从 JS 直接调）
    /// </summary>
    public void SetStatusString(string s)
    {
        SetStatus(HealthStatusUtil.Parse(s));
    }

    // ---- 视觉应用 ----
    private void ApplyVisual(HealthStatus status)
    {
        Color baseColor = HealthStatusColors.GetColor(status);
        float emission  = HealthStatusColors.GetEmissionIntensity(status);
        Color emissionColor = baseColor * emission;

        foreach (var mat in _materialInstances)
        {
            // URP / Standard / Built-in 都尝试一下，最大兼容性
            if (mat.HasProperty("_BaseColor"))     mat.SetColor("_BaseColor", baseColor);
            if (mat.HasProperty("_Color"))         mat.SetColor("_Color",     baseColor);
            if (mat.HasProperty("_EmissionColor"))
            {
                mat.EnableKeyword("_EMISSION");
                mat.SetColor("_EmissionColor", emissionColor);
            }
        }
    }

    // ---- 脉冲动画 ----
    private void StartPulse()
    {
        if (_pulseCoroutine != null) return;
        _pulseCoroutine = StartCoroutine(PulseLoop());
    }

    private void StopPulse()
    {
        if (_pulseCoroutine != null)
        {
            StopCoroutine(_pulseCoroutine);
            _pulseCoroutine = null;
        }
        transform.localScale = _baseScale;
    }

    private IEnumerator PulseLoop()
    {
        while (true)
        {
            float t = Mathf.Sin(Time.time * pulseFrequency * Mathf.PI * 2f);
            float scale = 1f + t * pulseAmplitude;
            transform.localScale = _baseScale * scale;
            yield return null;
        }
    }

    void OnDisable()
    {
        StopPulse();
    }
}
