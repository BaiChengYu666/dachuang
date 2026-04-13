using UnityEngine;

// ============================================================
// HeartbeatAnimator —— 心脏跳动动画
//
// 跳动频率与真实心率绑定（默认 75 bpm）
// 通过 SetHeartRate(bpm) 实时更新频率
//
// ⚠️ 重要：本脚本缩放的是【子物体的 mesh】而不是脚本所在的 Transform
//    这样当 Heart 被 parent 到骨骼上时，骨骼动画驱动的 Transform 改变
//    与本脚本的缩放不会冲突。
// ============================================================
public class HeartbeatAnimator : MonoBehaviour
{
    [Header("基准跳动幅度（占基准缩放比例）")]
    [Range(0f, 0.3f)]
    public float beatAmplitude = 0.04f;

    [Header("初始心率（bpm）")]
    public int initialHeartRate = 75;

    [Header("最小/最大心率限制（防止异常值动画过快）")]
    public int minBpm = 30;
    public int maxBpm = 180;

    [Header("缩放目标（留空 = 自动用第一个子物体）")]
    public Transform scaleTarget;

    // ---- 内部 ----
    private int _currentBpm;
    private Vector3 _baseScale;
    private float _phase;
    private Transform _target;

    void Start()
    {
        // 优先用 Inspector 指定的 target，否则找第一个子物体，最后兜底用自己
        if (scaleTarget != null)
            _target = scaleTarget;
        else if (transform.childCount > 0)
            _target = transform.GetChild(0);
        else
            _target = transform;

        _baseScale = _target.localScale;
        _currentBpm = Mathf.Clamp(initialHeartRate, minBpm, maxBpm);
    }

    /// <summary>
    /// 设置心率（来自后端 / JS）
    /// </summary>
    public void SetHeartRate(int bpm)
    {
        if (bpm <= 0) return;
        _currentBpm = Mathf.Clamp(bpm, minBpm, maxBpm);
    }

    void LateUpdate()
    {
        if (_target == null) return;

        // 频率 = bpm/60 Hz
        float freq = _currentBpm / 60f;

        // 相位累积
        _phase += freq * 2f * Mathf.PI * Time.deltaTime;
        if (_phase > Mathf.PI * 2f) _phase -= Mathf.PI * 2f;

        // sin 波形脉动
        float pulse = Mathf.Sin(_phase);
        float scale = 1f + pulse * beatAmplitude;

        _target.localScale = _baseScale * scale;
    }

    void OnDisable()
    {
        if (_target != null) _target.localScale = _baseScale;
    }
}
