<template>
  <view class="container">

    <!-- 顶部当前活动 -->
    <view class="activity-header">
      <view class="activity-row">
        <view class="activity-icon-wrap">
          <text class="activity-icon">{{ currentActivity.icon }}</text>
        </view>
        <view class="activity-info">
          <text class="activity-name">{{ currentActivity.name }}</text>
          <text class="activity-duration">⏱️ 已持续 {{ currentActivity.duration }}</text>
        </view>
        <view class="activity-status-dot"></view>
      </view>
    </view>

    <!-- 今日统计 -->
    <view class="section-label">
      <text class="section-title">今日运动统计</text>
    </view>
    <view class="stats-grid">
      <view class="stat-card blue-grad">
        <text class="stat-big">{{ dailyStats.steps }}</text>
        <text class="stat-unit">步</text>
        <text class="stat-label">今日步数</text>
        <text class="stat-icon-bg">👟</text>
      </view>
      <view class="stat-card green-grad">
        <text class="stat-big">{{ dailyStats.activeMinutes }}</text>
        <text class="stat-unit">分钟</text>
        <text class="stat-label">活动时长</text>
        <text class="stat-icon-bg">🏃</text>
      </view>
      <view class="stat-card orange-grad">
        <text class="stat-big">{{ dailyStats.calories }}</text>
        <text class="stat-unit">kcal</text>
        <text class="stat-label">消耗热量</text>
        <text class="stat-icon-bg">🔥</text>
      </view>
    </view>

    <!-- 室内定位 -->
    <view class="card">
      <view class="card-title-row">
        <view class="title-icon-wrap blue-bg">
          <text class="title-icon">📍</text>
        </view>
        <text class="card-title">室内定位监测</text>
        <view class="location-badge">
          <text class="location-dot">●</text>
          <text class="location-name">{{ currentLocation }}</text>
        </view>
      </view>
      <view class="map-placeholder">
        <text class="map-grid-icon">🗺️</text>
        <text class="map-text">当前位置：{{ currentLocation }}</text>
        <text class="map-hint">室内 UWB 精准定位中</text>
      </view>
    </view>

    <!-- 活动记录时间轴 -->
    <view class="card">
      <view class="card-title-row">
        <view class="title-icon-wrap green-bg">
          <text class="title-icon">📝</text>
        </view>
        <text class="card-title">今日活动记录</text>
      </view>
      <view class="timeline">
        <view class="tl-item" v-for="(item, index) in activityHistory" :key="index">
          <view class="tl-left">
            <view class="tl-dot" :style="{background: getTypeColor(item.type)}"></view>
            <view v-if="index < activityHistory.length - 1" class="tl-line"></view>
          </view>
          <view class="tl-content">
            <view class="tl-row">
              <text class="tl-time">{{ item.time }}</text>
              <text class="tl-activity">{{ item.activity }}</text>
            </view>
            <text class="tl-location">📍 {{ item.location }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 行为异常预警设置 -->
    <view class="card">
      <view class="card-title-row">
        <view class="title-icon-wrap red-bg">
          <text class="title-icon">🔔</text>
        </view>
        <text class="card-title">行为异常预警</text>
      </view>
      <view class="alert-list">
        <view class="alert-item" v-for="(item, key) in alertConfig" :key="key">
          <view class="alert-left">
            <text class="alert-icon">{{ item.icon }}</text>
            <view class="alert-info">
              <text class="alert-label">{{ item.label }}</text>
              <text class="alert-desc">{{ item.desc }}</text>
            </view>
          </view>
          <switch :checked="alerts[key]" @change="toggleAlert(key)" color="#4CAF50"/>
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
      currentActivity: {
        icon: '🧍',
        name: '当前状态：站立',
        duration: '8分钟'
      },
      currentLocation: '客厅',
      dailyStats: {
        steps: 3256,
        activeMinutes: 87,
        calories: 245
      },
      activityHistory: [
        { time: '14:52', activity: '站立',     location: '客厅', type: 'stand' },
        { time: '14:30', activity: '缓慢行走', location: '卧室→客厅', type: 'walk' },
        { time: '13:30', activity: '午睡',     location: '卧室', type: 'sleep' },
        { time: '12:00', activity: '用餐',     location: '餐厅', type: 'meal' },
        { time: '10:30', activity: '户外散步', location: '小区花园', type: 'outdoor' }
      ],
      alerts: {
        fall: true,
        inactive: true,
        leave: true,
        night: false
      },
      alertConfig: {
        fall:     { icon: '🛡️', label: '跌倒检测',       desc: '检测到跌倒立即报警' },
        inactive: { icon: '⏰', label: '长时间静止预警', desc: '超过2小时未活动提醒' },
        leave:    { icon: '🚪', label: '离开安全区域',   desc: '离开设定范围时提醒' },
        night:    { icon: '🌙', label: '夜间活动监测',   desc: '深夜异常起床预警' }
      }
    }
  },

  onShow() {
    this.syncTabBar()
  },

  methods: {
    syncTabBar() {
      const pages = getCurrentPages()
      const page = pages[pages.length - 1]
      if (page && page.getTabBar) {
        page.getTabBar()?.setSelected?.(1)
      }
    },

    getTypeColor(type) {
      const colors = {
        stand:   '#4CAF50',
        rest:    '#66BB6A',
        walk:    '#42A5F5',
        sleep:   '#7E57C2',
        meal:    '#FFA726',
        outdoor: '#26C6DA'
      }
      return colors[type] || '#9E9E9E'
    },

    toggleAlert(key) {
      this.alerts[key] = !this.alerts[key]
      uni.showToast({
        title: `${this.alertConfig[key].label}已${this.alerts[key] ? '开启' : '关闭'}`,
        icon: 'success'
      })
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #F1F5F1;
  padding: 16rpx 0 20rpx;
  box-sizing: border-box;
}

/* ===== 顶部活动 ===== */
.activity-header {
  background: #ffffff;
  padding: 24rpx 28rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 18rpx rgba(0,0,0,0.07);
}

.activity-row {
  display: flex;
  align-items: center;
}

.activity-icon-wrap {
  width: 90rpx;
  height: 90rpx;
  border-radius: 26rpx;
  background: #E3F2FD;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.activity-icon { font-size: 50rpx; }

.activity-info { flex: 1; }

.activity-name {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #2E3A2E;
  margin-bottom: 8rpx;
}

.activity-duration {
  display: block;
  font-size: 24rpx;
  color: #9E9E9E;
}

.activity-status-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #4CAF50;
  box-shadow: 0 0 0 6rpx rgba(76,175,80,0.2);
  animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 0 6rpx rgba(76,175,80,0.2); }
  50% { box-shadow: 0 0 0 10rpx rgba(76,175,80,0.08); }
}

