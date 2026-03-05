<template>
  <view class="unity-page">
    <!-- 加载状态 -->
    <view v-if="isLoading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">Unity加载中...</text>
      <text class="loading-progress">{{ loadingProgress }}%</text>
      <text class="loading-tip">首次加载需要10-30秒，请耐心等待</text>
    </view>
    
    <!-- Unity WebGL -->
    <web-view
      v-else
      :src="unityUrl"
      @message="handleMessage"
      @load="handleLoad"
      @error="handleError">
    </web-view>

    <!-- 行为状态角标（cover-view 可覆盖在 web-view 上） -->
    <cover-view v-if="!isLoading && !hasError" class="beh-status" :class="isFalling ? 'beh-status-fall' : ''">
      <cover-view class="beh-status-text">{{ behaviorIcon }}  {{ behaviorLabel }}</cover-view>
    </cover-view>

    <!-- 错误提示 -->
    <view v-if="hasError" class="error-container">
      <view class="error-icon">⚠️</view>
      <text class="error-title">加载失败</text>
      <text class="error-message">{{ errorMessage }}</text>
      <button class="retry-btn" @click="retry">重试</button>
      <button class="back-btn" @click="goBack">返回首页</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      unityUrl: 'https://unity-web-gl-iota.vercel.app/index.html?gender=female', // 默认女性
      
      isLoading: true,
      loadingProgress: 0,
      hasError: false,
      errorMessage: '',
      
      // 健康数据
      healthData: {
        heartRate: 72,
        bloodPressure: { high: 125, low: 80 },
        bloodOxygen: 98,
        temperature: 36.5
      },

      // 行为状态
      currentBehavior: 'Standing',
      isFalling: false,
      elderlyId: 1
    }
  },

  computed: {
    behaviorLabel() {
      switch ((this.currentBehavior || '').toLowerCase()) {
        case 'walking': case '步行': return '步行中'
        case 'falling': case '跌倒': return '跌倒警报'
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
    console.log('🚀 Unity页面加载')

    // 读取登录用户性别
    try {
      const userInfo = uni.getStorageSync('userInfo')
      let gender = 'female' // 默认女性

      if (userInfo && userInfo.gender) {
        // 处理"未知"或空值
        if (userInfo.gender === 'male' || userInfo.gender === '男') {
          gender = 'male'
        } else {
          gender = 'female'
        }
      }

      // 读取 elderlyId
      if (userInfo && userInfo.elderlyId) {
        this.elderlyId = userInfo.elderlyId
      }

      // 拼接性别和 elderlyId 参数到 Unity URL
      this.unityUrl = `https://unity-web-gl-iota.vercel.app/index.html?gender=${gender}&elderlyId=${this.elderlyId}`
      console.log('👤 用户性别:', gender, '老人ID:', this.elderlyId)
      console.log('🎮 Unity URL:', this.unityUrl)

    } catch (e) {
      console.error('读取用户信息失败:', e)
    }

    // 模拟加载进度
    this.simulateLoading()

    // 启动数据同步
    this.startDataSync()

    // 启动行为状态轮询（延迟3s等场景稳定）
    setTimeout(() => { this.startBehaviorPoll() }, 3000)
  },

  onUnload() {
    if (this._progressInterval) {
      clearInterval(this._progressInterval)
      this._progressInterval = null
    }
    if (this._loadTimeout) {
      clearTimeout(this._loadTimeout)
      this._loadTimeout = null
    }
    if (this._syncInterval) {
      clearInterval(this._syncInterval)
      this._syncInterval = null
    }
    if (this._behaviorInterval) {
      clearInterval(this._behaviorInterval)
      this._behaviorInterval = null
    }
  },

  methods: {
    // 模拟加载进度
    simulateLoading() {
      this._progressInterval = setInterval(() => {
        if (this.loadingProgress < 85) {
          this.loadingProgress += Math.random() * 5
        } else {
          clearInterval(this._progressInterval)
          this._progressInterval = null
        }
      }, 800)

      // 45秒后自动隐藏加载界面（Unity WebGL首次加载需要时间）
      this._loadTimeout = setTimeout(() => {
        if (this.isLoading) {
          this.isLoading = false
          this.loadingProgress = 100
        }
      }, 45000)
    },
    
    // Unity加载完成
    handleLoad(e) {
      console.log('✅ Unity加载完成', e)
      this.isLoading = false
      this.loadingProgress = 100
      
      uni.showToast({
        title: 'Unity加载成功',
        icon: 'success',
        duration: 2000
      })
    },
    
    // Unity加载错误
    handleError(e) {
      console.error('❌ Unity加载失败', e)
      this.hasError = true
      this.isLoading = false
      
      this.errorMessage = 
        'Unity加载失败\n\n' +
        '可能原因：\n' +
        '1. 网络连接问题\n' +
        '2. Unity文件未正确部署\n' +
        '3. 域名未配置白名单\n\n' +
        '请检查后重试'
    },
    
    // 接收Unity消息
    handleMessage(e) {
      console.log('💬 Unity消息', e.detail.data)

      const data = e.detail.data[0]
      if (data && data.type === 'unity_loaded') {
        this.isLoading = false
        console.log('✅ Unity初始化完成')
      }
      // Unity JS桥回调：跌倒警报
      if (data && data.type === 'fall') {
        this.applyBehavior('Falling')
      }
    },

    // ---- 行为轮询 ----
    startBehaviorPoll() {
      if (this._behaviorInterval) clearInterval(this._behaviorInterval)
      this.fetchBehavior()
      this._behaviorInterval = setInterval(() => {
        this.fetchBehavior()
      }, 5000)
    },

    fetchBehavior() {
      uni.request({
        url: `http://localhost:8080/api/data/behavior/latest/${this.elderlyId}`,
        method: 'GET',
        success: (res) => {
          if (res.statusCode === 200 && res.data && res.data.code === 200 && res.data.data) {
            // 后端有数据：停止演示模式
            this._demoFailCount = 0
            if (this._inDemoMode) this.stopDemoMode()
            this.applyBehavior(res.data.data.activityType)
          } else {
            this._onBehaviorFail()
          }
        },
        fail: () => { this._onBehaviorFail() }
      })
    },

    _onBehaviorFail() {
      this._demoFailCount = (this._demoFailCount || 0) + 1
      if (this._demoFailCount >= 3 && !this._inDemoMode) this.startDemoMode()
    },

    applyBehavior(activityType) {
      if (!activityType) return
      this.currentBehavior = activityType
      const falling = activityType.toLowerCase() === 'falling' || activityType === '跌倒'

      // 推送到全局状态，同步所有页面
      try {
        const app = getApp()
        if (app && app.pushBehavior) app.pushBehavior(activityType)
      } catch (e) {}

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
              uni.navigateTo({ url: '/pages/behavior/behavior' })
            }
          }
        })
      } else if (!falling) {
        this.isFalling = false
      }
    },

    // 前端演示模式：后端连续失败时自动循环 站立↔步行
    startDemoMode() {
      if (this._inDemoMode) return
      this._inDemoMode = true
      console.log('▶ 前端演示模式启动（无后端，自动循环）')
      const cycle = () => {
        if (!this._inDemoMode) return
        this.applyBehavior('Standing')
        this._demoTimeout = setTimeout(() => {
          if (!this._inDemoMode) return
          this.applyBehavior('Walking')
          this._demoTimeout = setTimeout(cycle, 5000)
        }, 4000)
      }
      cycle()
    },

    stopDemoMode() {
      this._inDemoMode = false
      if (this._demoTimeout) { clearTimeout(this._demoTimeout); this._demoTimeout = null }
    },
    
    // 数据同步
    startDataSync() {
      this._syncInterval = setInterval(() => {
        this.updateHealthData()
      }, 1000)
    },
    
    updateHealthData() {
      const time = Date.now() / 1000
      this.healthData.heartRate = 72 + Math.sin(time * 0.5) * 8
      this.healthData.bloodPressure.high = 120 + Math.sin(time * 0.3) * 8
      this.healthData.temperature = 36.5 + Math.sin(time * 0.1) * 0.3
    },
    
    // 重试
    retry() {
      this.hasError = false
      this.isLoading = true
      this.loadingProgress = 0
      
      // 重新加载
      setTimeout(() => {
        this.simulateLoading()
      }, 500)
    },
    
    // 返回
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.unity-page {
  width: 100vw;
  height: 100vh;
  background: #1a1a2e;
  position: relative;
}

