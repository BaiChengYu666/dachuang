# 数字孪生器官模型 - Unity 操作手册

## 📦 已完成的工作（我已经帮你做好的）

```
people/Assets/Models/Organs/
├── Heart.glb              ← 心脏模型 (3.9MB, CC BY 4.0)
├── Lung.glb               ← 肺模型 (6.1MB, CC BY 4.0)
├── Brain.glb              ← 大脑模型 (12MB, CC BY 4.0)
├── ATTRIBUTION.txt        ← 模型署名/许可文件
└── READ_ME_FIRST.md       ← 本文档

people/Assets/Scripts/
├── GenderModelManager.cs       (原有，未改)
├── HealthDataModel.cs          ← 新：数据结构
├── HealthStatusColors.cs       ← 新：颜色编码
├── OrganStateController.cs     ← 新：器官颜色/脉冲控制
├── HeartbeatAnimator.cs        ← 新：心脏跳动动画
├── LungBreathingAnimator.cs    ← 新：肺呼吸动画
└── TwinDataReceiver.cs         ← 新：数据中枢（核心）
```

---

## 🛠️ 你需要在 Unity 里做的操作（30-60 分钟）

### 步骤 0：安装 glTFast 包（必须！）

**为什么**：Unity 2022.3 不能直接识别 .glb 文件，需要装一个官方包。

1. 打开 Unity，菜单栏 → **Window → Package Manager**
2. 左上角下拉选 **Unity Registry**
3. 右上角搜索框输入：`gltfast`
4. 找到 **glTFast** 点击右下角 **Install**
5. 安装完毕后**关闭** Package Manager

> 安装后，Unity 会自动重新导入 `Assets/Models/Organs/` 里的 `.glb` 文件，
> 你会看到 Heart.glb / Lung.glb / Brain.glb 旁边出现一个小三角形，可以展开看到 Mesh 子项。

---

### 步骤 1：把器官模型拖入场景

1. 在 Project 窗口打开 `Assets/Scenes/HealthMonitorScene.unity`（双击它）
2. 在 Hierarchy 里找到男性模型的根 GameObject（应该叫 `MaleModel` 或类似名字，是 GenderModelManager 里 maleModel 字段引用的那个）
3. 在 Project 窗口拖拽 **Heart.glb** 到 Hierarchy，**作为男性模型的子物体**（拖到男性模型下面）
4. 同样拖入 **Lung.glb** 和 **Brain.glb**

> 💡 拖入后器官可能在地板下面或者超大/超小，下一步调整。

---

### 步骤 2：调整器官的位置和大小

选中刚拖入的 **Heart**，在 Inspector 里设置 Transform：

| 器官 | Position (X, Y, Z) | Rotation (X, Y, Z) | Scale (X, Y, Z) | 说明 |
|---|---|---|---|---|
| Heart | (0, 1.45, 0.05) | (0, 0, 0) | (0.08, 0.08, 0.08) | 胸腔偏左 |
| Lung  | (0, 1.50, 0.0)  | (0, 0, 0) | (0.10, 0.10, 0.10) | 胸腔，包住心脏 |
| Brain | (0, 1.78, 0.0)  | (0, 0, 0) | (0.07, 0.07, 0.07) | 头部 |

> ⚠️ **这些数值是估算的**，需要你在 Scene 视图里微调。男性模型的高度不同会影响 Y 值。
> 调整方法：
> 1. 双击 Heart 让 Scene 视图聚焦它
> 2. 用 W 键（移动工具）拖动到胸腔位置
> 3. 用 R 键（缩放工具）调到合适大小
> 4. **目标**：心脏在胸腔中央偏左，肺在两侧包住心脏，大脑在头部内

---

### 步骤 3：让人体皮肤变半透明（关键！）

这样才能透过皮肤看到内部的器官。

1. 在 Hierarchy 里找到男性模型的皮肤 Mesh（通常是某个带 `SkinnedMeshRenderer` 的子物体，名字可能叫 `Body` / `Skin` / `Mesh`）
2. 在 Inspector 里展开它的 **Skinned Mesh Renderer** → **Materials**
3. 双击材质名进入材质 Inspector
4. 在材质上方 **Surface Type** 改为 **Transparent**
5. **Base Map** 旁边的颜色，把 Alpha 通道改为 **60-100**（数值越小越透明）

> 如果你用的是 URP，材质属性叫 Surface Type；
> 如果是 Built-in Render Pipeline，叫 Rendering Mode → 选 Fade 或 Transparent。

---

### 步骤 4：给器官挂脚本

#### 4.1 Heart（心脏）

1. 在 Hierarchy 选中 Heart GameObject
2. Inspector 底部 **Add Component** → 搜索 **OrganStateController** → 添加
3. 在 OrganStateController 组件里，**Organ Name** 字段填：`Heart`
4. 再次 Add Component → 搜索 **HeartbeatAnimator** → 添加
5. 保持默认参数即可（心率 75 bpm，跳动幅度 0.12）

#### 4.2 Lung（肺）

1. 选中 Lung GameObject
2. Add Component → **OrganStateController**，Organ Name 填 `Lung`
3. Add Component → **LungBreathingAnimator**，保持默认参数

#### 4.3 Brain（大脑）

