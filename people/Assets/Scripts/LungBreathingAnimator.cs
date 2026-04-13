using UnityEngine;

// ============================================================
// LungBreathingAnimator —— 肺呼吸动画
//
// 呼吸频率默认 16 次/分钟（成年人静息）
// 血氧低时呼吸会加快（模拟代偿）
//
// ⚠️ 重要：本脚本缩放的是【子物体的 mesh】而不是脚本所在的 Transform
//    避免与骨骼动画/跟随脚本冲突
// ============================================================
public class LungBreathingAnimator : MonoBehaviour
{
    [Header("呼吸幅度（占基准缩放比例）")]
    [Range(0f, 0.3f)]
    public float breathAmplitude = 0.06f;

    [Header("基准呼吸频率（次/分钟）")]
    public float baseRespirationRate = 16f;

    [Header("血氧值（由 TwinDataReceiver 设置）")]
    [Range(70, 100)]
    public int currentSpo2 = 98;

    [Header("缩放目标（留空 = 自动用第一个子物体）")]
    public Transform scaleTarget;

    private Vector3 _baseScale;
    private float _phase;
    private Transform _target;

    void Start()
    {
        if (scaleTarget != null)
            _target = scaleTarget;
        else if (transform.childCount > 0)
            _target = transform.GetChild(0);
        else
            _target = transform;

        _baseScale = _target.localScale;
    }

    /// <summary>
    /// 设置血氧 —— 血氧低时自动加快呼吸频率
    /// </summary>
    public void SetBloodOxygen(int spo2)
    {
        if (spo2 <= 0) return;
        currentSpo2 = Mathf.Clamp(spo2, 70, 100);
    }

    void LateUpdate()
    {
        if (_target == null) return;

        // 血氧 95+ → 16次/分；血氧 90 → 22次/分；血氧 85 → 28次/分
        float rate = baseRespirationRate;
        if (currentSpo2 < 95) rate += (95 - currentSpo2) * 1.2f;

        float freq = rate / 60f; // Hz
        _phase += freq * 2f * Mathf.PI * Time.deltaTime;
        if (_phase > Mathf.PI * 2f) _phase -= Mathf.PI * 2f;

        // 呼吸是缓慢起伏，用 sin 即可
        float breath = (Mathf.Sin(_phase) + 1f) * 0.5f; // 0~1
        float scale = 1f + breath * breathAmplitude;

        _target.localScale = _baseScale * scale;
    }

    void OnDisable()
    {
        if (_target != null) _target.localScale = _baseScale;
    }
}
