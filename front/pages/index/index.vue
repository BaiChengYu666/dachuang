<template>
  <view class="container">

    <!-- 顶部轮播图 -->
    <swiper
      class="banner-swiper"
      indicator-dots
      autoplay
      interval="3000"
      duration="500"
      circular
      indicator-color="rgba(255,255,255,0.5)"
      indicator-active-color="#ffffff">
      <swiper-item v-for="(img, i) in bannerImages" :key="i">
        <image class="banner-img" :src="img" mode="aspectFill"></image>
      </swiper-item>
    </swiper>

    <!-- 数字孪生 3D 模型 -->
    <view class="card unity-card">
      <view class="card-header">
        <view class="card-title-row">
          <view class="icon-wrap green-bg">
            <text class="card-icon">🧬</text>
          </view>
          <text class="card-title">数字孪生 3D 模型</text>
        </view>
          </view>
      <view class="unity-frame">
        <!-- #ifdef H5 -->
        <web-view
          v-if="unityUrl"
          :src="unityUrl"
          class="unity-webview"
          @load="onUnityLoad"
          @error="onUnityError">
        </web-view>
        <view v-if="unityLoading" class="unity-loading">
          <view class="loading-spin"></view>
          <text class="loading-txt">3D 模型加载中...</text>
        </view>
        <!-- #endif -->
        <!-- #ifdef MP-WEIXIN -->
        <view class="unity-preview" @click="goToUnity">
          <image class="unity-preview-img" src="/static/images/unity-preview.png" mode="aspectFill"></image>
          <view class="unity-preview-mask">
            <text class="unity-preview-btn">点击查看 3D 模型</text>
          </view>
        </view>
        <!-- #endif -->
        <!-- 心率浮层 -->
        <view class="hr-overlay" :class="{beat: heartBeat}">
          <text class="hr-icon">❤️</text>
          <text class="hr-val">{{ Math.round(healthData.heartRate) }}</text>
          <text class="hr-unit">bpm</text>
        </view>
        <!-- 连接状态 -->
        <view class="conn-badge" :class="backendConnected ? 'conn-ok' : 'conn-off'">
          <text class="conn-dot">●</text>
          <text class="conn-label">{{ backendConnected ? '已连接' : '模拟数据' }}</text>
        </view>
        <!-- 行为状态角标 -->
        <view class="beh-overlay" :class="isFalling ? 'beh-falling' : ''">
          <text class="beh-overlay-text">{{ behaviorIcon }} {{ behaviorLabel }}</text>
        </view>
      </view>
    </view>

    <!-- 实时健康数据卡片 -->
    <view class="section-label">
      <text class="section-title">实时健康数据</text>
      <text class="section-time" :class="{'flash-label': dataFlash}">{{ dataFlash ? '✓ 已更新' : (backendConnected ? '实时同步' : '模拟数据') }}</text>
    </view>
    <view class="health-grid">

      <view class="health-card" :class="{flash: dataFlash}" @click="goToDetail('heartRate')">
        <view class="hc-icon-wrap" style="background:#FFF0F0;">
          <text class="hc-icon">❤️</text>
        </view>
        <text class="hc-label">心率</text>
        <view class="hc-value-row">
          <text class="hc-value">{{ Math.round(healthData.heartRate) }}</text>
          <text class="hc-unit">bpm</text>
        </view>
        <view class="hc-status" :class="getHeartRateStatus(healthData.heartRate).cls">
          <text>{{ getHeartRateStatus(healthData.heartRate).text }}</text>
        </view>
      </view>

      <view class="health-card" :class="{flash: dataFlash}" @click="goToDetail('bloodPressure')">
        <view class="hc-icon-wrap" style="background:#F0EEFF;">
          <text class="hc-icon">🩺</text>
        </view>
        <text class="hc-label">血压</text>
        <view class="hc-value-row">
          <text class="hc-value" style="font-size:34rpx;">{{ Math.round(healthData.bloodPressure.high) }}/{{ Math.round(healthData.bloodPressure.low) }}</text>
        </view>
        <view class="hc-status" :class="getBPStatus(healthData.bloodPressure).cls">
          <text>{{ getBPStatus(healthData.bloodPressure).text }}</text>
        </view>
      </view>

      <view class="health-card" :class="{flash: dataFlash}" @click="goToDetail('bloodOxygen')">
        <view class="hc-icon-wrap" style="background:#E8F4FD;">
          <text class="hc-icon">💧</text>
        </view>
        <text class="hc-label">血氧</text>
        <view class="hc-value-row">
          <text class="hc-value">{{ Number(healthData.bloodOxygen).toFixed(1) }}</text>
          <text class="hc-unit">%</text>
        </view>
        <view class="hc-status" :class="getOxygenStatus(healthData.bloodOxygen).cls">
          <text>{{ getOxygenStatus(healthData.bloodOxygen).text }}</text>
        </view>
      </view>

      <view class="health-card" :class="{flash: dataFlash}" @click="goToDetail('temperature')">
        <view class="hc-icon-wrap" style="background:#FFF8E1;">
          <text class="hc-icon">🌡️</text>
        </view>
        <text class="hc-label">体温</text>
        <view class="hc-value-row">
          <text class="hc-value">{{ Number(healthData.temperature).toFixed(1) }}</text>
          <text class="hc-unit">°C</text>
        </view>
        <view class="hc-status" :class="getTempStatus(healthData.temperature).cls">
          <text>{{ getTempStatus(healthData.temperature).text }}</text>
        </view>
      </view>

    </view>

    <!-- 行为状态监测 -->
    <view class="card">
      <view class="card-header">
        <view class="card-title-row">
          <view class="icon-wrap blue-bg">
            <text class="card-icon">🚶</text>
          </view>
          <text class="card-title">行为状态监测</text>
        </view>
        <text class="more-btn" @click="goToPage('/pages/behavior/behavior')">更多 ›</text>
      </view>
      <view class="behavior-row">
        <view class="beh-item">
          <text class="beh-icon">🎯</text>
          <text class="beh-val">{{ behavior.activity }}</text>
          <text class="beh-key">当前活动</text>
        </view>
        <view class="beh-divider"></view>
        <view class="beh-item">
          <text class="beh-icon">⏱️</text>
          <text class="beh-val">{{ behavior.duration }}</text>
          <text class="beh-key">活动时长</text>
        </view>
        <view class="beh-divider"></view>
        <view class="beh-item">
          <text class="beh-icon">👟</text>
          <text class="beh-val">{{ behavior.steps }}</text>
          <text class="beh-key">今日步数</text>
        </view>
      </view>
    </view>

    <!-- 环境参数监测 -->
    <view class="card">
      <view class="card-header">
        <view class="card-title-row">
          <view class="icon-wrap teal-bg">
            <text class="card-icon">🌿</text>
          </view>
          <text class="card-title">环境参数监测</text>
        </view>
        <text class="more-btn" @click="goToPage('/pages/environment/environment')">更多 ›</text>
      </view>
      <view class="env-grid">
        <view class="env-item">
          <view class="env-icon-wrap" style="background:#FFF3E0;">
            <text class="env-icon">🌡️</text>
          </view>
          <text class="env-val">24°C</text>
          <text class="env-key">室温</text>
        </view>
        <view class="env-item">
          <view class="env-icon-wrap" style="background:#E3F2FD;">
            <text class="env-icon">💧</text>
          </view>
          <text class="env-val">55%</text>
          <text class="env-key">湿度</text>
        </view>
        <view class="env-item">
          <view class="env-icon-wrap" style="background:#E8F5E9;">
            <text class="env-icon">🌬️</text>
          </view>
          <text class="env-val">优</text>
          <text class="env-key">空气</text>
        </view>
        <view class="env-item">
          <view class="env-icon-wrap" style="background:#FFFDE7;">
            <text class="env-icon">💡</text>
          </view>
          <text class="env-val">良好</text>
          <text class="env-key">光照</text>
        </view>
      </view>
    </view>

    <view class="bottom-space"></view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      bannerImages: [
        '/static/banner/029FADDCE802270E4A373AE3F9B25CAD.png',
        '/static/banner/0E9066A4A5EED5E945EA71883206267B.png',
        '/static/banner/B0A3AADF2A6364FB14EA4A290275A477.png',
        '/static/banner/BB05C7B2A05CF0E79DC5781EFCD04836.png',
        '/static/banner/BC217E1D4CA2E98A3F40334A204BF2A0.png'
      ],
      healthStatus: 'normal',
      statusText: '状态良好',
      greeting: '你好',
      userInfo: {
        username: '',
        nickname: '',
        gender: 'male',
        avatarUrl: '/static/default-avatar.webp'
      },
      unityUrl: '',
      unityLoading: true,
      elderlyId: 1, // 会在 loadUserInfo 中从存储读取
      backendConnected: false,
      healthData: {
        heartRate: 72,
        bloodPressure: { high: 125, low: 80 },
        bloodOxygen: 98,
        temperature: 36.5
      },
      behavior: {
        activity: '站立',
        duration: '8分钟',
        steps: 3256
      },
      riskScore: 15,
      riskLevel: 'low',
      riskText: '低风险',
      healthAdvice: '保持规律作息，适当增加户外活动',
      heartBeat: false,
      dataFlash: false,

      // 行为状态
      currentBehavior: 'Standing',
      isFalling: false
    }
  },

  computed: {
    behaviorLabel() {
      switch ((this.currentBehavior || '').toLowerCase()) {
        case 'walking': case '步行': return '步行中'
        case 'falling': case '跌倒': return '跌倒!'
        default: return '站立中'
      }
    },
    behaviorIcon() {
      switch ((this.currentBehavior || '').toLowerCase()) {
        case 'walking': case '步行': return '🚶'
        case 'falling': case '跌倒': return '⚠️'
        default: return '🧍'
      }
    }
  },

  onLoad() {
    this.setGreeting()
    this.checkLogin()
  },

  onShow() {
    this.checkLogin()
    this.syncTabBar()
    // 从全局状态同步最新行为（切回首页时立即刷新）
    try {
      const app = getApp()
      if (app && app.globalData && app.globalData.currentBehavior) {
        this.applyBehavior(app.globalData.currentBehavior)
      }
    } catch (e) {}
    if (!this._timer) {
      this.startHealthDataUpdate()
    }
    if (!this._behaviorInterval) {
      this.startBehaviorPoll()
    }
    if (!this._globalSyncInterval) {
      this.startGlobalDataSync()
    }
    // 后端未连接 + Unity 已加载时才启动演示模式
    if (!this.unityLoading && !this._inDemoMode && !this.backendConnected) {
      this.startDemoMode()
    }
    this.loadBackendData()
  },

  onHide() {
    if (this._timer) {
      clearInterval(this._timer)
      this._timer = null
    }
    if (this._globalSyncInterval) {
      clearInterval(this._globalSyncInterval)
      this._globalSyncInterval = null
    }
    this.stopDemoMode()
  },

  onUnload() {
    if (this._timer) {
      clearInterval(this._timer)
      this._timer = null
    }
    if (this._globalSyncInterval) {
      clearInterval(this._globalSyncInterval)
      this._globalSyncInterval = null
    }
    this.stopDemoMode()
  },

  methods: {
    syncTabBar() {
      const pages = getCurrentPages()
      const page = pages[pages.length - 1]
      if (page && page.getTabBar) {
        page.getTabBar()?.setSelected?.(0)
      }
    },

    setGreeting() {
      const h = new Date().getHours()
      if (h < 6) this.greeting = '夜深了'
      else if (h < 12) this.greeting = '早上好'
      else if (h < 18) this.greeting = '下午好'
      else this.greeting = '晚上好'
    },

    checkLogin() {
      try {
        const isLogin = uni.getStorageSync('isLogin')
        const userInfo = uni.getStorageSync('userInfo')
        if (!isLogin || !userInfo) {
          uni.reLaunch({ url: '/pages/login/login' })
          return
        }
        this.loadUserInfo()
        this.loadBackendData()
        this.startHealthDataUpdate()
        this.startBehaviorPoll()
      } catch (e) {
        uni.reLaunch({ url: '/pages/login/login' })
      }
    },

    loadUserInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) {
          this.userInfo = userInfo
          if (!this.userInfo.gender || this.userInfo.gender === '未知') {
            this.userInfo.gender = 'male'
          }
          // 从存储中读取 elderlyId
          if (userInfo.elderlyId) {
            this.elderlyId = userInfo.elderlyId
          }
        }
        const gender = this.userInfo.gender
        this.unityUrl = `http://121.43.211.31:8081/index.html?gender=${gender}`
      } catch (e) {
        console.error('读取用户信息失败:', e)
      }
    },

    onUnityLoad() {
      this.unityLoading = false
      // Unity 加载完成后拉取后端数据，后端不可用时才启动演示
      setTimeout(() => {
        if (!this.backendConnected) this.startDemoMode()
      }, 3000)
    },
    onUnityError() { this.unityLoading = false },

    goToUnity() {
      // #ifdef MP-WEIXIN
      uni.navigateTo({ url: '/pages/unity/unity' })
      // #endif
    },

    async loadBackendData() {
      try {
        const res = await new Promise((resolve, reject) => {
          uni.request({
            url: `http://121.43.211.31:8080/api/data/latest/${this.elderlyId}`,
            method: 'GET',
            success: resolve,
            fail: reject
          })
        })
        if (res.statusCode === 200 && res.data.code === 200) {
          const data = res.data.data
          if (data && data.physiological) {
            this.healthData.heartRate = data.physiological.heartRate || 72
            this.healthData.bloodPressure.high = data.physiological.bloodPressureHigh || 125
            this.healthData.bloodPressure.low = data.physiological.bloodPressureLow || 80
            this.healthData.bloodOxygen = data.physiological.bloodOxygen || 98
            this.healthData.temperature = data.physiological.bodyTemperature || 36.5
            this.backendConnected = true
            // 后端已连接，停止前端演示模式，使用真实数据
            if (this._inDemoMode) this.stopDemoMode()
            this.updateHealthStatus()
          } else {
            this.backendConnected = false
          }
        } else {
          this.backendConnected = false
        }
      } catch (e) {
        this.backendConnected = false
      }
    },

    updateHealthStatus() {
      const hr = this.healthData.heartRate
      const o2 = this.healthData.bloodOxygen
      const temp = this.healthData.temperature
      if (hr > 100 || hr < 55 || o2 < 95 || temp > 37.5) {
        this.healthStatus = 'warning'
        this.statusText = '请注意'
      } else {
        this.healthStatus = 'normal'
        this.statusText = '状态良好'
      }
    },

    startHealthDataUpdate() {
      if (this._timer) clearInterval(this._timer)
      this._timer = setInterval(async () => {
        if (this.backendConnected) {
          // 同时拉取健康+行为数据，保持两者同步
          await this.loadBackendData()
          this.fetchBehaviorData()
        } else {
          this.simulateHealthChange()
        }
        this.dataFlash = true
        setTimeout(() => { this.dataFlash = false }, 800)
        this.heartBeat = true
        setTimeout(() => { this.heartBeat = false }, 300)
      }, 5000)
    },

    // 后端已连接：在真实数据基础上叠加小幅抖动，模拟传感器实时波动
    applySensorJitter() {
      const d = this.healthData
      d.heartRate          = Math.max(55,  Math.min(110, d.heartRate          + Math.round((Math.random() - 0.5) * 6)))
      d.bloodPressure.high = Math.max(90,  Math.min(160, d.bloodPressure.high + Math.round((Math.random() - 0.5) * 6)))
      d.bloodPressure.low  = Math.max(55,  Math.min(100, d.bloodPressure.low  + Math.round((Math.random() - 0.5) * 4)))
      d.bloodOxygen        = Math.round(Math.max(93, Math.min(100, d.bloodOxygen   + (Math.random() - 0.5) * 1.5)) * 10) / 10
      d.temperature        = Math.round(Math.max(35.5,Math.min(38, d.temperature   + (Math.random() - 0.5) * 0.4)) * 10) / 10
      this.updateHealthStatus()
    },

    // 后端未连接：模拟数据，变化幅度足够让人肉眼看出来
    simulateHealthChange() {
      const d = this.healthData
      d.heartRate          = Math.round(Math.max(60,  Math.min(95,  d.heartRate          + (Math.random() - 0.5) * 8)))
      d.bloodOxygen        = Math.round(Math.max(95,  Math.min(100, d.bloodOxygen        + (Math.random() - 0.5) * 1.5)) * 10) / 10
      d.temperature        = Math.round(Math.max(36,  Math.min(37.2,d.temperature        + (Math.random() - 0.5) * 0.4)) * 10) / 10
      d.bloodPressure.high = Math.round(Math.max(110, Math.min(140, d.bloodPressure.high + (Math.random() - 0.5) * 8)))
      d.bloodPressure.low  = Math.round(Math.max(70,  Math.min(90,  d.bloodPressure.low  + (Math.random() - 0.5) * 5)))
    },

    // ---- 行为轮询（已合并到 startHealthDataUpdate 中，此处仅供首次加载调用） ----
    startBehaviorPoll() {
      // 行为数据现在和健康数据同步拉取（每5秒），不再单独轮询
      this.fetchBehaviorData()
    },

    fetchBehaviorData() {
      uni.request({
        url: `http://121.43.211.31:8080/api/data/behavior/latest/${this.elderlyId}`,
        method: 'GET',
        success: (res) => {
          if (res.statusCode === 200 && res.data && res.data.code === 200 && res.data.data) {
            this.applyBehavior(res.data.data.activityType)
          }
        },
        fail: () => {}
      })
    },

    applyBehavior(activityType) {
      if (!activityType) return
      this.currentBehavior = activityType

      // 同步更新行为卡片
      const labelMap = { walking: '步行', standing: '站立', falling: '跌倒' }
      this.behavior.activity = labelMap[(activityType || '').toLowerCase()] || this.behavior.activity

      // 步行时步数递增（每次调用 +8~15 步，模拟真实步频）
      if (activityType.toLowerCase() === 'walking') {
        this.behavior.steps += Math.floor(Math.random() * 8) + 8
      }

      // 推送到全局状态（同步行为页时间轴）
      try {
        const app = getApp()
        if (app && app.pushBehavior) app.pushBehavior(activityType)
        if (app && app.getBehaviorDuration) this.behavior.duration = app.getBehaviorDuration()
      } catch (e) {}

      const falling = activityType.toLowerCase() === 'falling' || activityType === '跌倒'
      if (falling && !this.isFalling) {
        this.isFalling = true
        uni.showModal({
          title: '⚠️ 跌倒警报',
          content: '检测到老人可能发生跌倒，请立即查看！',
          confirmText: '立即查看',
          cancelText: '已知晓',
          showCancel: true,
          success: (res) => {
            if (res.confirm) {
              this.goToPage('/pages/behavior/behavior')
            }
          }
        })
      } else if (!falling) {
        this.isFalling = false
      }
    },

    // ---- 演示模式：镜像 Unity JS 动画节奏（站立8s → 步行15s 无限循环）----
    startDemoMode() {
      if (this._inDemoMode) return
      this._inDemoMode = true
      const cycle = () => {
        if (!this._inDemoMode) return
        this.applyBehavior('Standing')
        this._demoTimeout = setTimeout(() => {
          if (!this._inDemoMode) return
          this.applyBehavior('Walking')
          this._demoTimeout = setTimeout(cycle, 15000) // 步行持续 15s
        }, 8000) // 站立持续 8s
      }
      cycle()
    },

    stopDemoMode() {
      this._inDemoMode = false
      if (this._demoTimeout) { clearTimeout(this._demoTimeout); this._demoTimeout = null }
    },

    // 每2秒从全局状态同步行为（演示模式由 unity.vue 写入全局）
    startGlobalDataSync() {
      if (this._globalSyncInterval) clearInterval(this._globalSyncInterval)
      this._globalSyncInterval = setInterval(() => { this.syncBehaviorFromGlobal() }, 2000)
    },

    syncBehaviorFromGlobal() {
      try {
        const app = getApp()
        if (!app || !app.globalData) return

        // 始终刷新活动时长（即使行为没变，时长也在增加）
        if (app.getBehaviorDuration) {
          this.behavior.duration = app.getBehaviorDuration()
        }

        const gd = app.globalData.currentBehavior
        if (!gd) return
        const gdKey = gd.toLowerCase()
        const curKey = (this.currentBehavior || '').toLowerCase()
        if (gdKey === curKey) return  // 行为没变，时长已在上面更新

        // 行为变化：更新展示
        this.currentBehavior = gd
        const labelMap = { walking: '步行', standing: '站立', falling: '跌倒' }
        const label = labelMap[gdKey]
        if (label) this.behavior.activity = label

        const falling = gdKey === 'falling' || gd === '跌倒'
        if (falling && !this.isFalling) {
          this.isFalling = true
          uni.showModal({
            title: '⚠️ 跌倒警报',
            content: '检测到老人可能发生跌倒，请立即查看！',
            confirmText: '立即查看',
            cancelText: '已知晓',
            showCancel: true,
            success: (res) => { if (res.confirm) this.goToPage('/pages/behavior/behavior') }
          })
        } else if (!falling) {
          this.isFalling = false
        }
      } catch (e) {}
    },

    getHeartRateStatus(v) {
      if (v < 55) return { cls: 'warn', text: '偏低' }
      if (v > 100) return { cls: 'warn', text: '偏高' }
      return { cls: 'ok', text: '正常' }
    },
    getBPStatus(bp) {
      if (bp.high > 140 || bp.low > 90) return { cls: 'warn', text: '偏高' }
      if (bp.high < 90 || bp.low < 60) return { cls: 'warn', text: '偏低' }
      return { cls: 'ok', text: '正常' }
    },
    getOxygenStatus(v) {
      if (v < 95) return { cls: 'danger', text: '偏低' }
      return { cls: 'ok', text: '正常' }
    },
    getTempStatus(v) {
      if (v > 37.5) return { cls: 'warn', text: '发热' }
      if (v < 36) return { cls: 'warn', text: '偏低' }
      return { cls: 'ok', text: '正常' }
    },

    goToDetail(type) {
      uni.navigateTo({ url: `/pages/detail/detail?type=${type}` })
    },
    goToPage(url) {
      const tabBarPages = [
        '/pages/index/index',
        '/pages/behavior/behavior',
        '/pages/ai/ai',
        '/pages/risk/risk',
        '/pages/settings/settings'
      ]
      if (tabBarPages.includes(url)) {
        uni.switchTab({ url })
      } else {
        uni.navigateTo({ url })
      }
    }
  }
}
</script>

