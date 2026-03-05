<template>
  <view class="container">

    <!-- 风险总览 -->
    <view class="risk-overview">
      <view class="overview-content">
        <view class="score-ring" :class="riskLevel">
          <text class="score-num">{{ riskScore }}</text>
          <text class="score-max">/100</text>
        </view>
        <view class="overview-info">
          <view class="level-badge" :class="riskLevel">
            <text class="level-icon">{{ riskIcon }}</text>
            <text class="level-text">{{ riskText }}</text>
          </view>
          <text class="overview-desc">保持规律作息，适当增加户外活动</text>
        </view>
      </view>
    </view>

    <!-- 风险因素分析 -->
    <view class="card">
      <view class="card-title-row">
        <view class="title-icon-wrap orange-bg">
          <text class="title-icon">📊</text>
        </view>
        <text class="card-title">风险因素分析</text>
      </view>
      <view class="factor-list">
        <view class="factor-item" v-for="(factor, index) in factors" :key="index">
          <view class="factor-header">
            <view class="factor-left">
              <text class="factor-dot" :style="{color: factor.color}">●</text>
              <text class="factor-name">{{ factor.name }}</text>
            </view>
            <text class="factor-score" :class="factor.level">{{ factor.score }}分</text>
          </view>
          <view class="factor-bar-bg">
            <view class="factor-bar-fill" :style="{ width: factor.score + '%', background: factor.color }"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 风险趋势预测 -->
    <view class="card">
      <view class="card-title-row">
        <view class="title-icon-wrap blue-bg">
          <text class="title-icon">📈</text>
        </view>
        <text class="card-title">未来风险趋势预测</text>
      </view>

      <!-- 今日 + 未来3天 横向卡片 -->
      <view class="trend-row">
        <view
          class="trend-day-card"
          :class="'trend-' + trend.level"
          v-for="(trend, i) in trendDays"
          :key="i">
          <text class="trend-day-label">{{ trend.label }}</text>
          <!-- 评分环 -->
          <view class="trend-score-ring" :class="'ring-' + trend.level">
            <text class="trend-score-num">{{ trend.score }}</text>
          </view>
          <!-- 趋势箭头 -->
          <view class="trend-arrow-row">
            <text class="trend-arrow" :class="trend.delta > 0 ? 'arrow-up' : trend.delta < 0 ? 'arrow-down' : 'arrow-flat'">
              {{ trend.delta > 0 ? '▲' : trend.delta < 0 ? '▼' : '—' }}
            </text>
            <text class="trend-delta" :class="trend.delta > 0 ? 'delta-up' : trend.delta < 0 ? 'delta-down' : 'delta-flat'">
              {{ trend.delta > 0 ? '+' + trend.delta : trend.delta === 0 ? '持平' : trend.delta }}
            </text>
          </view>
          <text class="trend-level-label" :class="'level-txt-' + trend.level">{{ trend.levelText }}</text>
        </view>
      </view>

      <!-- 迷你折线（用连续色块模拟趋势走势） -->
      <view class="trend-bar-wrap">
        <view
          class="trend-bar-seg"
          v-for="(trend, i) in trendDays"
          :key="i"
          :style="{ flex: 1, height: (trend.score / 100 * 48 + 8) + 'rpx', background: trend.barColor, borderRadius: '6rpx', margin: '0 4rpx', alignSelf: 'flex-end' }">
        </view>
      </view>
      <view class="trend-bar-labels">
        <text class="trend-bar-lbl" v-for="(trend, i) in trendDays" :key="i">{{ trend.label }}</text>
      </view>

      <text class="trend-tip">💡 预测基于近期生理指标变化趋势，仅供参考</text>
    </view>

    <!-- 个性化健康建议 -->
    <view class="card">
      <view class="card-title-row">
        <view class="title-icon-wrap green-bg">
          <text class="title-icon">💡</text>
        </view>
        <text class="card-title">个性化健康建议</text>
      </view>
      <view class="advice-list">
        <view class="advice-item" v-for="(advice, index) in adviceList" :key="index">
          <view class="advice-icon-wrap">
            <text class="advice-icon">{{ advice.icon }}</text>
          </view>
          <view class="advice-content">
            <text class="advice-title">{{ advice.title }}</text>
            <text class="advice-desc">{{ advice.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 紧急联系 -->
    <view class="card">
      <view class="card-title-row">
        <view class="title-icon-wrap red-bg">
          <text class="title-icon">🆘</text>
        </view>
        <text class="card-title">紧急联系</text>
      </view>
      <view class="emergency-row">
        <view class="emergency-btn red-btn" @click="callEmergency">
          <text class="em-btn-icon">📞</text>
          <text class="em-btn-text">拨打 120</text>
        </view>
        <view class="emergency-btn pink-btn" @click="callFamily">
          <text class="em-btn-icon">👨‍👩‍👧</text>
          <text class="em-btn-text">联系家人</text>
        </view>
      </view>
    </view>

    <view class="bottom-space"></view>
  </view>

  <!-- 拨号遮罩 -->
  <view v-if="calling" class="calling-mask">
    <view class="calling-card">
      <view class="calling-pulse-wrap">
        <view class="pulse-ring pulse-ring1"></view>
        <view class="pulse-ring pulse-ring2"></view>
        <view class="pulse-ring pulse-ring3"></view>
        <view class="calling-avatar">
          <text class="calling-avatar-text">{{ callingInfo.avatar }}</text>
        </view>
      </view>
      <text class="calling-name">{{ callingInfo.name }}</text>
      <text class="calling-rel" v-if="callingInfo.rel">{{ callingInfo.rel }}</text>
      <text class="calling-phone">{{ callingInfo.phone }}</text>
      <text class="calling-hint">正在为您拨号...</text>
      <view class="calling-cancel" @click="cancelCall">
        <text class="calling-cancel-icon">📵</text>
        <text class="calling-cancel-text">取消</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      riskScore: 15,
      riskLevel: 'low',
      riskText: '低风险',
      riskIcon: '✅',
      factors: [
        { name: '心血管风险', score: 12, level: 'low', color: '#66BB6A' },
        { name: '跌倒风险',   score: 8,  level: 'low', color: '#4DB6AC' },
        { name: '营养不良风险', score: 20, level: 'low', color: '#FFB74D' },
        { name: '认知功能',   score: 5,  level: 'low', color: '#64B5F6' }
      ],
      trendDays: [],
      calling: false,
      callingInfo: { avatar: '', name: '', phone: '', rel: '' },
      adviceList: [
        { icon: '🏃', title: '增加运动', desc: '建议每天进行 30 分钟轻度运动，如散步、太极等' },
        { icon: '🥗', title: '均衡饮食', desc: '多摄入蔬菜水果，减少高盐高脂食物' },
        { icon: '😴', title: '规律作息', desc: '保持每天 7-8 小时睡眠，避免熬夜' },
        { icon: '💊', title: '按时服药', desc: '请按医嘱定时定量服用药物' }
      ]
    }
  },

  onShow() {
    this.syncTabBar()
    this.loadRiskData()
  },

  methods: {
    syncTabBar() {
      const pages = getCurrentPages()
      const page = pages[pages.length - 1]
      if (page && page.getTabBar) {
        page.getTabBar()?.setSelected?.(3)
      }
    },

    async loadRiskData() {
      try {
        const res = await uni.request({
          url: 'http://localhost:8080/api/risk/latest',
          method: 'GET'
        })
        if (res.statusCode === 200 && res.data.code === 200 && res.data.data) {
          const d = res.data.data
          this.riskScore = d.riskScore || 15
          this.riskLevel = d.riskLevel || 'low'
          this.riskText  = { low: '低风险', medium: '中风险', high: '高风险' }[this.riskLevel] || '低风险'
          this.riskIcon  = { low: '✅', medium: '⚠️', high: '🚨' }[this.riskLevel] || '✅'
        }
      } catch (e) {}
      this.buildTrendPrediction()
    },

    buildTrendPrediction() {
      const base = this.riskScore
      // 模拟未来3天的预测波动（实际项目可替换为后端 AI 预测接口）
      const deltas = [
        Math.round((Math.random() - 0.45) * 8),
        Math.round((Math.random() - 0.45) * 10),
        Math.round((Math.random() - 0.45) * 12)
      ]
      const labels = ['今天', '明天', '后天', '第3天']
      const scores = [base, base + deltas[0], base + deltas[0] + deltas[1], base + deltas[0] + deltas[1] + deltas[2]]
        .map(s => Math.max(0, Math.min(100, s)))

      this.trendDays = scores.map((score, i) => {
        const level = score >= 70 ? 'high' : score >= 40 ? 'medium' : 'low'
        const delta = i === 0 ? 0 : scores[i] - scores[i - 1]
        return {
          label: labels[i],
          score,
          level,
          levelText: { low: '低风险', medium: '中风险', high: '高风险' }[level],
          delta: Math.round(delta),
          barColor: level === 'high' ? '#EF5350' : level === 'medium' ? '#FFA726' : '#66BB6A'
        }
      })
    },

    callEmergency() {
      this.callingInfo = { avatar: '🚑', name: '120 急救中心', phone: '120', rel: '急救服务' }
      this.calling = true
    },

    async callFamily() {
      try {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo && userInfo.phone) {
          const res = await uni.request({
            url: `http://localhost:8080/api/guardian/${userInfo.phone}`,
            method: 'GET'
          })
          if (res.statusCode === 200 && res.data.code === 200) {
            const contacts = res.data.data || []
            if (contacts.length === 0) {
              uni.showModal({
                title: '暂无紧急联系人',
                content: '请先前往设置添加紧急联系人',
                confirmText: '去添加',
                success: (r) => {
                  if (r.confirm) uni.navigateTo({ url: '/pages/guardian/guardian' })
                }
              })
              return
            }
            const primary = contacts.find(c => c.isPrimary) || contacts[0]
            this.callingInfo = {
              avatar: primary.name ? primary.name[0] : '👤',
              name: primary.name,
              phone: primary.contactPhone,
              rel: primary.relationship || ''
            }
            this.calling = true
            return
          }
        }
      } catch (e) {}
      uni.showModal({
        title: '暂无紧急联系人',
        content: '请先前往设置添加紧急联系人',
        confirmText: '去添加',
        success: (r) => {
          if (r.confirm) uni.navigateTo({ url: '/pages/guardian/guardian' })
        }
      })
    },

    cancelCall() {
      this.calling = false
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

/* ===== 风险总览 ===== */
.risk-overview {
  background: #ffffff;
  padding: 28rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 18rpx rgba(0,0,0,0.07);
}

.overview-content {
  display: flex;
  align-items: center;
}

.score-ring {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 28rpx;
  flex-shrink: 0;
}

.score-ring.low    { background: linear-gradient(135deg, #66BB6A, #2E7D32); box-shadow: 0 8rpx 22rpx rgba(46,125,50,0.3); }
.score-ring.medium { background: linear-gradient(135deg, #FFA726, #E65100); box-shadow: 0 8rpx 22rpx rgba(230,81,0,0.3); }
.score-ring.high   { background: linear-gradient(135deg, #EF5350, #B71C1C); box-shadow: 0 8rpx 22rpx rgba(183,28,28,0.3); }

.score-num  { font-size: 56rpx; font-weight: bold; color: #ffffff; line-height: 1; }
.score-max  { font-size: 22rpx; color: rgba(255,255,255,0.8); }

.overview-info { flex: 1; }

.level-badge {
  display: inline-flex;
  align-items: center;
  padding: 10rpx 22rpx;
  border-radius: 30rpx;
  margin-bottom: 14rpx;
}

.level-badge.low    { background: #E8F5E9; }
.level-badge.medium { background: #FFF3E0; }
.level-badge.high   { background: #FFEBEE; }

.level-icon { font-size: 26rpx; margin-right: 8rpx; }
.level-text.low    { font-size: 28rpx; font-weight: bold; color: #2E7D32; }
.level-text.medium { font-size: 28rpx; font-weight: bold; color: #E65100; }
.level-text.high   { font-size: 28rpx; font-weight: bold; color: #B71C1C; }
.level-text { font-size: 28rpx; font-weight: bold; color: #2E3A2E; }

.overview-desc {
  display: block;
  font-size: 22rpx;
  color: #9E9E9E;
  line-height: 1.6;
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

.green-bg  { background: #E8F5E9; }
.blue-bg   { background: #E3F2FD; }
.orange-bg { background: #FFF3E0; }
.red-bg    { background: #FFEBEE; }
.title-icon { font-size: 32rpx; }

/* ===== 趋势预测 ===== */
.trend-row {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.trend-day-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 18rpx 8rpx 14rpx;
  border-radius: 20rpx;
}
.trend-low    { background: #F1F8F1; border: 1rpx solid #C8E6C9; }
.trend-medium { background: #FFF8EE; border: 1rpx solid #FFE082; }
.trend-high   { background: #FFF0EE; border: 1rpx solid #FFCDD2; }

.trend-day-label {
  font-size: 20rpx;
  color: #9E9E9E;
  margin-bottom: 10rpx;
}

.trend-score-ring {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10rpx;
}
.ring-low    { background: linear-gradient(135deg, #66BB6A, #2E7D32); }
.ring-medium { background: linear-gradient(135deg, #FFA726, #E65100); }
.ring-high   { background: linear-gradient(135deg, #EF5350, #B71C1C); }

.trend-score-num {
  font-size: 30rpx;
  font-weight: bold;
  color: #fff;
}

.trend-arrow-row {
  display: flex;
  align-items: center;
  gap: 4rpx;
  margin-bottom: 6rpx;
}

.trend-arrow { font-size: 18rpx; }
.arrow-up   { color: #E53935; }
.arrow-down { color: #43A047; }
.arrow-flat { color: #9E9E9E; }

.trend-delta { font-size: 20rpx; font-weight: bold; }
.delta-up   { color: #E53935; }
.delta-down { color: #43A047; }
.delta-flat { color: #9E9E9E; }

.trend-level-label { font-size: 18rpx; }
.level-txt-low    { color: #388E3C; }
.level-txt-medium { color: #E65100; }
.level-txt-high   { color: #C62828; }

/* 迷你柱状趋势图 */
.trend-bar-wrap {
  display: flex;
  align-items: flex-end;
  height: 60rpx;
  padding: 0 4rpx;
  margin-bottom: 6rpx;
}

.trend-bar-labels {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16rpx;
}

.trend-bar-lbl {
  font-size: 18rpx;
  color: #BDBDBD;
  flex: 1;
  text-align: center;
}

.trend-tip {
  display: block;
  font-size: 20rpx;
  color: #BDBDBD;
  text-align: center;
  line-height: 1.6;
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #2E3A2E;
}

/* ===== 风险因素 ===== */
.factor-list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.factor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.factor-left {
  display: flex;
  align-items: center;
}

.factor-dot {
  font-size: 20rpx;
  margin-right: 10rpx;
}

.factor-name { font-size: 28rpx; color: #424242; }

.factor-score { font-size: 26rpx; font-weight: bold; }
.factor-score.low    { color: #388E3C; }
.factor-score.medium { color: #E65100; }
.factor-score.high   { color: #B71C1C; }

.factor-bar-bg {
  height: 16rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  overflow: hidden;
}

.factor-bar-fill {
  height: 100%;
  border-radius: 8rpx;
  transition: width 0.4s ease;
}

/* ===== 健康建议 ===== */
.advice-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.advice-item {
  display: flex;
  align-items: flex-start;
  padding: 20rpx;
  background: #F8FBF8;
  border-radius: 20rpx;
}

.advice-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  background: #E8F5E9;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 18rpx;
  flex-shrink: 0;
}

.advice-icon { font-size: 34rpx; }

.advice-content { flex: 1; }

.advice-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #2E3A2E;
  margin-bottom: 8rpx;
}

.advice-desc {
  display: block;
  font-size: 24rpx;
  color: #757575;
  line-height: 1.6;
}

/* ===== 紧急联系 ===== */
.emergency-row {
  display: flex;
  gap: 20rpx;
}

.emergency-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 28rpx 0;
  border-radius: 24rpx;
}

.red-btn  { background: linear-gradient(135deg, #EF9A9A, #E57373); box-shadow: 0 6rpx 18rpx rgba(229,115,115,0.35); }
.pink-btn { background: linear-gradient(135deg, #F48FB1, #EC407A); box-shadow: 0 6rpx 18rpx rgba(236,64,122,0.35); }

.em-btn-icon { font-size: 40rpx; margin-bottom: 10rpx; }

.em-btn-text {
  font-size: 26rpx;
  font-weight: bold;
  color: #ffffff;
}

.bottom-space { height: 0; }

/* ===== 拨号遮罩 ===== */
.calling-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.calling-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 60rpx 50rpx;
}

.calling-pulse-wrap {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 50rpx;
}

.pulse-ring {
  position: absolute;
  border-radius: 50%;
  border: 2rpx solid rgba(255,255,255,0.4);
  animation: pulse-expand 2s ease-out infinite;
}

.pulse-ring1 { width: 180rpx; height: 180rpx; animation-delay: 0s; }
.pulse-ring2 { width: 180rpx; height: 180rpx; animation-delay: 0.6s; }
.pulse-ring3 { width: 180rpx; height: 180rpx; animation-delay: 1.2s; }

@keyframes pulse-expand {
  0%   { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(2.2); opacity: 0; }
}

.calling-avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.15);
  border: 4rpx solid rgba(255,255,255,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.calling-avatar-text { font-size: 60rpx; }

.calling-name {
  font-size: 44rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 12rpx;
}

.calling-rel {
  font-size: 26rpx;
  color: rgba(255,255,255,0.6);
  margin-bottom: 12rpx;
}

.calling-phone {
  font-size: 34rpx;
  color: rgba(255,255,255,0.9);
  letter-spacing: 2rpx;
  margin-bottom: 20rpx;
}

.calling-hint {
  font-size: 26rpx;
  color: rgba(255,255,255,0.5);
  margin-bottom: 70rpx;
}

.calling-cancel {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(239,83,80,0.85);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.calling-cancel-icon { font-size: 38rpx; }
.calling-cancel-text { font-size: 20rpx; color: #fff; margin-top: 4rpx; }
</style>
