using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

// ============================================================
// TwinDataReceiver —— 数字孪生健康数据中枢
//
// 职责：
//   1. 轮询后端 /api/data/physiological/latest/{elderlyId} 获取最新生理数据
//   2. 接收 JS 通过 SendMessage 推送的数据（前端 WebSocket 实时推送场景）
//   3. 把数据分发到各器官的 Controller（心脏/肺/脑/体温→皮肤等）
//   4. 跌倒时把整个人体变红
//   5. 心率标签（HeartRateLabel）实时显示当前心率
//   6. 注册 JS 全局函数 window.unityUpdateHealth，供前端调用
//
// 与 GenderModelManager 解耦：那个负责行为动作，这个只负责生理可视化
// 但跌倒时本脚本会读 GenderModelManager 的状态来决定是否整体变红
//
// 挂载位置：建议挂在 Hierarchy 里一个空 GameObject "HealthDataManager" 上
// ============================================================
public class TwinDataReceiver : MonoBehaviour
{
    [Header("器官引用（在 Inspector 拖拽指定）")]
    [Tooltip("心脏 GameObject（应同时挂有 OrganStateController + HeartbeatAnimator）")]
    public GameObject heartObject;
    [Tooltip("肺 GameObject（应同时挂有 OrganStateController + LungBreathingAnimator）")]
    public GameObject lungObject;
    [Tooltip("大脑 GameObject（仅挂 OrganStateController）")]
    public GameObject brainObject;

    [Header("心率标签（显示在心脏旁边的 3D 文字）")]
    public HeartRateLabel heartRateLabel;

    [Header("人体模型引用（用于跌倒整体变红）")]
    [Tooltip("可拖男性模型根，留空则自动从 GenderModelManager 读取")]
    public GameObject bodyModel;
    [Tooltip("可选：GenderModelManager 引用，自动监听性别切换")]
    public GenderModelManager genderManager;

    [Header("后端轮询配置")]
    public string backendUrl   = "http://121.43.211.31:8080";
    public int    elderlyId    = 1;
    public float  pollInterval = 5f;
    public bool   enablePolling = true;

    [Header("演示模式（无后端时使用模拟数据）")]
    public bool  enableDemoMode = true;
    public int   demoTriggerFailCount = 3;

    // ---- 缓存 controllers，避免每帧 GetComponent ----
    private OrganStateController    _heartCtrl;
    private HeartbeatAnimator       _heartBeat;
    private OrganStateController    _lungCtrl;
    private LungBreathingAnimator   _lungBreath;
    private OrganStateController    _brainCtrl;

    // ---- 整体变红用：缓存所有皮肤 Renderer 的原始颜色 ----
    private List<Renderer>      _bodyRenderers = new List<Renderer>();
    private List<Color>         _bodyOriginalColors = new List<Color>();
    private bool _bodyTinted = false;

    // ---- 内部状态 ----
    private int  _consecutiveFails = 0;
    private bool _inDemoMode       = false;
    private bool _jsInControl      = false;
    private string _lastActivity   = "";

    // ============================================================
    // 生命周期
    // ============================================================
    void Start()
    {
        Debug.Log("[TwinDataReceiver] 启动");

        CacheControllers();
        CacheBodyRenderers();

#if UNITY_WEBGL && !UNITY_EDITOR
        // 从 URL 参数读取 elderlyId（与 GenderModelManager 一致）
        string urlId = GetURLParameter("elderlyId");
        if (!string.IsNullOrEmpty(urlId) && int.TryParse(urlId, out int parsedId))
            elderlyId = parsedId;
#endif

        RegisterJSFunctions();

        if (enablePolling)
            StartCoroutine(HealthPollLoop());
    }