<style scoped>
/* ===== 基础 ===== */
.container {
  min-height: 100vh;
  background: #F1F5F1;
  padding: 16rpx 0 20rpx;
  box-sizing: border-box;
}

/* ===== 顶部轮播图 ===== */
.banner-swiper {
  width: 100%;
  height: 360rpx;
  padding: 0 20rpx;
  box-sizing: border-box;
  margin-bottom: 20rpx;
}

.banner-img {
  width: 100%;
  height: 100%;
  border-radius: 24rpx;
}

/* ===== 通用卡片 ===== */
.card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 28rpx;
  margin: 0 20rpx 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22rpx;
}

.card-title-row {
  display: flex;
  align-items: center;
}

.icon-wrap {
  width: 60rpx;
  height: 60rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.green-bg  { background: #E8F5E9; }
.blue-bg   { background: #E3F2FD; }
.teal-bg   { background: #E0F2F1; }
.orange-bg { background: #FFF3E0; }

.card-icon {
  font-size: 34rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #2E3A2E;
}

.more-btn {
  font-size: 26rpx;
  color: #4CAF50;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  background: #F1F8F1;
}

/* ===== Unity ===== */
.unity-card {
  margin: 0 20rpx 20rpx;
}

.gender-tag {
  font-size: 22rpx;
  color: #4CAF50;
  background: #E8F5E9;
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
}

.unity-frame {
  width: 100%;
  height: 820rpx;
  border-radius: 20rpx;
  overflow: hidden;
  position: relative;
  background: #1a3a2a;
}

.unity-webview {
  width: 100%;
  height: 100%;
  display: block;
  border: none;
}

.unity-loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #E8F5E9, #C8E6C9);
}

.loading-spin {
  width: 60rpx;
  height: 60rpx;
  border: 6rpx solid rgba(76,175,80,0.2);
  border-top-color: #4CAF50;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16rpx;
}

@keyframes spin { to { transform: rotate(360deg); } }

.loading-txt {
  font-size: 26rpx;
  color: #388E3C;
}

.hr-overlay {
  position: absolute;
  top: 18rpx;
  left: 18rpx;
  display: flex;
  align-items: center;
  background: rgba(0,0,0,0.45);
  padding: 10rpx 18rpx;
  border-radius: 30rpx;
  backdrop-filter: blur(4px);
}

.hr-overlay.beat {
  animation: beat 0.3s ease-in-out;
}

@keyframes beat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.12); }
}

.hr-icon { font-size: 28rpx; margin-right: 8rpx; }
.hr-val { font-size: 28rpx; font-weight: bold; color: #fff; margin-right: 4rpx; }
.hr-unit { font-size: 20rpx; color: rgba(255,255,255,0.8); }

.conn-badge {
  position: absolute;
  bottom: 16rpx;
  left: 16rpx;
  display: flex;
  align-items: center;
  padding: 8rpx 14rpx;
  border-radius: 20rpx;
  background: rgba(0,0,0,0.4);
}

.conn-dot { font-size: 18rpx; margin-right: 6rpx; }
.conn-ok .conn-dot { color: #69F0AE; }
.conn-off .conn-dot { color: #FF7043; }
.conn-label { font-size: 20rpx; color: #fff; }

/* 行为状态角标 */
.beh-overlay {
  position: absolute;
  top: 18rpx;
  right: 18rpx;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.45);
  padding: 10rpx 18rpx;
  border-radius: 30rpx;
  backdrop-filter: blur(4px);
  transition: background 0.3s;
}

.beh-overlay.beh-falling {
  background: rgba(210, 20, 20, 0.85);
}

.beh-overlay-text {
  font-size: 24rpx;
  color: #ffffff;
}

/* ===== 区块标题 ===== */
.section-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 26rpx 14rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #2E3A2E;
}

.section-time {
  font-size: 22rpx;
  color: #A5B4A5;
}

/* ===== 健康数据 2x2 网格 ===== */
.health-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  padding: 0 20rpx 20rpx;
}

.health-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx 22rpx;
  box-shadow: 0 4rpx 18rpx rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
}

.hc-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.hc-icon { font-size: 36rpx; }

.hc-label {
  font-size: 24rpx;
  color: #9E9E9E;
  margin-bottom: 8rpx;
}

.hc-value-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 12rpx;
}

