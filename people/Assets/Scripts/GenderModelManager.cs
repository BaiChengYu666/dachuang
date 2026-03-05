using System.Collections;
using UnityEngine;
using UnityEngine.Networking;

// ---- JSON 反序列化结构 ----
[System.Serializable]
public class BehaviorApiResponse {
    public int code;
    public BehaviorPayload data;
}

[System.Serializable]
public class BehaviorPayload {
    public string activityType;   // "Walking" | "Standing" | "Falling"
    public float  movementSpeed;
}

public class GenderModelManager : MonoBehaviour
{
    [Header("男性模型")]
    public GameObject maleModel;
    public Animator   maleAnimator;

    [Header("女性模型")]
    public GameObject femaleModel;
    public Animator   femaleAnimator;

    [Header("默认性别(Editor测试)")]
    public string userGender = "female";

    [Header("女性动画旋转修正")]
    [Tooltip("走路/摔倒向右歪用正值修正，向左歪用负值（建议先试 7）")]
    [SerializeField] private float femaleWalkFallZCorrection = -7f;

    [Header("行为监测设置")]
    public string backendUrl   = "http://localhost:8080";
    public int    elderlyId    = 1;
    public float  pollInterval = 3f;

    [Header("演示模式（无后端时自动循环站立/步行）")]
    public bool  enableDemoMode       = true;
    public float demoStandDuration    = 4f;   // 站立持续秒数
    public float demoWalkDuration     = 5f;   // 步行持续秒数
    public int   demoTriggerFailCount = 3;    // 连续失败几次后启动演示

    // ---- 内部状态 ----
    private Animator currentAnimator;
    private string   lastActivity  = "";
    private bool     isFalling     = false;

    // ---- 演示模式状态 ----
    private int       _consecutiveFails = 0;
    private bool      _inDemoMode       = false;
    private Coroutine _demoCoroutine    = null;

    // ---- 生命周期 ----
    void Start()
    {
        Debug.Log("GenderModelManager 启动");

#if UNITY_WEBGL && !UNITY_EDITOR
        string urlGender = GetURLParameter("gender");
        if (!string.IsNullOrEmpty(urlGender)) userGender = urlGender;

        string urlId = GetURLParameter("elderlyId");
        if (!string.IsNullOrEmpty(urlId) && int.TryParse(urlId, out int parsedId))
            elderlyId = parsedId;
#endif

        SetupModelByGender(userGender);
        RegisterJSFunctions();
        StartCoroutine(BehaviorPollLoop());
    }

    // ======================================================
    // 行为轮询
    // ======================================================
    IEnumerator BehaviorPollLoop()
    {
        yield return new WaitForSeconds(2f);
        while (true)
        {
            yield return StartCoroutine(FetchLatestBehavior());
            yield return new WaitForSeconds(pollInterval);
        }
    }

    IEnumerator FetchLatestBehavior()
    {
        string url = $"{backendUrl}/api/data/behavior/latest/{elderlyId}";
        using (UnityWebRequest req = UnityWebRequest.Get(url))
        {
            req.timeout = 5;
            yield return req.SendWebRequest();

            if (req.result == UnityWebRequest.Result.Success)
            {
                BehaviorApiResponse resp =
                    JsonUtility.FromJson<BehaviorApiResponse>(req.downloadHandler.text);

                if (resp != null && resp.code == 200 && resp.data != null)
                {
                    // 后端有数据：停止演示模式，使用真实数据
                    _consecutiveFails = 0;
                    if (_inDemoMode) StopDemoMode();
                    ApplyBehavior(resp.data.activityType);
                }
                else
                {
                    OnBackendUnavailable();
                }
            }
            else
            {
                OnBackendUnavailable();
            }
        }
    }

    void OnBackendUnavailable()
    {
        _consecutiveFails++;
        Debug.LogWarning($"后端不可用（连续失败 {_consecutiveFails} 次）");
        if (enableDemoMode && !_inDemoMode && _consecutiveFails >= demoTriggerFailCount)
            StartDemoMode();
    }

