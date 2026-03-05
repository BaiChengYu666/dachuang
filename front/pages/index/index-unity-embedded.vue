<template>
  <view class="container">
    <!-- 顶部状态栏 -->
    <view class="header">
      <view class="user-info">
        <image class="avatar" src="/static/logo.png" mode="aspectFill"></image>
        <view class="user-details">
          <text class="user-name">张奶奶</text>
          <text class="user-age">75岁</text>
        </view>
      </view>
      <view class="status-badge" :class="healthStatus">
        <text class="status-text">{{ statusText }}</text>
      </view>
    </view>

    <!-- ⭐ Unity数字孪生3D模型展示区 - 直接嵌入 -->
    <view class="unity-container">
      <view class="unity-placeholder">
        <text class="unity-title">实时行为姿态监测</text>
        <view class="unity-frame">
          <!-- ⭐ 方案1：小程序用web-view -->
          <!-- #ifdef MP-WEIXIN -->
          <web-view 
            :src="unityUrl"
            @message="handleUnityMessage"
            @load="handleUnityLoad"
            @error="handleUnityError"
            class="unity-webview">
          </web-view>
          <!-- #endif -->
          
          <!-- ⭐ 方案2：H5用iframe -->
          <!-- #ifdef H5 -->
          <iframe 
            :src="unityUrl"
            frameborder="0"
            class="unity-iframe"
            @load="handleUnityLoad">
          </iframe>
          <!-- #endif -->
          
          <!-- 加载状态 -->
          <view v-if="unityLoading" class="unity-loading">
            <view class="loading-spinner"></view>
            <text class="loading-text">3D模型加载中...</text>
          </view>
          
          <!-- 行为状态标签 -->
          <view class="behavior-tag" :class="'behavior-' + currentBehavior.toLowerCase()">
            <text class="behavior-icon">{{ behaviorIcon }}</text>
            <text class="behavior-text">{{ behaviorText }}</text>
          </view>
          
          <!-- 后端连接状态 -->
          <view class="backend-status" :class="backendConnected ? 'connected' : 'disconnected'">
            <text class="status-dot">●</text>
            <text class="status-label">{{ backendConnected ? '实时监测中' : '使用模拟数据' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 实时健康数据卡片 -->
    <view class="health-cards">
      <view class="card" @click="goToDetail('heartRate')">
        <view class="card-info">
          <text class="card-label">心率</text>
          <text class="card-value">{{ Math.round(healthData.heartRate) }} <text class="unit">bpm</text></text>
          <text class="card-status normal">正常</text>
        </view>
      </view>

      <view class="card" @click="goToDetail('bloodPressure')">
        <view class="card-info">
          <text class="card-label">血压</text>
          <text class="card-value">{{ Math.round(healthData.bloodPressure.high) }}/{{ Math.round(healthData.bloodPressure.low) }}</text>
          <text class="card-status normal">正常</text>
        </view>
      </view>

      <view class="card" @click="goToDetail('bloodOxygen')">
        <view class="card-info">
          <text class="card-label">血氧</text>
          <text class="card-value">{{ Number(healthData.bloodOxygen).toFixed(1) }} <text class="unit">%</text></text>
          <text class="card-status normal">正常</text>
        </view>
      </view>

      <view class="card" @click="goToDetail('temperature')">
        <view class="card-info">
          <text class="card-label">体温</text>
          <text class="card-value">{{ Number(healthData.temperature).toFixed(1) }} <text class="unit">°C</text></text>
          <text class="card-status normal">正常</text>
        </view>
      </view>
    </view>

    <!-- 其他内容保持不变... -->
    <view class="bottom-space"></view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      healthStatus: 'normal',
      statusText: '健康状态良好',
      
      elderlyId: 1,
      backendConnected: false,
      
      // ⭐ Unity相关
      unityUrl: '/static/unity/index.html',  // Unity本地路径
      unityLoading: true,
      currentBehavior: 'STANDING',  // STANDING, WALKING, FALLING
      
      healthData: {
        heartRate: 72,
        bloodPressure: { high: 125, low: 80 },
        bloodOxygen: 98,
        temperature: 36.5
      }
    }
  },
  
  computed: {
    behaviorIcon() {
      const icons = {
        'STANDING': '🧍',
        'WALKING': '🚶',
        'FALLING': '⚠️'
      }
      return icons[this.currentBehavior] || '🧍'
    },
    
    behaviorText() {
      const texts = {
        'STANDING': '站立',
        'WALKING': '行走',
        'FALLING': '摔倒警报'
      }
      return texts[this.currentBehavior] || '监测中'
    }
  },
  
  onLoad() {
    console.log('📱 页面加载中...')
    
    // 加载后端数据
    this.loadBackendData()
    
    // 启动定时更新
    this.startDataUpdate()
    
    // ⭐ 向Unity传递行为状态的全局函数
    this.setupUnityBridge()
  },
  
  methods: {
    // ⭐ 设置Unity通信桥接
    setupUnityBridge() {
      // 小程序环境
      // #ifdef MP-WEIXIN
      // 小程序通过web-view的message事件通信
      // #endif
      
      // H5环境
      // #ifdef H5
      // 设置全局函数供Unity调用
      window.getCurrentBehavior = () => {
        return this.currentBehavior
      }
      
      // Unity警报回调
      window.onUnityAlert = (message) => {
        console.log('Unity警报:', message)
        uni.showModal({
          title: '⚠️ 警报',
          content: message,
          showCancel: false
        })
      }
      // #endif
    },
    
    // ⭐ Unity加载完成
    handleUnityLoad() {
      console.log('✅ Unity加载完成')
      this.unityLoading = false
      
      // 初始化Unity行为状态
      setTimeout(() => {
        this.updateUnityBehavior(this.currentBehavior)
      }, 1000)
    },
    
    // ⭐ Unity加载错误
    handleUnityError(e) {
      console.error('❌ Unity加载失败:', e)
      this.unityLoading = false
      
      uni.showToast({
        title: 'Unity加载失败',
        icon: 'none'
      })
    },
    
    // ⭐ Unity消息处理
    handleUnityMessage(e) {
      console.log('💬 Unity消息:', e.detail.data)
    },
    
    // ⭐ 更新Unity中的行为状态
    updateUnityBehavior(behavior) {
      console.log('🔄 更新Unity行为:', behavior)
      
      // H5环境 - 通过iframe通信
      // #ifdef H5
      const iframe = document.querySelector('.unity-iframe')
      if (iframe && iframe.contentWindow) {
        iframe.contentWindow.postMessage({
          type: 'updateBehavior',
          behavior: behavior
        }, '*')
      }
      // #endif
      
      // 小程序环境 - 通过URL参数传递（重新加载）
      // #ifdef MP-WEIXIN
      // 注意：web-view通信受限，可能需要通过URL参数
      this.unityUrl = `/static/unity/index.html?behavior=${behavior}&t=${Date.now()}`
      // #endif
    },
    
    // 从后端获取行为状态
    async loadBehaviorStatus() {
      try {
        const res = await new Promise((resolve, reject) => {
          uni.request({
            url: `http://localhost:8080/api/behavior/current/${this.elderlyId}`,
            method: 'GET',
            success: resolve,
            fail: reject
          })
        })
        
        if (res.statusCode === 200 && res.data.code === 200) {
          const behavior = res.data.data.behaviorType
          
          if (this.currentBehavior !== behavior) {
            console.log('📍 行为状态变化:', this.currentBehavior, '→', behavior)
            this.currentBehavior = behavior
            
            // 更新Unity显示
            this.updateUnityBehavior(behavior)
            
            // 摔倒警报
            if (behavior === 'FALLING') {
              this.showFallAlert()
            }
          }
        }
      } catch (error) {
        console.error('❌ 获取行为状态失败:', error)
      }
    },
    
    // 摔倒警报
    showFallAlert() {
      uni.showModal({
        title: '⚠️ 摔倒警报',
        content: '检测到可能摔倒，请立即查看！',
        showCancel: false,
        confirmText: '查看详情'
      })
      
      // 震动
      uni.vibrateShort()
    },
    
    // 启动定时更新
    startDataUpdate() {
      // 每2秒获取一次行为状态
      setInterval(() => {
        this.loadBehaviorStatus()
      }, 2000)
      
      // 每5秒获取一次健康数据
      setInterval(() => {
        this.loadBackendData()
      }, 5000)
    },
    
    async loadBackendData() {
      try {
        console.log('🔄 正在从后端加载数据... elderlyId:', this.elderlyId)
        
        const url = `http://localhost:8080/api/data/latest/${this.elderlyId}`
        console.log('🌐 请求URL:', url)
        
        const res = await new Promise((resolve, reject) => {
          uni.request({
            url: url,
            method: 'GET',
            success: resolve,
            fail: reject
          })
        })
        
        console.log('📦 后端返回:', res)
        
        if (res.statusCode === 200 && res.data.code === 200) {
          const data = res.data.data
          
          if (data && data.physiological) {
            this.healthData.heartRate = data.physiological.heartRate || 72
            this.healthData.bloodPressure.high = data.physiological.bloodPressureHigh || 125
            this.healthData.bloodPressure.low = data.physiological.bloodPressureLow || 80
            this.healthData.bloodOxygen = data.physiological.bloodOxygen || 98
            this.healthData.temperature = data.physiological.bodyTemperature || 36.5
            
            this.backendConnected = true
            console.log('✅ 后端数据加载成功！')
            console.log('💓 心率:', this.healthData.heartRate)
          } else {
            console.log('⚠️ 后端暂无数据')
            this.backendConnected = false
          }
        } else {
          console.log('⚠️ 后端返回异常')
          this.backendConnected = false
        }
      } catch (error) {
        console.error('❌ 加载后端数据失败:', error)
        console.log('⚠️ 使用模拟数据')
        this.backendConnected = false
      }
    },
    
    goToDetail(type) {
      uni.navigateTo({
        url: `/pages/detail/detail?type=${type}`
      })
    }
  }
}
</script>