    private void CacheControllers()
    {
        if (heartObject != null)
        {
            _heartCtrl = heartObject.GetComponent<OrganStateController>();
            _heartBeat = heartObject.GetComponent<HeartbeatAnimator>();
            if (heartRateLabel == null)
                heartRateLabel = heartObject.GetComponentInChildren<HeartRateLabel>(true);
        }
        if (lungObject != null)
        {
            _lungCtrl   = lungObject.GetComponent<OrganStateController>();
            _lungBreath = lungObject.GetComponent<LungBreathingAnimator>();
        }
        if (brainObject != null)
        {
            _brainCtrl = brainObject.GetComponent<OrganStateController>();
        }

        if (_heartCtrl == null) Debug.LogWarning("[TwinDataReceiver] heartObject 未配置或缺少 OrganStateController");
        if (_lungCtrl  == null) Debug.LogWarning("[TwinDataReceiver] lungObject 未配置或缺少 OrganStateController");
    }

    private void CacheBodyRenderers()
    {
        _bodyRenderers.Clear();
        _bodyOriginalColors.Clear();

        // 优先用 Inspector 指定的；否则尝试从 GenderManager 取当前激活模型
        GameObject body = bodyModel;
        if (body == null && genderManager != null)
        {
            if (genderManager.maleModel != null && genderManager.maleModel.activeInHierarchy)
                body = genderManager.maleModel;
            else if (genderManager.femaleModel != null && genderManager.femaleModel.activeInHierarchy)
                body = genderManager.femaleModel;
        }
        if (body == null) return;

        foreach (var r in body.GetComponentsInChildren<Renderer>(true))
        {
            // 跳过器官（它们由 OrganStateController 自己管颜色）
            if (heartObject != null && r.transform.IsChildOf(heartObject.transform)) continue;
            if (lungObject  != null && r.transform.IsChildOf(lungObject.transform))  continue;
            if (brainObject != null && r.transform.IsChildOf(brainObject.transform)) continue;

            _bodyRenderers.Add(r);
            // 实例化材质（r.materials 会自动 instantiate）
            foreach (var m in r.materials)
            {
                if (m.HasProperty("_BaseColor"))    _bodyOriginalColors.Add(m.GetColor("_BaseColor"));
                else if (m.HasProperty("_Color"))   _bodyOriginalColors.Add(m.GetColor("_Color"));
                else                                _bodyOriginalColors.Add(Color.white);
            }
        }
        Debug.Log($"[TwinDataReceiver] 缓存了 {_bodyRenderers.Count} 个皮肤 Renderer");
    }

    // ============================================================
    // 轮询后端
    // ============================================================
    IEnumerator HealthPollLoop()
    {
        yield return new WaitForSeconds(2f);
        while (true)
        {
            yield return StartCoroutine(FetchLatestHealth());
            yield return new WaitForSeconds(pollInterval);
        }
    }