    // ======================================================
    // 演示模式：自动循环 站立(4s) → 步行(5s) → 站立...
    // ======================================================
    void StartDemoMode()
    {
        if (_inDemoMode) return;
        _inDemoMode = true;
        Debug.Log("▶ 启动演示模式（无后端数据，自动循环站立/步行）");
        _demoCoroutine = StartCoroutine(DemoCycleCoroutine());
    }

    void StopDemoMode()
    {
        _inDemoMode = false;
        Debug.Log("■ 停止演示模式（后端已恢复）");
        if (_demoCoroutine != null)
        {
            StopCoroutine(_demoCoroutine);
            _demoCoroutine = null;
        }
    }

    IEnumerator DemoCycleCoroutine()
    {
        while (_inDemoMode)
        {
            ApplyBehavior("Standing");
            yield return new WaitForSeconds(demoStandDuration);
            if (!_inDemoMode) yield break;

            ApplyBehavior("Walking");
            yield return new WaitForSeconds(demoWalkDuration);
        }
    }

    // ======================================================
    // 应用行为：切动画 + 颜色 + 通知JS
    // ======================================================
    void ApplyBehavior(string activityType)
    {
        if (string.IsNullOrEmpty(activityType)) return;
        if (activityType == lastActivity) return;

        lastActivity = activityType;
        Debug.Log("行为更新: " + activityType);

        bool falling = activityType.ToLower() == "falling" || activityType == "跌倒";

        switch (activityType.ToLower())
        {
            case "walking": case "步行":
                PlayAnimation("Walking"); break;
            case "falling": case "跌倒":
                PlayAnimation("Falling"); break;
            default:
                PlayAnimation("Standing"); break;
        }

        if (falling && !isFalling)
        {
            isFalling = true;
            SetModelColor(new Color(1f, 0.2f, 0.2f, 1f));
            SendFallAlert("⚠️ 检测到跌倒！请立即查看！");
        }
        else if (!falling && isFalling)
        {
            isFalling = false;
            ResetModelColor();
        }

        // 通知前端小程序行为变化
        NotifyBehaviorChange(activityType);
    }

    // ======================================================
    // 通知小程序行为变化（postMessage + 全局回调）
    // ======================================================
    void NotifyBehaviorChange(string activityType)
    {
#if UNITY_WEBGL && !UNITY_EDITOR
        string escaped = activityType.Replace("'", "\\'");
        Application.ExternalEval(
            "try{" +
            "  if(typeof wx!=='undefined'&&wx.miniProgram){" +
            "    wx.miniProgram.postMessage({data:{type:'behavior',activityType:'" + escaped + "'}});" +
            "  }" +
            "  if(window.onUnityBehaviorChange)window.onUnityBehaviorChange('" + escaped + "');" +
            "}catch(e){}");
#endif
    }

    // ======================================================
    // 模型颜色
    // ======================================================
    void SetModelColor(Color color)
    {
        if (currentAnimator == null) return;
        foreach (var r in currentAnimator.GetComponentsInChildren<Renderer>())
            foreach (var mat in r.materials)
                if (mat.HasProperty("_Color")) mat.color = color;
    }

    void ResetModelColor() { SetModelColor(Color.white); }

    // ======================================================
    // 性别 & 动画
    // ======================================================
    public void SetupModelByGender(string gender)
    {
        bool isMale = (gender.ToLower() == "male" || gender == "男");
        if (maleModel   != null) maleModel.SetActive(false);
        if (femaleModel != null) femaleModel.SetActive(false);

        if (isMale)
        {
            if (maleModel != null && maleAnimator != null)
            {
                maleModel.SetActive(true);
                currentAnimator = maleAnimator;
                maleAnimator.applyRootMotion = false;
            }
            else Debug.LogError("男性模型或Animator未配置！");
        }
        else
        {
            if (femaleModel != null && femaleAnimator != null)
            {
                femaleModel.SetActive(true);
                currentAnimator = femaleAnimator;
                femaleAnimator.applyRootMotion = false;
            }
            else Debug.LogError("女性模型或Animator未配置！");
        }

        PlayAnimation("Standing");
    }