<style scoped>
/* 基础容器：统一暖白色背景保持不变 */
.container {
  min-height: 100vh;
  background: #faf7f2; /* 暖白背景，和其他页面统一 */
  padding: 30rpx; /* 增大内边距，更宽松 */
  box-sizing: border-box;
}

/* 顶部栏：调整配色为绿色系 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background: #ffffff;
  border-radius: 30rpx; /* 统一30rpx圆角 */
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.03); /* 更柔和的阴影 */
  margin-bottom: 30rpx;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background: #f0eeeb; /* 暖白色占位 */
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #4a4a4a; /* 深灰色，保持可读性 */
  margin-bottom: 10rpx;
}

.user-age {
  font-size: 28rpx;
  color: #999;
}

/* 健康状态标签：替换暖黄系为绿色系 */
.status-badge {
  padding: 15rpx 30rpx;
  border-radius: 30rpx;
}

.status-badge.normal {
  background: #f0fcf0; /* 绿色系正常背景 */
}

.status-text {
  font-size: 28rpx;
  color: #73c973; /* 主浅绿色文字 */
  font-weight: 500;
}

/* Unity容器：调整配色为绿色系 */
.unity-container {
  margin-bottom: 30rpx;
}

.unity-placeholder {
  background: #ffffff;
  border-radius: 30rpx; /* 统一30rpx圆角 */
  padding: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.03);
}