1. 选中 Brain GameObject
2. Add Component → **OrganStateController**，Organ Name 填 `Brain`
3. （脑不需要呼吸/跳动动画）

---

### 步骤 5：创建数据中枢 GameObject

1. 在 Hierarchy 空白处右键 → **Create Empty**
2. 把新建的 GameObject 重命名为 **HealthDataManager**（必须叫这个名字，脚本里写死了）
3. 选中 HealthDataManager，Inspector → **Add Component** → **TwinDataReceiver**
4. 在 TwinDataReceiver 组件的 Inspector 里：
   - **Heart Object**：从 Hierarchy 拖 Heart 到这个槽位
   - **Lung Object**：从 Hierarchy 拖 Lung 到这个槽位
   - **Brain Object**：从 Hierarchy 拖 Brain 到这个槽位
   - **Backend Url**：默认 `http://localhost:8080`，部署时改成你的服务器地址
   - **Elderly Id**：默认 1
   - **Poll Interval**：5（秒）
   - **Enable Demo Mode**：✅ 勾选（无后端时自动循环演示）

---

### 步骤 6：编辑器测试（不需要后端就能看效果）

1. 点 Unity 顶部的 ▶️ Play 按钮运行场景
2. **观察心脏**：应该有规律的跳动动画
3. **观察肺**：应该有缓慢的呼吸起伏
4. **按键测试**（场景必须运行中，鼠标点 Game 视图获得焦点）：
   - 按 **Q** → 全部状态正常（绿色）
   - 按 **W** → 注意状态（黄色）
   - 按 **E** → 警告状态（橙色）
   - 按 **R** → 危险状态（红色 + 心脏脉冲闪烁）
5. 还有原有的快捷键：
   - 按 1/2/3 → 切换站立/走路/跌倒动画
   - 按 M/F → 切换男女模型
   - 按 D → 切换演示模式

---

### 步骤 7：构建 WebGL

1. **File → Build Settings**
2. 确认 Scenes In Build 列表里有 `HealthMonitorScene`，且勾选
3. Platform 选 **WebGL**（如果当前不是，点 Switch Platform，会等几分钟）
4. 点 **Build**，选择输出到 `WebGLBuild/` 文件夹（覆盖原来的）
5. 构建完成后用 `deploy_unity.sh` 上传到服务器

---

## ⚠️ 常见问题

### Q1: 拖入 .glb 后看不到模型 / 是个空物体
- 没装 glTFast 包。回到步骤 0。
- 装了之后还是空？右键 Heart.glb → Reimport

### Q2: 器官位置完全不对，跑到角色脚下了
- 男性模型可能用了不同的 Pivot 点
- 选中器官，把它做成男性模型某个子骨骼的子物体（如 `Spine_02`），位置会自动跟随骨骼

### Q3: 心脏不跳动
- HeartbeatAnimator 没挂到 Heart GameObject 上
- 或者 Time.timeScale 被改为 0 了（场景暂停）

### Q4: 颜色没变化
- OrganStateController 没找到 Renderer
- 检查 Heart GameObject 的子物体是否有 MeshRenderer 组件
- 检查材质 Shader 是否支持 _BaseColor 或 _Color 属性

### Q5: 玩家在 Game 视图里按 Q/W/E/R 没反应
- 鼠标先点击一下 Game 视图获得焦点
- 检查 Hierarchy 里是否有 HealthDataManager GameObject

### Q6: WebGL 构建失败
- 看 Console 报错。常见是 glTFast 包对 WebGL 有特殊要求
- 解决：Edit → Project Settings → Player → WebGL → Other Settings → 勾选 "Decompression Fallback"

---

## 🎬 演示效果说明

完成后你会看到：

1. **静态视觉**：男性人物半透明，胸腔里能看到红色心脏 + 粉色肺部，头部能看到大脑
2. **心脏跳动**：心脏周期性脉动（频率 = 真实心率）
3. **肺部呼吸**：肺缓慢起伏（频率 = 呼吸频率，血氧低时加快）
4. **状态变色**：
   - 正常 → 各器官保持本色 + 微弱绿光
   - 注意 → 黄色 + 中等亮度
   - 警告 → 橙色 + 强亮度
   - 危险 → 红色 + 强亮度 + **脉冲闪烁动画**
5. **后端联动**：每 5 秒拉取一次后端数据，根据 PhysiologicalData 自动更新器官
6. **JS 联动**：前端小程序可以通过 `window.unityUpdateHealth(jsonStr)` 实时推送数据

---

## 📝 后端需要做的配套工作（下一步）

1. 在 Spring Boot 后端新增 endpoint：
   ```
   GET /api/data/physiological/latest/{elderlyId}
   ```
   返回最新一条 PhysiologicalData，并附带 status 字段（normal/attention/warning/danger）

2. 返回 JSON 格式：
   ```json
   {
     "code": 200,
     "data": {
       "heartRate": 75,
       "bloodPressureHigh": 120,
       "bloodPressureLow": 80,
       "bloodOxygen": 98,
       "bodyTemperature": 36.5,
       "heartStatus": "normal",
       "bloodPressureStatus": "normal",
       "bloodOxygenStatus": "normal",
       "temperatureStatus": "normal",
       "overallStatus": "normal"
     }
   }
   ```

需要我帮你写后端代码吗？告诉我即可。