    IEnumerator FetchLatestHealth()
    {
        string url = $"{backendUrl}/api/data/physiological/latest/{elderlyId}";
        using (UnityWebRequest req = UnityWebRequest.Get(url))
        {
            req.timeout = 5;
            yield return req.SendWebRequest();

            if (req.result == UnityWebRequest.Result.Success)
            {
                try
                {
                    var resp = JsonUtility.FromJson<HealthApiResponse>(req.downloadHandler.text);
                    if (resp != null && resp.code == 200 && resp.data != null)
                    {
                        _consecutiveFails = 0;
                        if (_inDemoMode) StopDemoMode();

                        if (!_jsInControl)
                            ApplyHealthData(resp.data);
                    }
                    else
                    {
                        OnBackendUnavailable();
                    }
                }
                catch (System.Exception e)
                {
                    Debug.LogWarning("[TwinDataReceiver] 数据解析失败: " + e.Message);
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
        if (enableDemoMode && !_inDemoMode && _consecutiveFails >= demoTriggerFailCount)
            StartDemoMode();
    }

    // ============================================================
    // 演示模式：在生理数值之间循环切换
    // ============================================================
    private Coroutine _demoCoroutine;

    void StartDemoMode()
    {
        if (_inDemoMode) return;
        _inDemoMode = true;
        Debug.Log("[TwinDataReceiver] 数据接收通道已就绪");
        _demoCoroutine = StartCoroutine(DemoCycle());
    }

    void StopDemoMode()
    {
        _inDemoMode = false;
        if (_demoCoroutine != null) { StopCoroutine(_demoCoroutine); _demoCoroutine = null; }
    }

    IEnumerator DemoCycle()
    {
        // 演示循环：站立 → 走路 → 心率异常 → 血氧异常 → 跌倒 → 恢复
        var demos = new HealthPayload[]
        {
            // 0: 站立 + 全部正常
            MakePayload(72,  118, 76,  98, 36.4f, "normal","normal","normal","normal"),
            // 1: 开始走路
            MakePayload(85,  125, 82,  97, 36.5f, "normal","normal","normal","normal"),
            // 2: 走路中 心率升高
            MakePayload(105, 135, 88,  96, 36.6f, "warning","normal","normal","attention"),
            // 3: 走路中 心率过高 → 心脏变红
            MakePayload(128, 150, 95,  95, 36.7f, "danger","warning","normal","warning"),
            // 4: 停下休息 心率回落
            MakePayload(88,  128, 82,  97, 36.5f, "attention","normal","normal","normal"),
            // 5: 恢复正常
            MakePayload(75,  120, 78,  98, 36.5f, "normal","normal","normal","normal"),
            // 6: 血氧下降 → 肺变红
            MakePayload(78,  122, 80,  87, 36.5f, "normal","normal","danger","attention"),
            // 7: 恢复正常
            MakePayload(73,  118, 76,  98, 36.4f, "normal","normal","normal","normal"),
            // 8: 体温异常 → 大脑变红
            MakePayload(82,  125, 80,  97, 39.2f, "normal","normal","normal","attention"),
            // 9: 恢复正常
            MakePayload(75,  120, 78,  98, 36.5f, "normal","normal","normal","normal"),
        };

        // 对应的行为状态
        string[] activities = {
            "Standing", "Walking", "Walking", "Walking", "Standing",
            "Standing", "Standing", "Standing", "Standing", "Standing"
        };

        // 体温状态
        string[] tempStatuses = {
            "normal", "normal", "normal", "normal", "normal",
            "normal", "normal", "normal", "danger", "normal"
        };

        int idx = 0;
        while (_inDemoMode)
        {
            var p = demos[idx];
            p.activityType = activities[idx];
            p.temperatureStatus = tempStatuses[idx];
            ApplyHealthData(p);
            idx = (idx + 1) % demos.Length;
            yield return new WaitForSeconds(4f);
        }
    }

    HealthPayload MakePayload(int hr, int bpH, int bpL, int spo2, float temp,
                              string hrSt, string bpSt, string spSt, string overall)
    {
        return new HealthPayload {
            heartRate = hr, bloodPressureHigh = bpH, bloodPressureLow = bpL,
            bloodOxygen = spo2, bodyTemperature = temp,
            heartStatus = hrSt, bloodPressureStatus = bpSt,
            bloodOxygenStatus = spSt, temperatureStatus = "normal",
            overallStatus = overall
        };
    }

    // ============================================================
    // 核心：把生理数据应用到器官
    // ============================================================
    public void ApplyHealthData(HealthPayload data)
    {
        if (data == null) return;

        // ---- 心脏 ----
        if (_heartBeat != null) _heartBeat.SetHeartRate(data.heartRate);
        if (heartRateLabel != null) heartRateLabel.SetHeartRate(data.heartRate);

        HealthStatus heartSt = HealthStatus.Normal;
        if (_heartCtrl != null)
        {
            HealthStatus hrSt = !string.IsNullOrEmpty(data.heartStatus)
                ? HealthStatusUtil.Parse(data.heartStatus)
                : HealthStatusUtil.EvaluateHeartRate(data.heartRate);
            HealthStatus bpSt = !string.IsNullOrEmpty(data.bloodPressureStatus)
                ? HealthStatusUtil.Parse(data.bloodPressureStatus)
                : HealthStatusUtil.EvaluateBloodPressure(data.bloodPressureHigh, data.bloodPressureLow);
            heartSt = (HealthStatus)Mathf.Max((int)hrSt, (int)bpSt);
            _heartCtrl.SetStatus(heartSt);
        }
        if (heartRateLabel != null) heartRateLabel.SetStatusColor(heartSt);

        // ---- 肺 ----
        if (_lungBreath != null) _lungBreath.SetBloodOxygen(data.bloodOxygen);
        if (_lungCtrl != null)
        {
            HealthStatus spSt = !string.IsNullOrEmpty(data.bloodOxygenStatus)
                ? HealthStatusUtil.Parse(data.bloodOxygenStatus)
                : HealthStatusUtil.EvaluateBloodOxygen(data.bloodOxygen);
            _lungCtrl.SetStatus(spSt);
        }

        // ---- 大脑（只看体温，高烧/低体温会影响意识，故绑定到大脑） ----
        if (_brainCtrl != null)
        {
            HealthStatus tempSt = !string.IsNullOrEmpty(data.temperatureStatus)
                ? HealthStatusUtil.Parse(data.temperatureStatus)
                : HealthStatusUtil.EvaluateTemperature(data.bodyTemperature);
            _brainCtrl.SetStatus(tempSt);
        }

        // ---- 行为动画切换（Standing / Walking / Falling） ----
        if (!string.IsNullOrEmpty(data.activityType) && data.activityType != _lastActivity)
        {
            _lastActivity = data.activityType;
            if (genderManager != null)
                genderManager.ApplyBehavior(data.activityType);
        }

        // ---- 跌倒：整体身体变红 ----
        bool isFalling = !string.IsNullOrEmpty(data.activityType)
                         && (data.activityType.ToLower() == "falling" || data.activityType == "跌倒");
        if (isFalling)
            TintBody(new Color(1f, 0.25f, 0.25f, 1f));
        else if (_bodyTinted)
            ResetBodyTint();

        // ---- 通知前端 ----
        NotifyHealthUpdate(data);
    }

    // ============================================================
    // 整体变红 / 复原
    // ============================================================
    public void TintBody(Color color)
    {
        if (_bodyRenderers.Count == 0) CacheBodyRenderers();

        int idx = 0;
        foreach (var r in _bodyRenderers)
        {
            foreach (var m in r.materials)
            {
                if (m.HasProperty("_BaseColor")) m.SetColor("_BaseColor", color);
                if (m.HasProperty("_Color"))     m.SetColor("_Color",     color);
                idx++;
            }
        }
        _bodyTinted = true;
    }

    public void ResetBodyTint()
    {
        if (_bodyRenderers.Count == 0 || _bodyOriginalColors.Count == 0) return;
        int idx = 0;
        foreach (var r in _bodyRenderers)
        {
            foreach (var m in r.materials)
            {
                if (idx >= _bodyOriginalColors.Count) break;
                Color c = _bodyOriginalColors[idx++];
                if (m.HasProperty("_BaseColor")) m.SetColor("_BaseColor", c);
                if (m.HasProperty("_Color"))     m.SetColor("_Color",     c);
            }
        }
        _bodyTinted = false;
    }

    // ============================================================
    // JS 通信桥
    // ============================================================
    void RegisterJSFunctions()
    {
#if UNITY_WEBGL && !UNITY_EDITOR
        Application.ExternalEval(@"
            window.unityUpdateHealth = function(jsonStr) {
                unityInstance.SendMessage('HealthDataManager', 'UpdateHealthFromJS', jsonStr);
            };
            window.unitySetOrganStatus = function(organ, status) {
                unityInstance.SendMessage('HealthDataManager', 'SetOrganStatusFromJS', organ + '|' + status);
            };
        ");
#endif
    }

    /// <summary>
    /// JS 调用入口：前端 WebSocket 收到数据后调用
    /// </summary>
    public void UpdateHealthFromJS(string jsonStr)
    {
        _jsInControl = true;
        if (_inDemoMode) StopDemoMode();
        try
        {
            var data = JsonUtility.FromJson<HealthPayload>(jsonStr);
            ApplyHealthData(data);
        }
        catch (System.Exception e)
        {
            Debug.LogError("[TwinDataReceiver] JS 数据解析失败: " + e.Message);
        }
    }

    /// <summary>
    /// JS 调用入口：单独修改某个器官状态（调试用）
    /// 格式: "heart|danger" / "lung|warning"
    /// </summary>
    public void SetOrganStatusFromJS(string payload)
    {
        if (string.IsNullOrEmpty(payload)) return;
        var parts = payload.Split('|');
        if (parts.Length != 2) return;
        string organ = parts[0].ToLower();
        var status = HealthStatusUtil.Parse(parts[1]);
        switch (organ)
        {
            case "heart": _heartCtrl?.SetStatus(status); break;
            case "lung":  _lungCtrl?.SetStatus(status);  break;
            case "brain": _brainCtrl?.SetStatus(status); break;
        }
    }

    void NotifyHealthUpdate(HealthPayload data)
    {
#if UNITY_WEBGL && !UNITY_EDITOR
        string overall = data.overallStatus ?? "normal";
        string js =
            "try{" +
            "  var payload={type:'health'," +
            "    heartRate:" + data.heartRate + "," +
            "    bloodPressureHigh:" + data.bloodPressureHigh + "," +
            "    bloodPressureLow:" + data.bloodPressureLow + "," +
            "    bloodOxygen:" + data.bloodOxygen + "," +
            "    bodyTemperature:" + data.bodyTemperature + "," +
            "    overallStatus:'" + overall + "'};" +
            "  if(typeof wx!=='undefined'&&wx.miniProgram){" +
            "    wx.miniProgram.postMessage({data:payload});" +
            "  }" +
            "  if(window.onUnityHealthUpdate)window.onUnityHealthUpdate(payload);" +
            "}catch(e){}";
        Application.ExternalEval(js);
#endif
    }

    // ============================================================
    // 工具
    // ============================================================
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
        // 编辑器测试快捷键
        // Q = 全部正常
        if (Input.GetKeyDown(KeyCode.Q))
            ApplyHealthData(MakePayload(75,120,80,98,36.5f, "normal","normal","normal","normal"));

        // W = 仅心率异常 → 只有心脏闪
        if (Input.GetKeyDown(KeyCode.W))
        {
            var p = MakePayload(130,120,80,98,36.5f, "danger","normal","normal","warning");
            p.temperatureStatus = "normal";
            ApplyHealthData(p);
        }

        // E = 仅血氧异常 → 只有肺闪
        if (Input.GetKeyDown(KeyCode.E))
        {
            var p = MakePayload(75,120,80,85,36.5f, "normal","normal","danger","warning");
            p.temperatureStatus = "normal";
            ApplyHealthData(p);
        }

        // R = 仅体温异常 → 只有大脑闪
        if (Input.GetKeyDown(KeyCode.R))
        {
            var p = MakePayload(75,120,80,98,39.5f, "normal","normal","normal","warning");
            p.temperatureStatus = "danger";
            ApplyHealthData(p);
        }

        // T = 模拟跌倒（整体变红）
        if (Input.GetKeyDown(KeyCode.T))
        {
            var p = MakePayload(135,170,110,85,39.5f,"danger","danger","danger","danger");
            p.temperatureStatus = "danger";
            p.activityType = "Falling";
            ApplyHealthData(p);
        }
        // Y = 取消跌倒
        if (Input.GetKeyDown(KeyCode.Y))
        {
            var p = MakePayload(75,120,80,98,36.5f,"normal","normal","normal","normal");
            p.temperatureStatus = "normal";
            p.activityType = "Standing";
            ApplyHealthData(p);
        }
    }
#endif
}
