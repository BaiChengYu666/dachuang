using UnityEngine;

/// <summary>
/// 行为姿态控制器 - 根据后端数据切换动画
/// </summary>
public class BehaviorController : MonoBehaviour
{
    // 引用Animator组件
    private Animator animator;
    
    // 当前行为状态
    private string currentBehavior = "Standing";
    
    void Start()
    {
        animator = GetComponent<Animator>();
        
        // 默认播放站立动画
        PlayAnimation("Standing");
        
        // 启动定时检查
        InvokeRepeating("CheckBehaviorFromWeb", 0f, 2f);  // 每2秒检查一次
    }
    
    /// <summary>
    /// 从网页获取当前行为状态
    /// </summary>
    void CheckBehaviorFromWeb()
    {
        #if UNITY_WEBGL && !UNITY_EDITOR
        // 调用JavaScript获取行为状态
        Application.ExternalEval(@"
            if (window.getCurrentBehavior) {
                var behavior = window.getCurrentBehavior();
                unityInstance.SendMessage('BehaviorController', 'UpdateBehavior', behavior);
            }
        ");
        #endif
    }
    
    /// <summary>
    /// 更新行为状态（由网页调用）
    /// </summary>
    public void UpdateBehavior(string behavior)
    {
        Debug.Log("接收到行为状态: " + behavior);
        
        if (currentBehavior != behavior)
        {
            currentBehavior = behavior;
            PlayAnimation(behavior);
        }
    }
    
    /// <summary>
    /// 播放对应动画
    /// </summary>
    void PlayAnimation(string behavior)
    {
        switch (behavior)
        {
            case "Standing":
            case "STANDING":
                animator.SetTrigger("Stand");
                Debug.Log("播放站立动画");
                break;
                
            case "Walking":
            case "WALKING":
                animator.SetTrigger("Walk");
                Debug.Log("播放行走动画");
                break;
                
            case "Falling":
            case "FALLING":
                animator.SetTrigger("Fall");
                Debug.Log("播放摔倒动画");
                // 发送警报到前端
                SendAlertToWeb("摔倒警报！");
                break;
                
            default:
                Debug.LogWarning("未知行为: " + behavior);
                break;
        }
    }
    
    /// <summary>
    /// 发送消息到网页
    /// </summary>
    void SendAlertToWeb(string message)
    {
        #if UNITY_WEBGL && !UNITY_EDITOR
        Application.ExternalEval("if (window.onUnityAlert) { window.onUnityAlert('" + message + "'); }");
        #endif
    }
    
    // 测试用：手动切换行为
    void Update()
    {
        #if UNITY_EDITOR
        if (Input.GetKeyDown(KeyCode.Alpha1))
            PlayAnimation("Standing");
        if (Input.GetKeyDown(KeyCode.Alpha2))
            PlayAnimation("Walking");
        if (Input.GetKeyDown(KeyCode.Alpha3))
            PlayAnimation("Falling");
        #endif
    }
}