.unity-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #4a4a4a; /* 深灰色标题 */
  margin-bottom: 20rpx;
  display: block;
}

.unity-frame {
  width: 100%;
  height: 500rpx;
  border-radius: 30rpx; /* 统一圆角 */
  position: relative;
  overflow: hidden;
  background: #73c973; /* 主浅绿色，替换暖黄色 */
}

/* Unity web-view和iframe */
.unity-webview,
.unity-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* 加载状态：替换暖黄为绿色系 */
.unity-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #73c973; /* 主浅绿色 */
  z-index: 10;
}

.loading-spinner {
  width: 80rpx;
  height: 80rpx;
  border: 6rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 20rpx;
  color: #ffffff;
  font-size: 28rpx;
}

/* 行为状态标签：替换暖黄系为绿色系 */
.behavior-tag {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  padding: 15rpx 30rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  backdrop-filter: blur(10rpx);
  z-index: 20;
}

/* 站立状态：深绿色 */
.behavior-standing {
  background: rgba(92, 184, 92, 0.9); /* 深绿色 */
}

/* 行走状态：主浅绿色 */
.behavior-walking {
  background: rgba(115, 201, 115, 0.9); /* 主浅绿色 */
}

/* 摔倒状态：警示红色（保持警示性） */
.behavior-falling {
  background: rgba(220, 53, 69, 0.9); /* 警示红 */
  animation: shake 0.5s infinite;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5rpx); }
  75% { transform: translateX(5rpx); }
}

.behavior-icon {
  font-size: 32rpx;
}

.behavior-text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: bold;
}

/* 后端状态：颜色调整为绿色系 */
.backend-status {
  position: absolute;
  bottom: 20rpx;
  left: 20rpx;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.5);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  z-index: 20;
}

.status-dot {
  font-size: 20rpx;
  margin-right: 8rpx;
}

.backend-status.connected .status-dot {
  color: #5cb85c; /* 深绿色 */
}

.backend-status.disconnected .status-dot {
  color: #73c973; /* 主浅绿色 */
}

.status-label {
  font-size: 20rpx;
  color: #ffffff;
}

/* 健康数据卡片：替换暖黄系为绿色系 */
.health-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.card {
  background: #ffffff;
  border-radius: 30rpx; /* 统一30rpx圆角 */
  padding: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: center;
}

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-label {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 10rpx;
}

.card-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #4a4a4a; /* 深灰色数值 */
  margin-bottom: 10rpx;
}

.unit {
  font-size: 24rpx;
  font-weight: normal;
  color: #999;
}

/* 卡片状态标签：替换暖黄系为绿色系 */
.card-status {
  font-size: 24rpx;
  padding: 5rpx 15rpx;
  border-radius: 20rpx; /* 更大圆角 */
  align-self: flex-start;
}

.card-status.normal {
  background: #f0fcf0; /* 绿色系正常背景 */
  color: #73c973; /* 主浅绿色 */
}

.bottom-space {
  height: 120rpx;
}
</style>