/* ===== 区块标题 ===== */
.section-label {
  padding: 0 26rpx 14rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #2E3A2E;
}

/* ===== 统计卡片 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
  padding: 0 20rpx 20rpx;
}

.stat-card {
  border-radius: 24rpx;
  padding: 26rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  position: relative;
  overflow: hidden;
  box-shadow: 0 6rpx 20rpx rgba(0,0,0,0.1);
}

.blue-grad   { background: linear-gradient(145deg, #42A5F5, #1E88E5); }
.green-grad  { background: linear-gradient(145deg, #66BB6A, #43A047); }
.orange-grad { background: linear-gradient(145deg, #FFA726, #FB8C00); }

.stat-big {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  line-height: 1;
}

.stat-unit {
  font-size: 20rpx;
  color: rgba(255,255,255,0.85);
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 22rpx;
  color: rgba(255,255,255,0.8);
}

.stat-icon-bg {
  position: absolute;
  right: 10rpx;
  bottom: 10rpx;
  font-size: 60rpx;
  opacity: 0.2;
}

/* ===== 通用卡片 ===== */
.card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 28rpx;
  margin: 0 20rpx 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
}

.card-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.title-icon-wrap {
  width: 60rpx;
  height: 60rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.green-bg { background: #E8F5E9; }
.blue-bg  { background: #E3F2FD; }
.red-bg   { background: #FFEBEE; }
.title-icon { font-size: 32rpx; }

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #2E3A2E;
  flex: 1;
}

/* ===== 位置标签 ===== */
.location-badge {
  display: flex;
  align-items: center;
  background: #E8F5E9;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.location-dot {
  font-size: 16rpx;
  color: #4CAF50;
  margin-right: 6rpx;
}

.location-name {
  font-size: 22rpx;
  color: #388E3C;
}

/* ===== 地图占位 ===== */
.map-placeholder {
  height: 240rpx;
  background: linear-gradient(145deg, #E3F2FD, #BBDEFB);
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.map-grid-icon { font-size: 60rpx; margin-bottom: 12rpx; }
.map-text { font-size: 28rpx; color: #1565C0; font-weight: 500; margin-bottom: 8rpx; }
.map-hint { font-size: 22rpx; color: #64B5F6; }

/* ===== 时间轴 ===== */
.timeline { display: flex; flex-direction: column; }

.tl-item {
  display: flex;
  align-items: flex-start;
  min-height: 80rpx;
}

.tl-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20rpx;
  width: 24rpx;
}

.tl-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 8rpx;
}

.tl-line {
  width: 2rpx;
  flex: 1;
  background: #EEEEEE;
  margin-top: 6rpx;
  min-height: 40rpx;
}

.tl-content {
  flex: 1;
  padding-bottom: 24rpx;
}

.tl-row {
  display: flex;
  align-items: center;
  margin-bottom: 6rpx;
}

.tl-time {
  font-size: 24rpx;
  color: #9E9E9E;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.tl-activity {
  font-size: 28rpx;
  font-weight: 500;
  color: #2E3A2E;
}

.tl-location {
  font-size: 22rpx;
  color: #BDBDBD;
}

/* ===== 预警列表 ===== */
.alert-list { display: flex; flex-direction: column; }

.alert-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #F5F5F5;
}

.alert-item:last-child { border-bottom: none; }

.alert-left { display: flex; align-items: center; flex: 1; }

.alert-icon { font-size: 36rpx; margin-right: 18rpx; }

.alert-info { display: flex; flex-direction: column; }

.alert-label {
  font-size: 28rpx;
  color: #2E3A2E;
  font-weight: 500;
  margin-bottom: 4rpx;
}

.alert-desc {
  font-size: 22rpx;
  color: #BDBDBD;
}

.bottom-space { height: 0; }
</style>