.hc-value {
  font-size: 44rpx;
  font-weight: bold;
  color: #2E3A2E;
}

.hc-unit {
  font-size: 22rpx;
  color: #9E9E9E;
  margin-left: 4rpx;
}

.hc-status {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  align-self: flex-start;
}

.hc-status.ok {
  background: #E8F5E9;
  color: #388E3C;
}

.hc-status.warn {
  background: #FFF3E0;
  color: #E65100;
}

.hc-status.danger {
  background: #FFEBEE;
  color: #C62828;
}

/* ===== 数据更新动效 ===== */
.health-card {
  transition: box-shadow 0.4s ease;
}
.health-card.flash {
  box-shadow: 0 4rpx 24rpx rgba(76,175,80,0.28), 0 0 0 2rpx rgba(76,175,80,0.18);
}
@keyframes valueSlideIn {
  from { opacity: 0.5; transform: translateY(-6rpx); }
  to   { opacity: 1;   transform: translateY(0); }
}
.health-card.flash .hc-value {
  animation: valueSlideIn 0.4s ease-out;
}
.section-time {
  transition: color 0.3s;
}

/* ===== 行为 ===== */
.behavior-row {
  display: flex;
  align-items: center;
}

.beh-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.beh-icon {
  font-size: 36rpx;
  margin-bottom: 8rpx;
}