/* 加载状态 */
.loading-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 999;
}

.loading-spinner {
  width: 120rpx;
  height: 120rpx;
  border: 8rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 40rpx;
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
}

.loading-progress {
  margin-top: 20rpx;
  color: #ffffff;
  font-size: 48rpx;
  font-weight: bold;
}

.loading-tip {
  margin-top: 30rpx;
  color: rgba(255, 255, 255, 0.8);
  font-size: 28rpx;
  text-align: center;
  padding: 0 60rpx;
}

/* 错误状态 */
.error-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx;
  background: #1a1a2e;
  z-index: 999;
}

.error-icon {
  font-size: 120rpx;
  margin-bottom: 40rpx;
}

.error-title {
  font-size: 40rpx;
  color: #ff5252;
  font-weight: bold;
  margin-bottom: 30rpx;
}

.error-message {
  font-size: 28rpx;
  color: #ffffff;
  line-height: 1.8;
  text-align: center;
  white-space: pre-line;
  margin-bottom: 60rpx;
}

.retry-btn,
.back-btn {
  width: 400rpx;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 32rpx;
  margin-bottom: 30rpx;
}

.retry-btn {
  background: linear-gradient(90deg, #667eea, #764ba2);
  color: #ffffff;
}

.back-btn {
  background: transparent;
  border: 2rpx solid #667eea;
  color: #667eea;
}

.retry-btn::after,
.back-btn::after {
  border: none;
}

/* 行为状态角标 */
.beh-status {
  position: fixed;
  bottom: 50rpx;
  right: 24rpx;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 32rpx;
  padding: 14rpx 28rpx;
  z-index: 9999;
}

.beh-status-fall {
  background: rgba(200, 0, 0, 0.82);
}

.beh-status-text {
  color: #ffffff;
  font-size: 28rpx;
}
</style>
