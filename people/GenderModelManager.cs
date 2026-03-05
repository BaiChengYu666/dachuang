using UnityEngine;

/// <summary>
/// 性别模型管理器 - 根据用户性别切换模型
/// </summary>
public class GenderModelManager : MonoBehaviour
{
    [Header("男性模型")]
    public GameObject maleModel;
    public Animator maleAnimator;

    [Header("女性模型")]
    public GameObject femaleModel;
    public Animator femaleAnimator;

    [Header("当前设置")]
    public string userGender = "female"; // 从用户数据获取

    [Header("女性动画旋转修正")]
    [Tooltip("女性走路/摔倒时Z轴旋转补偿（向右歪则调负值，向左歪则调正值）")]
    public float femaleWalkFallZCorrection = -7f;

    private GameObject currentModel;
    private Animator currentAnimator;
    private string currentBehavior = "Standing";

    void Start()
    {
        // 根据用户性别激活对应模型
        SetupModelByGender(userGender);
    }

    /// <summary>
    /// 根据性别设置模型
    /// </summary>
    public void SetupModelByGender(string gender)
    {
        // 先隐藏所有模型
        maleModel.SetActive(false);
        femaleModel.SetActive(false);

        if (gender.ToLower() == "male" || gender == "男")
        {
            // 显示男性模型
            maleModel.SetActive(true);
            currentModel = maleModel;
            currentAnimator = maleAnimator;
            Debug.Log("已激活男性模型");
        }
        else
        {
            // 显示女性模型
            femaleModel.SetActive(true);
            currentModel = femaleModel;
            currentAnimator = femaleAnimator;
            Debug.Log("已激活女性模型");
        }

        // 默认播放站立动画
        PlayAnimation("Standing");
    }

    /// <summary>
    /// 播放动画
    /// </summary>
    public void PlayAnimation(string animationName)
    {
        if (currentAnimator == null) return;

        currentBehavior = animationName;

        // 女性模型走路/摔倒时修正Z轴偏转
        if (currentModel == femaleModel)
        {
            bool needCorrection = animationName == "Walking" || animationName == "WALKING"
                               || animationName == "Falling" || animationName == "FALLING";
            float zAngle = needCorrection ? femaleWalkFallZCorrection : 0f;
            femaleModel.transform.localEulerAngles = new Vector3(0f, 180f, zAngle);
        }

        switch (animationName)
        {
            case "Standing":
            case "STANDING":
                currentAnimator.SetTrigger("Stand");
                break;

            case "Walking":
            case "WALKING":
                currentAnimator.SetTrigger("Walk");
                break;

            case "Falling":
            case "FALLING":
                currentAnimator.SetTrigger("Fall");
                // 发送警报
                SendAlertToWeb("摔倒警报！");
                break;
        }

        Debug.Log($"播放动画: {animationName}");
    }

    /// <summary>
    /// 从网页接收性别信息
    /// </summary>
    public void SetGenderFromWeb(string gender)
    {
        Debug.Log("收到性别信息: " + gender);
        userGender = gender;
        SetupModelByGender(gender);
    }

    /// <summary>
    /// 从网页接收行为状态
    /// </summary>
    public void UpdateBehavior(string behavior)
    {
        Debug.Log("收到行为状态: " + behavior);
        PlayAnimation(behavior);
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
}
