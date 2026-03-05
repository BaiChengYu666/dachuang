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
    public float  pollInterval = 3f;   // 每 3 秒轮询一次

    // ---- 内部状态 ----
    private Animator currentAnimator;
    private string   lastActivity  = "";
    private bool     isFalling     = false;

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

        // 启动行为轮询
        StartCoroutine(BehaviorPollLoop());
    }

    // ======================================================
    // 行为轮询
    // ======================================================
    IEnumerator BehaviorPollLoop()
    {
        yield return new WaitForSeconds(2f); // 等场景稳定后再开始
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
                    ApplyBehavior(resp.data.activityType);
            }
            else
            {
                Debug.LogWarning("行为轮询失败: " + req.error);
            }
        }
    }

    void ApplyBehavior(string activityType)
    {
        if (string.IsNullOrEmpty(activityType)) return;
        if (activityType == lastActivity) return;   // 没变化，跳过

        lastActivity = activityType;
        Debug.Log("行为更新: " + activityType);

        bool falling = activityType.ToLower() == "falling" ||
                       activityType == "跌倒";

        // 切换动画
        switch (activityType.ToLower())
        {
            case "walking": case "步行":
                PlayAnimation("Walking"); break;
            case "falling": case "跌倒":
                PlayAnimation("Falling"); break;
            default:
                PlayAnimation("Standing"); break;
        }

        // 颜色 & 警报
        if (falling && !isFalling)
        {
            isFalling = true;
            SetModelColor(new Color(1f, 0.2f, 0.2f, 1f)); // 模型变红
            SendFallAlert("⚠️ 检测到跌倒！请立即查看！");
        }
        else if (!falling && isFalling)
        {
            isFalling = false;
            ResetModelColor();
        }
    }

    // ======================================================
    // 模型颜色
    // ======================================================
    void SetModelColor(Color color)
    {
        if (currentAnimator == null) return;
        foreach (var r in currentAnimator.GetComponentsInChildren<Renderer>())
        {
            foreach (var mat in r.materials)
            {
                if (mat.HasProperty("_Color")) mat.color = color;
            }
        }
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
                maleAnimator.applyRootMotion = false;   // 防止动画覆盖根节点旋转
            }
            else Debug.LogError("男性模型或Animator未配置！");
        }
        else
        {
            if (femaleModel != null && femaleAnimator != null)
            {
                femaleModel.SetActive(true);
                currentAnimator = femaleAnimator;
                femaleAnimator.applyRootMotion = false; // 防止动画覆盖根节点旋转
            }
            else Debug.LogError("女性模型或Animator未配置！");
        }

        PlayAnimation("Standing");
    }

    public void PlayAnimation(string animationName)
    {
        if (currentAnimator == null) { Debug.LogWarning("Animator未设置！"); return; }

        // 女性模型走路/摔倒时修正Z轴偏转
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
                currentAnimator.SetTrigger("Fall");
                break;
        }
    }

    // ======================================================
    // JS 通信桥
    // ======================================================
    void RegisterJSFunctions()
    {
#if UNITY_WEBGL && !UNITY_EDITOR
        Application.ExternalEval(@"
            // 允许小程序控制性别
            window.unitySetGender = function(gender) {
                unityInstance.SendMessage('ModelManager', 'SetGenderFromJS', gender);
            };
            // 允许小程序手动触发行为（测试用）
            window.unityUpdateBehavior = function(behavior) {
                unityInstance.SendMessage('ModelManager', 'UpdateBehaviorFromJS', behavior);
            };
            // 跌倒警报：通知微信小程序
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
    // JS 调用入口（由小程序 SendMessage 调用）
    // ======================================================
    public void SetGenderFromJS(string gender) { userGender = gender; SetupModelByGender(gender); }

    public void UpdateBehaviorFromJS(string behavior) { ApplyBehavior(behavior); }

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
    }
#endif
}