    public void PlayAnimation(string animationName)
    {
        if (currentAnimator == null) { Debug.LogWarning("Animator未设置！"); return; }

        if (currentAnimator == femaleAnimator && femaleModel != null)
        {
            string upper = animationName.ToUpper();
            bool needCorrection = upper == "WALKING" || upper == "WALK"
                                || upper == "FALLING" || upper == "FALL";
            float z = needCorrection ? femaleWalkFallZCorrection : 0f;
            femaleModel.transform.localEulerAngles = new Vector3(0f, 180f, z);
        }

        currentAnimator.ResetTrigger("Stand");
        currentAnimator.ResetTrigger("Walk");
        currentAnimator.ResetTrigger("Fall");

        switch (animationName.ToUpper())
        {
            case "STANDING": case "STAND": case "IDLE":
                currentAnimator.SetTrigger("Stand"); break;
            case "WALKING":  case "WALK":
                currentAnimator.SetTrigger("Walk");  break;
            case "FALLING":  case "FALL":
                currentAnimator.SetTrigger("Fall");  break;
        }
    }

    // ======================================================
    // JS 通信桥
    // ======================================================
    void RegisterJSFunctions()
    {
#if UNITY_WEBGL && !UNITY_EDITOR
        Application.ExternalEval(@"
            window.unitySetGender = function(gender) {
                unityInstance.SendMessage('ModelManager', 'SetGenderFromJS', gender);
            };
            window.unityUpdateBehavior = function(behavior) {
                unityInstance.SendMessage('ModelManager', 'UpdateBehaviorFromJS', behavior);
            };
            window.onUnityFallAlert = function(msg) {
                try {
                    if (typeof wx !== 'undefined' && wx.miniProgram) {
                        wx.miniProgram.postMessage({ data: { type: 'fall', message: msg } });
                    }
                } catch(e) {}
            };
            if (window.onUnityReady) window.onUnityReady();
        ");
#endif
    }

    void SendFallAlert(string message)
    {
#if UNITY_WEBGL && !UNITY_EDITOR
        string escaped = message.Replace("'", "\\'");
        Application.ExternalEval(
            "if(window.onUnityFallAlert){window.onUnityFallAlert('" + escaped + "');}");
#endif
    }

    // ======================================================
    // JS 调用入口
    // ======================================================
    public void SetGenderFromJS(string gender)         { userGender = gender; SetupModelByGender(gender); }
    public void UpdateBehaviorFromJS(string behavior)  { ApplyBehavior(behavior); }

    // ======================================================
    // 工具
    // ======================================================
    string GetURLParameter(string paramName)
    {
#if UNITY_WEBGL && !UNITY_EDITOR
        string url    = Application.absoluteURL;
        string search = paramName + "=";
        int idx = url.IndexOf(search);
        if (idx < 0) return "";
        int start = idx + search.Length;
        int end   = url.IndexOf('&', start);
        return end < 0 ? url.Substring(start) : url.Substring(start, end - start);
#else
        return "";
#endif
    }

#if UNITY_EDITOR
    void Update()
    {
        if (Input.GetKeyDown(KeyCode.Alpha1)) ApplyBehavior("Standing");
        if (Input.GetKeyDown(KeyCode.Alpha2)) ApplyBehavior("Walking");
        if (Input.GetKeyDown(KeyCode.Alpha3)) ApplyBehavior("Falling");
        if (Input.GetKeyDown(KeyCode.M))      SetupModelByGender("male");
        if (Input.GetKeyDown(KeyCode.F))      SetupModelByGender("female");
        // D键切换演示模式
        if (Input.GetKeyDown(KeyCode.D)) { if (_inDemoMode) StopDemoMode(); else StartDemoMode(); }
    }
#endif
}
