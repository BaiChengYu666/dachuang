using UnityEngine;

// ============================================================
// OrganFollow —— 器官软跟随骨骼
//
// 为什么需要这个脚本？
//   之前把器官 parent 到 spine_03/head 骨骼下，结果走路时器官会跟着
//   骨骼一起剧烈抖动（骨骼动画的位移直接传给了子物体）。
//
//   解决方案：把器官从骨骼下面拿出来，作为 MaleModel 的子物体
//   （或场景根），然后用本脚本"软跟随"骨骼的世界坐标 —— 加平滑后
//   器官会贴在胸腔/头部位置，但不会复制骨骼的高频抖动。
//
// 使用方法：
//   1. 把 Heart/Lung 拖出骨骼层级，放回 MaleModel 根下
//   2. 给 Heart/Lung/Brain 各挂一个本脚本
//   3. 在 Inspector 里把 Target Bone 拖入对应骨骼：
//      - Heart  → spine_03
//      - Lung   → spine_03
//      - Brain  → head
//   4. 调整 Position Offset 让器官贴在合适位置
// ============================================================
public class OrganFollow : MonoBehaviour
{
    [Header("跟随的骨骼")]
    [Tooltip("Heart/Lung 拖 spine_03，Brain 拖 head")]
    public Transform targetBone;

    [Header("自动校准：勾选后启动时自动用当前位置作为偏移")]
    [Tooltip("用法：先把器官手动拖到合适位置（在身体里面），再勾选这个，按 Play，脚本会记住这个相对位置")]
    public bool autoCalibrateOnStart = true;

    [Header("相对骨骼的偏移（局部坐标，自动校准开启时无需手填）")]
    public Vector3 positionOffset = Vector3.zero;

    [Header("是否跟随骨骼旋转")]
    public bool followRotation = true;
    public Vector3 rotationOffset = Vector3.zero;

    [Header("平滑度（0 = 完全跟随，0.95 = 极平滑/慢）")]
    [Range(0f, 0.99f)]
    public float positionSmoothing = 0.85f;

    [Range(0f, 0.99f)]
    public float rotationSmoothing = 0.85f;

    [Header("是否启用 Y 轴抑制（消除走路上下抖动）")]
    public bool dampenVerticalBobbing = true;
    [Range(0f, 1f)]
    public float verticalDampening = 0.9f; // 0=不抑制，1=完全锁住Y

    private Vector3 _smoothedPos;
    private Quaternion _smoothedRot;
    private bool _initialized = false;
    private float _baseY;
    private Quaternion _initialRotOffset;

    // 必须在 Awake 里做校准：此时 Animator 还没更新第一帧，
    // 骨骼仍保持 T-pose，InverseTransformPoint 算出的 offset 才正确
    void Awake()
    {
        if (autoCalibrateOnStart && targetBone != null)
        {
            // 把当前世界位置转成 骨骼局部坐标 的偏移
            positionOffset    = targetBone.InverseTransformPoint(transform.position);
            _initialRotOffset = Quaternion.Inverse(targetBone.rotation) * transform.rotation;
            Debug.Log($"[OrganFollow:{name}] 已校准 offset={positionOffset}");
        }
        else
        {
            _initialRotOffset = Quaternion.Euler(rotationOffset);
        }
    }

    void LateUpdate()
    {
        if (targetBone == null) return;

        // 目标位置 = 骨骼位置 + 局部坐标系下的偏移
        Vector3 targetPos = targetBone.TransformPoint(positionOffset);
        Quaternion targetRot = targetBone.rotation * _initialRotOffset;

        if (!_initialized)
        {
            _smoothedPos = targetPos;
            _smoothedRot = targetRot;
            _baseY = targetPos.y;
            _initialized = true;
        }
        else
        {
            // 指数平滑（帧率无关）
            float posT = 1f - Mathf.Pow(positionSmoothing, Time.deltaTime * 60f);
            float rotT = 1f - Mathf.Pow(rotationSmoothing, Time.deltaTime * 60f);
            _smoothedPos = Vector3.Lerp(_smoothedPos, targetPos, posT);
            _smoothedRot = Quaternion.Slerp(_smoothedRot, targetRot, rotT);
        }

        // Y 轴抑制：把高频上下抖动滤掉
        Vector3 finalPos = _smoothedPos;
        if (dampenVerticalBobbing)
        {
            // 慢速跟踪基准 Y（这样人物走路前进/下蹲依然能跟，但走路抖动滤掉）
            _baseY = Mathf.Lerp(_baseY, targetPos.y, Time.deltaTime * 1.5f);
            finalPos.y = Mathf.Lerp(_smoothedPos.y, _baseY, verticalDampening);
        }

        transform.position = finalPos;
        if (followRotation)
            transform.rotation = _smoothedRot;
    }
}