.beh-val {
  font-size: 28rpx;
  font-weight: bold;
  color: #2E3A2E;
  margin-bottom: 6rpx;
}

.beh-key {
  font-size: 22rpx;
  color: #9E9E9E;
}

.beh-divider {
  width: 1rpx;
  height: 80rpx;
  background: #EEEEEE;
}

/* ===== 环境 ===== */
.env-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10rpx;
}

.env-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 8rpx;
  background: #F8FBF8;
  border-radius: 18rpx;
}

.env-icon-wrap {
  width: 60rpx;
  height: 60rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10rpx;
}

.env-icon { font-size: 30rpx; }

.env-val {
  font-size: 26rpx;
  font-weight: bold;
  color: #2E3A2E;
  margin-bottom: 4rpx;
}

.env-key {
  font-size: 20rpx;
  color: #9E9E9E;
}

/* ===== 风险 ===== */
.risk-row {
  display: flex;
  align-items: center;
}

.risk-score-wrap {
  width: 130rpx;
  height: 130rpx;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 28rpx;
  flex-shrink: 0;
}

.risk-score-wrap.low    { background: linear-gradient(135deg, #66BB6A, #43A047); box-shadow: 0 8rpx 20rpx rgba(76,175,80,0.3); }
.risk-score-wrap.medium { background: linear-gradient(135deg, #FFA726, #FB8C00); box-shadow: 0 8rpx 20rpx rgba(255,152,0,0.3); }
.risk-score-wrap.high   { background: linear-gradient(135deg, #EF5350, #E53935); box-shadow: 0 8rpx 20rpx rgba(244,67,54,0.3); }

.risk-score { font-size: 52rpx; font-weight: bold; color: #fff; line-height: 1; }
.risk-score-max { font-size: 22rpx; color: rgba(255,255,255,0.8); }

.risk-info { flex: 1; }

.risk-badge {
  display: inline-block;
  font-size: 24rpx;
  font-weight: bold;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  margin-bottom: 14rpx;
}

.risk-badge.low    { background: #E8F5E9; color: #2E7D32; }
.risk-badge.medium { background: #FFF3E0; color: #E65100; }
.risk-badge.high   { background: #FFEBEE; color: #B71C1C; }

.risk-advice {
  font-size: 24rpx;
  color: #757575;
  line-height: 1.6;
  display: block;
}

.bottom-space { height: 0; }
</style>
