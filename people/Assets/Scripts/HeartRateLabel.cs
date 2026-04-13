using UnityEngine;
using TMPro;

// ============================================================
// HeartRateLabel —— 心脏旁边显示心率数字
//
// ⭐ 重要用法变更：
//   Text 物体应作为【独立物体】放在场景根或 HealthDataManager 下，
//   不要作为 Heart 的子物体（否则会继承 MaleModel 的 180° 旋转和 Heart 的缩放）
//
// 本脚本挂在 Heart 上：
//   - Follow Target 不填 → 自动用 transform（即 Heart 自己）
//   - TMP Text 或 Legacy Text 拖入 → 脚本每帧把它放到 Heart 旁边
//   - Billboard 让文字始终面向相机
// ============================================================
public class HeartRateLabel : MonoBehaviour
{
    [Header("文本组件（拖 Scene 里的 Text - TextMeshPro）")]
    public TextMeshPro tmpText;       // TextMeshPro 3D 文本
    public TextMesh    legacyText;    // Legacy 3D Text

    [Header("跟随目标（留空 = 自身 Transform，即 Heart）")]
    public Transform followTarget;

    [Header("相对心脏的世界空间偏移")]
    [Tooltip("X=右, Y=上, Z=前。单位是世界米")]
    public Vector3 worldOffset = new Vector3(0.3f, 0.15f, 0f);

    [Header("文字世界空间缩放（越大越大）")]
    public float worldScale = 0.15f;

    [Header("是否始终面向相机")]
    public bool faceCamera = true;

    [Header("文字方向反了就勾上这个")]
    public bool flipFacing = true;

    [Header("显示格式（{0} = 心率数字）")]
    public string format = "♥ {0} bpm";

    private int _currentBpm = 75;
    private Camera _cam;
    private Transform _labelTf;

    void Awake()
    {
        if (followTarget == null) followTarget = transform;
    }

    void Start()
    {
        // 自动找 Text 组件
        if (tmpText == null && legacyText == null)
        {
            // 先找场景里所有 TMP，取距离自己最近的那个
            tmpText = FindClosestTMP();
            if (tmpText == null)
                legacyText = GetComponentInChildren<TextMesh>(true);
        }

        if (tmpText != null)    _labelTf = tmpText.transform;
        else if (legacyText != null) _labelTf = legacyText.transform;

        if (_labelTf != null)
            _labelTf.localScale = Vector3.one * worldScale;

        _cam = Camera.main;
        UpdateText();
    }

    TextMeshPro FindClosestTMP()
    {
        var all = GameObject.FindObjectsOfType<TextMeshPro>();
        TextMeshPro best = null;
        float bestDist = float.MaxValue;
        foreach (var t in all)
        {
            float d = Vector3.Distance(t.transform.position, transform.position);
            if (d < bestDist) { bestDist = d; best = t; }
        }
        return best;
    }

    /// <summary>
    /// 由 TwinDataReceiver 调用 —— 后端心率数据进来后调这里
    /// </summary>
    public void SetHeartRate(int bpm)
    {
        if (bpm <= 0) return;
        _currentBpm = bpm;
        UpdateText();
    }

    /// <summary>
    /// 根据健康状态变颜色
    /// </summary>
    public void SetStatusColor(HealthStatus status)
    {
        Color c = HealthStatusColors.GetColor(status);
        if (tmpText != null)    tmpText.color = c;
        if (legacyText != null) legacyText.color = c;
    }

    private void UpdateText()
    {
        string s = string.Format(format, _currentBpm);
        if (tmpText != null)    tmpText.text = s;
        if (legacyText != null) legacyText.text = s;
    }

    void LateUpdate()
    {
        if (_labelTf == null) return;

        // 1. 把文字位置锁到心脏世界位置 + 固定偏移（不受父旋转/缩放影响）
        _labelTf.position = followTarget.position + worldOffset;
        _labelTf.localScale = Vector3.one * worldScale;

        // 2. Billboard：始终面向相机（标准 billboard 写法）
        if (faceCamera)
        {
            if (_cam == null) _cam = Camera.main;
            if (_cam != null)
            {
                // 让文字 +Z 指向相机（TextMesh 可读面在 +Z 侧）
                _labelTf.rotation = Quaternion.LookRotation(
                    _cam.transform.position - _labelTf.position,
                    _cam.transform.up);
                if (flipFacing)
                    _labelTf.Rotate(0f, 180f, 0f, Space.Self);
            }
        }
    }
}
