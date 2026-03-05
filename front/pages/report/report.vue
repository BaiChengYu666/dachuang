<template>
  <view class="container">

    <!-- 报告日期提示 -->
    <view class="report-date-bar" v-if="reportData.basicInfo">
      <text class="report-date-icon">📋</text>
      <text class="report-date-text">生成时间：{{ reportData.basicInfo.generatedAt }}</text>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="loading-box">
      <view class="loading-dots">
        <view class="dot"></view>
        <view class="dot"></view>
        <view class="dot"></view>
      </view>
      <text class="loading-text">正在生成报告...</text>
    </view>

    <view v-else>
      <!-- ① 个人信息 -->
      <view class="card">
        <view class="card-title-row" @click="toggle('basicInfo')">
          <view class="title-icon-wrap blue-bg"><text class="title-icon">👤</text></view>
          <text class="card-title">个人信息</text>
          <text class="chevron" :class="{ rotated: expanded.basicInfo }">›</text>
        </view>
        <view v-show="expanded.basicInfo" class="section-body">
          <view class="info-grid" v-if="reportData.basicInfo">
            <view class="info-item">
              <text class="info-lbl">姓名</text>
              <text class="info-val">{{ reportData.basicInfo.name }}</text>
            </view>
            <view class="info-item">
              <text class="info-lbl">年龄</text>
              <text class="info-val">{{ reportData.basicInfo.age }}岁</text>
            </view>
            <view class="info-item">
              <text class="info-lbl">性别</text>
              <text class="info-val">{{ reportData.basicInfo.gender }}</text>
            </view>
            <view class="info-item">
              <text class="info-lbl">手机号</text>
              <text class="info-val">{{ reportData.basicInfo.phone }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- ② 健康指标 -->
      <view class="card">
        <view class="card-title-row" @click="toggle('metrics')">
          <view class="title-icon-wrap red-bg"><text class="title-icon">❤️</text></view>
          <text class="card-title">健康指标</text>
          <text class="chevron" :class="{ rotated: expanded.metrics }">›</text>
        </view>
        <view v-show="expanded.metrics" class="section-body">
          <view class="metrics-grid" v-if="reportData.metrics">
            <view class="metric-item" v-for="(m, key) in metricsDisplay" :key="key">
              <view class="metric-icon-wrap" :style="{ background: m.bg }">
                <text class="metric-icon">{{ m.icon }}</text>
              </view>
              <text class="metric-name">{{ m.label }}</text>
              <text class="metric-val">{{ reportData.metrics[key] ? reportData.metrics[key].value : '--' }}</text>
              <text class="metric-unit">{{ m.unit }}</text>
              <view class="metric-status" :class="statusClass(reportData.metrics[key] ? reportData.metrics[key].status : '--')">
                <text>{{ reportData.metrics[key] ? reportData.metrics[key].status : '--' }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- ③ 风险评估 -->
      <view class="card">
        <view class="card-title-row" @click="toggle('riskAssessment')">
          <view class="title-icon-wrap orange-bg"><text class="title-icon">📊</text></view>
          <text class="card-title">风险评估</text>
          <text class="chevron" :class="{ rotated: expanded.riskAssessment }">›</text>
        </view>
        <view v-show="expanded.riskAssessment" class="section-body">
          <view class="risk-row" v-if="reportData.riskAssessment">
            <view class="risk-score-circle" :class="reportData.riskAssessment.riskLevel">
              <text class="risk-num">{{ reportData.riskAssessment.riskScore }}</text>
              <text class="risk-max">/100</text>
            </view>
            <view class="risk-info">
              <view class="risk-badge" :class="reportData.riskAssessment.riskLevel">
                <text class="risk-text">{{ reportData.riskAssessment.riskText }}</text>
              </view>
              <text class="risk-desc">综合评估您的健康数据</text>
            </view>
          </view>
        </view>
      </view>

      <!-- ④ 本周统计 -->
      <view class="card" v-if="reportData.weekStats">
        <view class="card-title-row" @click="toggle('weekStats')">
          <view class="title-icon-wrap green-bg"><text class="title-icon">📈</text></view>
          <text class="card-title">本周统计</text>
          <text class="chevron" :class="{ rotated: expanded.weekStats }">›</text>
        </view>
        <view v-show="expanded.weekStats" class="section-body">
          <view class="week-row">
            <view class="week-item">
              <text class="week-val">{{ reportData.weekStats.avgHeartRate }}</text>
              <text class="week-unit">bpm</text>
              <text class="week-lbl">平均心率</text>
            </view>
            <view class="week-sep"></view>
            <view class="week-item">
              <text class="week-val">{{ reportData.weekStats.avgBloodOxygen }}</text>
              <text class="week-unit">%</text>
              <text class="week-lbl">平均血氧</text>
            </view>
            <view class="week-sep"></view>
            <view class="week-item">
              <text class="week-val">{{ reportData.weekStats.recordCount }}</text>
              <text class="week-unit">次</text>
              <text class="week-lbl">监测次数</text>
            </view>
          </view>
        </view>
      </view>

      <!-- ⑤ 健康建议 -->
      <view class="card" v-if="reportData.recommendations && reportData.recommendations.length">
        <view class="card-title-row" @click="toggle('recommendations')">
          <view class="title-icon-wrap green-bg"><text class="title-icon">💡</text></view>
          <text class="card-title">健康建议</text>
          <text class="chevron" :class="{ rotated: expanded.recommendations }">›</text>
        </view>
        <view v-show="expanded.recommendations" class="section-body">
          <view class="rec-list">
            <view class="rec-item" v-for="(r, i) in reportData.recommendations" :key="i">
              <view class="rec-icon-wrap">
                <text class="rec-icon">{{ r.icon }}</text>
              </view>
              <view class="rec-content">
                <text class="rec-title">{{ r.title }}</text>
                <text class="rec-desc">{{ r.desc }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 操作按钮组 -->
      <view class="action-row">
        <view class="action-btn share-action" @click="shareReport">
          <text class="action-icon">📤</text>
          <text class="action-text">分享报告</text>
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
      loading: true,
      reportData: {},
      userPhone: '',
      expanded: {
        basicInfo: true,
        metrics: true,
        riskAssessment: true,
        weekStats: true,
        recommendations: true
      },
      metricsDisplay: {
        heartRate:     { label: '心率',  icon: '❤️',  unit: 'bpm',  bg: '#FFEBEE' },
        bloodPressure: { label: '血压',  icon: '🩸',  unit: 'mmHg', bg: '#FCE4EC' },
        bloodOxygen:   { label: '血氧',  icon: '💧',  unit: '%',    bg: '#E3F2FD' },
        temperature:   { label: '体温',  icon: '🌡️', unit: '°C',   bg: '#FFF3E0' }
      }
    }
  },

  onLoad() {
    try {
      const info = uni.getStorageSync('userInfo')
      this.userPhone = info ? info.phone : ''
    } catch (e) {}
    this.loadReport()
  },

  methods: {
    async loadReport() {
      this.loading = true
      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/report/${this.userPhone || 'guest'}`,
          method: 'GET'
        })
        if (res.statusCode === 200 && res.data.code === 200) {
          this.reportData = res.data.data
        }
      } catch (e) {
        uni.showToast({ title: '报告加载失败', icon: 'none' })
      }
      this.loading = false
    },

    toggle(key) {
      this.expanded[key] = !this.expanded[key]
    },

    statusClass(status) {
      if (status === '正常') return 'status-normal'
      if (status === '偏低') return 'status-low'
      if (status === '偏高' || status === '发热') return 'status-high'
      return ''
    },

    goToAssessment() {
      uni.navigateTo({ url: '/pages/assessment/assessment' })
    },

    shareReport() {
      uni.showModal({
        title: '分享健康报告',
        content: '可截图分享给家人或医生',
        confirmText: '知道了',
        showCancel: false,
        success: () => {
          uni.showToast({ title: '请截屏保存后分享', icon: 'none' })
        }
      })
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #F1F5F1;
  padding: 16rpx 0 8rpx;
  box-sizing: border-box;
}

/* ===== 日期提示栏 ===== */
.report-date-bar {
  display: flex;
  align-items: center;
  padding: 14rpx 24rpx;
  margin: 0 20rpx 16rpx;
  background: #ffffff;
  border-radius: 18rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}
.report-date-icon { font-size: 28rpx; margin-right: 10rpx; }
.report-date-text { font-size: 24rpx; color: #9E9E9E; }

/* ===== 加载动画 ===== */
.loading-box  { display: flex; flex-direction: column; align-items: center; padding: 100rpx 0; }
.loading-dots { display: flex; gap: 12rpx; margin-bottom: 20rpx; }
.dot {
  width: 18rpx; height: 18rpx; border-radius: 50%;
  background: #66BB6A;
  animation: bounce 1.2s infinite;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-12rpx); }
}
.loading-text { font-size: 26rpx; color: #9E9E9E; }

/* ===== 通用卡片 ===== */
.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 0;
  margin: 0 20rpx 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
  overflow: hidden;
}

.card-title-row {
  display: flex; align-items: center;
  padding: 24rpx 28rpx;
  /* 可点击样式 */
  position: relative;
}
.card-title-row:active { background: #F8FBF8; }

.title-icon-wrap {
  width: 60rpx; height: 60rpx; border-radius: 18rpx;
  display: flex; align-items: center; justify-content: center;
  margin-right: 16rpx; flex-shrink: 0;
}
.blue-bg   { background: #E3F2FD; }
.red-bg    { background: #FFEBEE; }
.orange-bg { background: #FFF3E0; }
.green-bg  { background: #E8F5E9; }
.title-icon { font-size: 30rpx; }

.card-title { flex: 1; font-size: 30rpx; font-weight: bold; color: #2E3A2E; }

/* 折叠箭头 */
.chevron {
  font-size: 40rpx;
  color: #BDBDBD;
  transform: rotate(90deg);
  transition: transform 0.25s ease;
  display: inline-block;
  line-height: 1;
}
.chevron.rotated { transform: rotate(-90deg); }

/* 内容区域 */
.section-body {
  padding: 0 28rpx 24rpx;
  border-top: 1rpx solid #F5F5F5;
}

/* ===== 个人信息 ===== */
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16rpx; padding-top: 20rpx; }
.info-item { background: #F8FBF8; border-radius: 16rpx; padding: 20rpx 16rpx; }
.info-lbl  { display: block; font-size: 22rpx; color: #9E9E9E; margin-bottom: 6rpx; }
.info-val  { display: block; font-size: 28rpx; font-weight: bold; color: #2E3A2E; }

/* ===== 健康指标 ===== */
.metrics-grid {
  display: grid; grid-template-columns: 1fr 1fr;
  gap: 16rpx; padding-top: 20rpx;
}
.metric-item {
  background: #F8FBF8; border-radius: 20rpx;
  padding: 20rpx; display: flex; flex-direction: column; align-items: center;
}
.metric-icon-wrap {
  width: 70rpx; height: 70rpx; border-radius: 20rpx;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 10rpx;
}
.metric-icon { font-size: 34rpx; }
.metric-name { font-size: 22rpx; color: #9E9E9E; margin-bottom: 4rpx; }
.metric-val  { font-size: 32rpx; font-weight: bold; color: #2E3A2E; }
.metric-unit { font-size: 20rpx; color: #9E9E9E; margin-bottom: 8rpx; }
.metric-status { padding: 5rpx 16rpx; border-radius: 12rpx; font-size: 20rpx; }
.status-normal { background: #E8F5E9; color: #2E7D32; }
.status-low    { background: #E3F2FD; color: #1565C0; }
.status-high   { background: #FFEBEE; color: #C62828; }

/* ===== 风险评估 ===== */
.risk-row { display: flex; align-items: center; padding-top: 20rpx; }
.risk-score-circle {
  width: 130rpx; height: 130rpx; border-radius: 50%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  margin-right: 30rpx; flex-shrink: 0;
}
.risk-score-circle.low    { background: linear-gradient(135deg,#A5D6A7,#66BB6A); box-shadow: 0 6rpx 18rpx rgba(102,187,106,0.4); }
.risk-score-circle.medium { background: linear-gradient(135deg,#FFE082,#FFB300); box-shadow: 0 6rpx 18rpx rgba(255,179,0,0.4); }
.risk-score-circle.high   { background: linear-gradient(135deg,#EF9A9A,#E53935); box-shadow: 0 6rpx 18rpx rgba(229,57,53,0.4); }
.risk-num { font-size: 52rpx; font-weight: bold; color: #fff; line-height: 1; }
.risk-max { font-size: 20rpx; color: rgba(255,255,255,0.8); }
.risk-info { flex: 1; }
.risk-badge {
  display: inline-flex; padding: 10rpx 24rpx; border-radius: 30rpx; margin-bottom: 10rpx;
}
.risk-badge.low    { background: #E8F5E9; }
.risk-badge.medium { background: #FFF8E1; }
.risk-badge.high   { background: #FFEBEE; }
.risk-text  { font-size: 28rpx; font-weight: bold; color: #2E3A2E; }
.risk-desc  { display: block; font-size: 22rpx; color: #9E9E9E; margin-bottom: 14rpx; }

/* 深度评估入口 */
.ai-assess-btn {
  display: flex; align-items: center;
  background: linear-gradient(135deg, #E8F5E9, #C8E6C9);
  border-radius: 20rpx; padding: 14rpx 18rpx;
}
.ai-assess-icon { font-size: 28rpx; margin-right: 8rpx; }
.ai-assess-text { flex: 1; font-size: 24rpx; color: #2E7D32; font-weight: 500; }
.ai-assess-arrow { font-size: 32rpx; color: #66BB6A; }

/* ===== 本周统计 ===== */
.week-row { display: flex; align-items: center; padding-top: 20rpx; }
.week-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
.week-val  { font-size: 40rpx; font-weight: bold; color: #2E7D32; }
.week-unit { font-size: 22rpx; color: #9E9E9E; margin-bottom: 4rpx; }
.week-lbl  { font-size: 22rpx; color: #9E9E9E; }
.week-sep  { width: 1rpx; height: 60rpx; background: #F0F0F0; }

/* ===== 健康建议 ===== */
.rec-list { display: flex; flex-direction: column; gap: 14rpx; padding-top: 20rpx; }
.rec-item { display: flex; align-items: flex-start; background: #F8FBF8; border-radius: 18rpx; padding: 18rpx; }
.rec-icon-wrap {
  width: 60rpx; height: 60rpx; border-radius: 16rpx;
  background: #E8F5E9;
  display: flex; align-items: center; justify-content: center;
  margin-right: 16rpx; flex-shrink: 0;
}
.rec-icon  { font-size: 30rpx; }
.rec-content { flex: 1; }
.rec-title { display: block; font-size: 28rpx; font-weight: bold; color: #2E3A2E; margin-bottom: 6rpx; }
.rec-desc  { display: block; font-size: 24rpx; color: #757575; line-height: 1.6; }

/* ===== 底部操作按钮 ===== */
.action-row {
  display: flex; gap: 16rpx;
  padding: 0 20rpx 10rpx;
}
.action-btn {
  flex: 1; display: flex; align-items: center; justify-content: center;
  padding: 28rpx 0; border-radius: 24rpx;
  box-shadow: 0 6rpx 18rpx rgba(0,0,0,0.12);
}
.action-icon { font-size: 34rpx; margin-right: 10rpx; }
.action-text { font-size: 28rpx; font-weight: bold; color: #fff; }
.assess-action { background: linear-gradient(135deg, #42A5F5, #1565C0); box-shadow: 0 6rpx 18rpx rgba(21,101,192,0.35); }
.share-action  { background: linear-gradient(135deg, #66BB6A, #2E7D32); box-shadow: 0 6rpx 18rpx rgba(46,125,50,0.35); }

.bottom-space { height: 20rpx; }
</style>
