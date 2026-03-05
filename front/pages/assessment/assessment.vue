<template>
  <view class="container">

    <!-- 输入表单 -->
    <view class="form-card">
      <view class="form-card-title">
        <text class="form-card-title-icon">📝</text>
        <text class="form-card-title-text">填写健康指标</text>
      </view>

      <view class="form-item" v-for="field in fields" :key="field.key">
        <view class="item-header">
          <view class="item-label-row">
            <text class="item-emoji">{{ field.icon }}</text>
            <text class="item-label">{{ field.label }}</text>
          </view>
          <view class="item-range-tag">
            <text class="item-range">{{ field.range }}</text>
          </view>
        </view>
        <view class="input-wrap">
          <input
            class="input"
            type="number"
            :placeholder="field.placeholder"
            placeholder-style="color:#BDBDBD"
            v-model.number="data[field.key]"
          />
          <text class="input-unit">{{ field.unit }}</text>
        </view>
      </view>

      <!-- 按钮组 -->
      <view class="btn-group">
        <view class="btn btn-clear" @click="clearData">
          <text class="btn-icon">🗑️</text>
          <text class="btn-text">清空</text>
        </view>
        <view class="btn btn-assess" :class="loading ? 'btn-loading' : ''" @click="assess">
          <text class="btn-icon">{{ loading ? '⏳' : '🚀' }}</text>
          <text class="btn-text">{{ loading ? 'AI 分析中...' : '立即评估' }}</text>
        </view>
      </view>
    </view>

    <!-- 评估结果 -->
    <view v-if="result" class="result-card">

      <!-- 分数头部 -->
      <view class="result-header">
        <view class="score-ring" :class="'level-' + result.risk_level">
          <text class="score-num">{{ Math.round(result.risk_score) }}</text>
          <text class="score-label">/100</text>
        </view>
        <view class="result-info">
          <view class="result-level-badge" :class="'level-' + result.risk_level">
            <text class="result-level-text">{{ getLevelText(result.risk_level) }}</text>
          </view>
          <text class="result-level-desc">{{ getLevelDesc(result.risk_level) }}</text>
          <view class="result-tag-row">
            <view class="result-tag">
              <text class="result-tag-text">🤖 AI 分析</text>
            </view>
            <view class="result-tag">
              <text class="result-tag-text">🔬 Transformer</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 异常指标 -->
      <view class="section" v-if="result.details.abnormal_items.length > 0">
        <view class="section-title-row">
          <view class="section-icon-wrap warn-bg"><text>⚠️</text></view>
          <text class="section-title">异常指标</text>
          <view class="count-badge warn-badge">
            <text>{{ result.details.abnormal_items.length }}</text>
          </view>
        </view>
        <view class="abnormal-list">
          <view class="abnormal-item" v-for="(item, i) in result.details.abnormal_items" :key="i">
            <view class="abnormal-dot"></view>
            <text class="abnormal-text">{{ item }}</text>
          </view>
        </view>
      </view>

      <!-- 全部正常 -->
      <view class="section all-normal" v-else>
        <text class="all-normal-icon">✅</text>
        <text class="all-normal-text">所有指标均在正常范围内</text>
      </view>

      <!-- 健康建议 -->
      <view class="section">
        <view class="section-title-row">
          <view class="section-icon-wrap green-bg"><text>💡</text></view>
          <text class="section-title">健康建议</text>
        </view>
        <view class="advice-box">
          <text class="advice-text">{{ result.advice }}</text>
        </view>
      </view>

      <!-- 详细说明 -->
      <view class="section" v-if="result.details.warnings.length > 0">
        <view class="section-title-row">
          <view class="section-icon-wrap red-bg"><text>🚨</text></view>
          <text class="section-title">详细说明</text>
        </view>
        <view class="warning-list">
          <view class="warning-item" v-for="(w, i) in result.details.warnings" :key="i">
            <text class="warning-text">{{ w }}</text>
          </view>
        </view>
      </view>

      <!-- 重新评估 -->
      <view class="reassess-btn" @click="clearData">
        <text class="reassess-icon">🔄</text>
        <text class="reassess-text">重新评估</text>
      </view>

    </view>

    <view class="bottom-space"></view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      data: { heartRate: null, bpHigh: null, bpLow: null, oxygen: null, temp: null },
      loading: false,
      result: null,
      userAge: 70,
      userGender: 0,
      fields: [
        { key: 'heartRate', icon: '❤️', label: '心率',     unit: 'bpm',  placeholder: '例如：72',   range: '正常 60-100' },
        { key: 'bpHigh',   icon: '📈', label: '收缩压',   unit: 'mmHg', placeholder: '例如：120',  range: '正常 90-140' },
        { key: 'bpLow',    icon: '📉', label: '舒张压',   unit: 'mmHg', placeholder: '例如：80',   range: '正常 60-90' },
        { key: 'oxygen',   icon: '🫁', label: '血氧饱和度', unit: '%',   placeholder: '例如：98',   range: '正常 95-100' },
        { key: 'temp',     icon: '🌡️', label: '体温',     unit: '°C',   placeholder: '例如：36.5', range: '正常 36-37.5' }
      ]
    }
  },

  onLoad() {
    try {
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo) {
        this.userAge    = userInfo.age || 70
        this.userGender = userInfo.gender === 'male' ? 1 : 0
      }
    } catch (e) {}
  },

  methods: {
    async assess() {
      if (!this.validate()) return
      this.loading = true
      try {
        const features = [
          this.data.heartRate, this.data.bpHigh, this.data.bpLow,
          this.data.oxygen, this.data.temp, this.userAge, this.userGender
        ]
        const res = await uni.request({
          url: 'http://localhost:5000/predict',
          method: 'POST',
          header: { 'Content-Type': 'application/json' },
          data: { features }
        })
        if (res.statusCode === 200) {
          this.result = res.data
          uni.showToast({ title: '评估完成', icon: 'success' })
          setTimeout(() => { uni.pageScrollTo({ scrollTop: 9999, duration: 400 }) }, 200)
        }
      } catch (e) {
        this.result = this.mockAssess()
        uni.showToast({ title: '评估完成（本地模式）', icon: 'success' })
        setTimeout(() => { uni.pageScrollTo({ scrollTop: 9999, duration: 400 }) }, 200)
      } finally {
        this.loading = false
      }
    },

    validate() {
      const { heartRate, bpHigh, bpLow, oxygen, temp } = this.data
      if (!heartRate || !bpHigh || !bpLow || !oxygen || !temp) {
        uni.showToast({ title: '请填写全部指标', icon: 'none' }); return false
      }
      if (heartRate < 0 || heartRate > 300 || bpHigh < 0 || bpHigh > 300 ||
          bpLow < 0 || bpLow > 200 || oxygen < 0 || oxygen > 100 ||
          temp < 30 || temp > 45) {
        uni.showToast({ title: '数值超出合理范围', icon: 'none' }); return false
      }
      return true
    },

    mockAssess() {
      const { heartRate, bpHigh, bpLow, oxygen, temp } = this.data
      let score = 0; let abnormal = []; let warnings = []
      if (heartRate < 60) { score += Math.min((60 - heartRate) * 2, 40); abnormal.push('心率过低'); warnings.push(`心率 ${heartRate} bpm 低于正常值`) }
      else if (heartRate > 100) { score += Math.min((heartRate - 100) * 1.5, 40); abnormal.push('心率过高'); warnings.push(`心率 ${heartRate} bpm 高于正常值`) }
      if (bpHigh > 140) { score += Math.min((bpHigh - 140) * 0.5, 35); abnormal.push('收缩压偏高'); warnings.push(`收缩压 ${bpHigh} mmHg 偏高`) }
      else if (bpHigh < 90) { score += Math.min((90 - bpHigh) * 0.6, 30); abnormal.push('收缩压偏低'); warnings.push(`收缩压 ${bpHigh} mmHg 偏低`) }
      if (bpLow > 90) { score += Math.min((bpLow - 90) * 0.5, 30); abnormal.push('舒张压偏高'); warnings.push(`舒张压 ${bpLow} mmHg 偏高`) }
      else if (bpLow < 60) { score += Math.min((60 - bpLow) * 0.6, 25); abnormal.push('舒张压偏低'); warnings.push(`舒张压 ${bpLow} mmHg 偏低`) }
      if (oxygen < 95) { score += Math.min((95 - oxygen) * 5, 50); abnormal.push('血氧严重偏低'); warnings.push(`血氧 ${oxygen}% 严重偏低，请立即就医`) }
      if (temp > 37.5) { score += Math.min((temp - 37.5) * 10, 35); abnormal.push('体温偏高'); warnings.push(`体温 ${temp}°C 偏高`) }
      else if (temp < 36) { score += Math.min((36 - temp) * 10, 30); abnormal.push('体温偏低'); warnings.push(`体温 ${temp}°C 偏低`) }
      if (abnormal.length >= 3) { score += 20; warnings.push('多项指标异常，强烈建议立即就医') }
      score = Math.min(Math.round(score), 100)
      const level = score >= 70 ? 'high' : score >= 40 ? 'medium' : 'low'
      const advice = level === 'high'
        ? '情况较为严重，建议立即联系家人并就医检查。' + (abnormal.length ? '\n异常指标：' + abnormal.join('、') : '')
        : level === 'medium'
        ? '部分指标异常，建议尽快就医。' + (abnormal.length ? '\n异常指标：' + abnormal.join('、') : '')
        : '当前健康状况良好，请继续保持规律生活习惯。'
      return { risk_score: score, risk_level: level, advice, details: { abnormal_items: abnormal, warnings, abnormal_count: abnormal.length } }
    },

    clearData() {
      this.data = { heartRate: null, bpHigh: null, bpLow: null, oxygen: null, temp: null }
      this.result = null
      uni.pageScrollTo({ scrollTop: 0, duration: 300 })
    },

    getLevelText(level) {
      return { low: '低风险', medium: '中风险', high: '高风险' }[level] || '--'
    },
    getLevelDesc(level) {
      return { low: '健康状况良好', medium: '需要关注', high: '请及时就医' }[level] || '--'
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

/* ===== 表单卡片 ===== */
.form-card {
  background: #fff;
  border-radius: 28rpx;
  padding: 28rpx;
  margin: 0 20rpx 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
}
.form-card-title {
  display: flex; align-items: center;
  margin-bottom: 24rpx; padding-bottom: 20rpx;
  border-bottom: 1rpx solid #F5F5F5;
}
.form-card-title-icon { font-size: 32rpx; margin-right: 12rpx; }
.form-card-title-text { font-size: 30rpx; font-weight: bold; color: #2E3A2E; }

.form-item { margin-bottom: 24rpx; }

.item-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 12rpx;
}
.item-label-row { display: flex; align-items: center; }
.item-emoji   { font-size: 28rpx; margin-right: 10rpx; }
.item-label   { font-size: 28rpx; color: #2E3A2E; font-weight: 500; }
.item-range-tag {
  background: #E8F5E9; border-radius: 12rpx; padding: 5rpx 14rpx;
}
.item-range   { font-size: 20rpx; color: #388E3C; }

.input-wrap {
  display: flex; align-items: center;
  background: #F8FAF8; border-radius: 18rpx;
  border: 2rpx solid #E8F5E9;
  padding: 0 20rpx;
  height: 90rpx;
}
.input {
  flex: 1; font-size: 30rpx; color: #2E3A2E;
  height: 90rpx; background: transparent;
}
.input-unit { font-size: 24rpx; color: #9E9E9E; flex-shrink: 0; margin-left: 8rpx; }

/* ===== 按钮组 ===== */
.btn-group { display: flex; gap: 16rpx; margin-top: 32rpx; }

.btn {
  flex: 1; height: 96rpx; border-radius: 48rpx;
  display: flex; align-items: center; justify-content: center;
  gap: 10rpx;
}
.btn-icon { font-size: 30rpx; }
.btn-text { font-size: 30rpx; font-weight: bold; }

.btn-clear {
  background: #F5F5F5;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.06);
}
.btn-clear .btn-text { color: #757575; }

.btn-assess {
  background: linear-gradient(135deg, #42A5F5, #1565C0);
  box-shadow: 0 6rpx 20rpx rgba(21,101,192,0.35);
}
.btn-assess .btn-text { color: #fff; }
.btn-loading { opacity: 0.75; }

/* ===== 结果卡片 ===== */
.result-card {
  background: #fff;
  border-radius: 28rpx;
  padding: 28rpx;
  margin: 0 20rpx 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
}

/* 分数头部 */
.result-header { display: flex; align-items: center; margin-bottom: 28rpx; }

.score-ring {
  width: 150rpx; height: 150rpx; border-radius: 50%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  margin-right: 28rpx; flex-shrink: 0;
}
.level-low    { background: linear-gradient(135deg, #66BB6A, #2E7D32); box-shadow: 0 8rpx 22rpx rgba(46,125,50,0.35); }
.level-medium { background: linear-gradient(135deg, #FFB300, #E65100); box-shadow: 0 8rpx 22rpx rgba(230,81,0,0.35); }
.level-high   { background: linear-gradient(135deg, #EF5350, #B71C1C); box-shadow: 0 8rpx 22rpx rgba(183,28,28,0.35); }

.score-num   { font-size: 56rpx; font-weight: bold; color: #fff; line-height: 1; }
.score-label { font-size: 22rpx; color: rgba(255,255,255,0.8); margin-top: 4rpx; }

.result-info { flex: 1; }

.result-level-badge {
  display: inline-flex; padding: 10rpx 28rpx; border-radius: 30rpx;
  margin-bottom: 10rpx;
}
.result-level-badge.level-low    { background: #E8F5E9; }
.result-level-badge.level-medium { background: #FFF3E0; }
.result-level-badge.level-high   { background: #FFEBEE; }
.result-level-text { font-size: 30rpx; font-weight: bold; color: #2E3A2E; }

.result-level-desc {
  display: block; font-size: 24rpx; color: #9E9E9E; margin-bottom: 14rpx;
}
.result-tag-row { display: flex; gap: 10rpx; }
.result-tag {
  background: #E8F5E9; border-radius: 12rpx; padding: 6rpx 14rpx;
}
.result-tag-text { font-size: 20rpx; color: #388E3C; }

/* 通用区块 */
.section { margin-bottom: 24rpx; }

.section-title-row { display: flex; align-items: center; margin-bottom: 16rpx; }

.section-icon-wrap {
  width: 52rpx; height: 52rpx; border-radius: 14rpx;
  display: flex; align-items: center; justify-content: center;
  margin-right: 14rpx; font-size: 26rpx;
}
.warn-bg  { background: #FFF8E1; }
.green-bg { background: #E8F5E9; }
.red-bg   { background: #FFEBEE; }

.section-title { font-size: 28rpx; font-weight: bold; color: #2E3A2E; flex: 1; }

.count-badge {
  width: 40rpx; height: 40rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 22rpx; font-weight: bold;
}
.warn-badge { background: #FFF8E1; color: #F57F17; }

/* 异常指标 */
.abnormal-list {
  background: #FFFDE7; border-radius: 18rpx; padding: 18rpx 20rpx;
  border-left: 6rpx solid #FFB300;
  display: flex; flex-direction: column; gap: 12rpx;
}
.abnormal-item { display: flex; align-items: center; }
.abnormal-dot  { width: 12rpx; height: 12rpx; border-radius: 50%; background: #FFB300; margin-right: 12rpx; flex-shrink: 0; }
.abnormal-text { font-size: 26rpx; color: #E65100; }

/* 全部正常 */
.all-normal {
  display: flex; align-items: center;
  background: #E8F5E9; border-radius: 18rpx; padding: 24rpx 20rpx;
}
.all-normal-icon { font-size: 36rpx; margin-right: 14rpx; }
.all-normal-text { font-size: 28rpx; color: #2E7D32; font-weight: 500; }

/* 健康建议 */
.advice-box {
  background: #E8F5E9; border-radius: 18rpx;
  padding: 20rpx; border-left: 6rpx solid #66BB6A;
}
.advice-text { font-size: 26rpx; color: #2E7D32; line-height: 1.8; }

/* 详细说明 */
.warning-list { display: flex; flex-direction: column; gap: 12rpx; }
.warning-item {
  background: #FFEBEE; border-radius: 14rpx;
  padding: 16rpx 20rpx; border-left: 6rpx solid #EF5350;
}
.warning-text { font-size: 24rpx; color: #C62828; line-height: 1.6; }

/* 重新评估 */
.reassess-btn {
  display: flex; align-items: center; justify-content: center;
  background: #F5F5F5; border-radius: 24rpx;
  padding: 22rpx 0; margin-top: 10rpx;
  gap: 10rpx;
}
.reassess-icon { font-size: 28rpx; }
.reassess-text { font-size: 28rpx; font-weight: bold; color: #757575; }

.bottom-space { height: 30rpx; }
</style>
