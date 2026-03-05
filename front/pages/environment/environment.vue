<template>
  <view class="container">

    <!-- 综合评分卡 -->
    <view class="score-card">
      <view class="score-circle" :class="'lvl-' + envLevel">
        <text class="score-num">{{ envScore }}</text>
        <text class="score-sub">综合评分</text>
      </view>
      <view class="score-right">
        <text class="score-title">室内环境状况</text>
        <view class="score-badge" :class="'lvl-' + envLevel">
          <text class="score-badge-text">{{ getLevelText(envLevel) }}</text>
        </view>
        <text class="score-desc">{{ getLevelDesc(envLevel) }}</text>
      </view>
    </view>

    <!-- 实时参数 -->
    <view class="section-label">
      <text class="section-title">实时环境参数</text>
      <text class="section-hint">每 5 秒更新</text>
    </view>
    <view class="params-grid">
      <view class="param-card" v-for="(p, i) in paramList" :key="i">
        <view class="param-icon-wrap" :style="{background: p.bg}">
          <text class="param-icon">{{ p.icon }}</text>
        </view>
        <text class="param-label">{{ p.label }}</text>
        <view class="param-value-row">
          <text class="param-value">{{ p.value }}</text>
          <text class="param-unit">{{ p.unit }}</text>
        </view>
        <view class="param-status" :class="p.cls">
          <text>{{ p.statusText }}</text>
        </view>
      </view>
    </view>

    <!-- 环境建议 -->
    <view class="card" v-if="envAdvice.length > 0">
      <view class="card-title-row">
        <view class="title-icon-wrap yellow-bg">
          <text class="title-icon">💡</text>
        </view>
        <text class="card-title">环境优化建议</text>
      </view>
      <view class="advice-list">
        <view class="advice-item" v-for="(item, index) in envAdvice" :key="index">
          <view class="advice-dot"></view>
          <text class="advice-text">{{ item }}</text>
        </view>
      </view>
    </view>

    <!-- 天气预报式温度趋势 -->
    <view class="card">
      <view class="card-title-row">
        <view class="title-icon-wrap orange-bg">
          <text class="title-icon">🌡️</text>
        </view>
        <text class="card-title">今日气温变化</text>
        <text class="temp-range-text">{{ minTemp }}° ~ {{ maxTemp }}°</text>
      </view>

      <!-- SVG 折线图 -->
      <view class="svg-wrap">
        <svg viewBox="0 0 600 100" style="width:100%;height:auto;display:block;overflow:visible;">
          <defs>
            <linearGradient id="fillGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#FF9800" stop-opacity="0.25"/>
              <stop offset="100%" stop-color="#FF9800" stop-opacity="0"/>
            </linearGradient>
          </defs>
          <!-- 渐变填充 -->
          <polygon :points="svgFillPoints" fill="url(#fillGrad)"/>
          <!-- 折线 -->
          <polyline :points="svgLinePoints" fill="none" stroke="#FF9800" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
          <!-- 圆点 -->
          <circle v-for="(d, i) in svgDots" :key="i"
            :cx="d.x" :cy="d.y" r="5"
            :fill="d.isMax ? '#FF5722' : d.isMin ? '#42A5F5' : '#FF9800'"
            stroke="white" stroke-width="2.5"/>
        </svg>
      </view>

      <!-- 温度标签行 -->
      <view class="temp-num-row">
        <view class="temp-num-col" v-for="(h, i) in hourlyData" :key="i">
          <text class="temp-num"
            :class="h.temp === maxTemp ? 'num-max' : h.temp === minTemp ? 'num-min' : ''">
            {{ h.temp }}°
          </text>
        </view>
      </view>

      <!-- 图标 + 时间行 -->
      <view class="forecast-row">
        <view class="forecast-col" v-for="(h, i) in hourlyData" :key="i">
          <text class="fc-icon">{{ h.icon }}</text>
          <text class="fc-time">{{ h.time }}</text>
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
      envData: {
        temperature: 24,
        humidity: 55,
        airQuality: 35,
        light: 320,
        pm25: 12,
        co2: 450
      },
      envScore: 85,
      envLevel: 'good',
      envAdvice: [],
      maxTemp: 26,
      minTemp: 22,
      hourlyData: [
        { time: '6时',  icon: '🌙', temp: 22 },
        { time: '9时',  icon: '⛅', temp: 23 },
        { time: '12时', icon: '☀️', temp: 25 },
        { time: '15时', icon: '☀️', temp: 26 },
        { time: '18时', icon: '🌤️', temp: 24 },
        { time: '21时', icon: '🌙', temp: 23 }
      ],
      timer: null
    }
  },

  computed: {
    svgDots() {
      const W = 600, padTop = 14, padBottom = 14, H = 100
      const chartH = H - padTop - padBottom
      const temps = this.hourlyData.map(d => d.temp)
      const min = Math.min(...temps), max = Math.max(...temps)
      const range = max - min || 1
      const colW = W / temps.length
      return temps.map((t, i) => ({
        x: Math.round(colW * i + colW / 2),
        y: Math.round(padTop + (max - t) / range * chartH),
        isMax: t === max,
        isMin: t === min
      }))
    },
    svgLinePoints() {
      return this.svgDots.map(d => `${d.x},${d.y}`).join(' ')
    },
    svgFillPoints() {
      const pts = this.svgDots
      const line = pts.map(d => `${d.x},${d.y}`).join(' ')
      return `${pts[0].x},100 ${line} ${pts[pts.length - 1].x},100`
    },

    paramList() {
      const d = this.envData
      return [
        { icon: '🌡️', label: '室内温度', value: d.temperature,  unit: '°C',    bg: '#FFF0F0', cls: this.getStatusClass('temperature'), statusText: this.getStatusText('temperature') },
        { icon: '💧', label: '空气湿度', value: d.humidity,      unit: '%',     bg: '#E3F2FD', cls: this.getStatusClass('humidity'),    statusText: this.getStatusText('humidity') },
        { icon: '🌬️', label: '空气质量', value: d.airQuality,    unit: 'AQI',   bg: '#E0F2F1', cls: this.getStatusClass('airQuality'), statusText: this.getStatusText('airQuality') },
        { icon: '💡', label: '光照强度', value: d.light,          unit: 'lux',   bg: '#FFFDE7', cls: this.getStatusClass('light'),      statusText: this.getStatusText('light') },
        { icon: '🔬', label: 'PM2.5',   value: d.pm25,           unit: 'μg/m³', bg: '#F3E5F5', cls: this.getStatusClass('pm25'),       statusText: this.getStatusText('pm25') },
        { icon: '🫧', label: 'CO₂浓度', value: d.co2,             unit: 'ppm',   bg: '#EDE7F6', cls: this.getStatusClass('co2'),        statusText: this.getStatusText('co2') }
      ]
    }
  },

  onLoad() {
    this.calculateScore()
    this.generateAdvice()
    this.startSimulation()
  },

  onUnload() {
    if (this.timer) clearInterval(this.timer)
  },

  methods: {
    startSimulation() {
      this.timer = setInterval(() => {
        this.envData.temperature = Math.round((this.envData.temperature + (Math.random() - 0.5) * 0.5) * 10) / 10
        this.envData.humidity    = Math.round(this.envData.humidity + (Math.random() - 0.5) * 2)
        this.envData.airQuality  = Math.round(this.envData.airQuality + (Math.random() - 0.5) * 3)
        this.calculateScore()
        this.generateAdvice()
      }, 5000)
    },

    calculateScore() {
      let score = 100
      const d = this.envData
      if (d.temperature < 18 || d.temperature > 28) score -= 15
      else if (d.temperature < 20 || d.temperature > 26) score -= 5
      if (d.humidity < 30 || d.humidity > 70) score -= 15
      else if (d.humidity < 40 || d.humidity > 60) score -= 5
      if (d.airQuality > 100) score -= 20
      else if (d.airQuality > 50) score -= 10
      if (d.pm25 > 75) score -= 15
      else if (d.pm25 > 35) score -= 8
      if (d.co2 > 1000) score -= 15
      else if (d.co2 > 800) score -= 8
      this.envScore = Math.max(score, 0)
      this.envLevel = this.envScore >= 80 ? 'good' : this.envScore >= 60 ? 'medium' : 'poor'
    },

    generateAdvice() {
      const advice = []
      const d = this.envData
      if (d.temperature < 20)   advice.push('室内温度偏低，建议开启暖气')
      else if (d.temperature > 26) advice.push('室内温度偏高，建议开启空调或通风')
      if (d.humidity < 40)      advice.push('空气较干燥，建议使用加湿器')
      else if (d.humidity > 60) advice.push('湿度偏高，建议开启除湿或通风')
      if (d.airQuality > 50)    advice.push('空气质量一般，建议开窗通风')
      if (d.pm25 > 35)          advice.push('PM2.5偏高，建议使用空气净化器')
      if (d.co2 > 800)          advice.push('CO₂浓度偏高，建议增加通风')
      if (d.light < 300)        advice.push('光线偏暗，建议开灯以保护视力')
      this.envAdvice = advice
    },

    getStatusClass(param) {
      const v = this.envData[param]
      const ranges = {
        temperature: { good: [20, 26], medium: [18, 28] },
        humidity:    { good: [40, 60], medium: [30, 70] },
        airQuality:  { good: [0, 50],  medium: [51, 100] },
        light:       { good: [300, 1000], medium: [200, 1500] },
        pm25:        { good: [0, 35],  medium: [36, 75] },
        co2:         { good: [0, 800], medium: [801, 1000] }
      }
      const r = ranges[param]
      if (v >= r.good[0] && v <= r.good[1]) return 'st-good'
      if (v >= r.medium[0] && v <= r.medium[1]) return 'st-medium'
      return 'st-poor'
    },

    getStatusText(param) {
      const c = this.getStatusClass(param)
      return c === 'st-good' ? '良好' : c === 'st-medium' ? '一般' : '较差'
    },

    getLevelText(l) { return l === 'good' ? '优秀' : l === 'medium' ? '良好' : '需改善' },
    getLevelDesc(l) { return l === 'good' ? '室内环境舒适宜人' : l === 'medium' ? '环境状况一般，可适当调节' : '请注意环境调节' }
  }
}
</script>

<style scoped>
.container {
  background: #F1F5F1;
  padding: 16rpx 0 16rpx;
  box-sizing: border-box;
  overflow: hidden;
}

/* ===== 综合评分 ===== */
.score-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 28rpx;
  margin: 0 20rpx 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
}

.score-circle {
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

.lvl-good   { background: linear-gradient(135deg, #66BB6A, #2E7D32); box-shadow: 0 8rpx 22rpx rgba(46,125,50,0.3); }
.lvl-medium { background: linear-gradient(135deg, #FFA726, #E65100); box-shadow: 0 8rpx 22rpx rgba(230,81,0,0.3); }
.lvl-poor   { background: linear-gradient(135deg, #EF5350, #B71C1C); box-shadow: 0 8rpx 22rpx rgba(183,28,28,0.3); }

.score-num { font-size: 52rpx; font-weight: bold; color: #ffffff; line-height: 1; }
.score-sub { font-size: 20rpx; color: rgba(255,255,255,0.8); margin-top: 4rpx; }

.score-right { flex: 1; }

.score-title {
  display: block;
  font-size: 26rpx;
  color: #9E9E9E;
  margin-bottom: 12rpx;
}

.score-badge {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 22rpx;
  border-radius: 30rpx;
  margin-bottom: 14rpx;
}

.score-badge.lvl-good   { background: #E8F5E9; }
.score-badge.lvl-medium { background: #FFF3E0; }
.score-badge.lvl-poor   { background: #FFEBEE; }

.score-badge-text {
  font-size: 28rpx;
  font-weight: bold;
}
.score-badge.lvl-good   .score-badge-text { color: #2E7D32; }
.score-badge.lvl-medium .score-badge-text { color: #E65100; }
.score-badge.lvl-poor   .score-badge-text { color: #B71C1C; }

.score-desc {
  display: block;
  font-size: 22rpx;
  color: #9E9E9E;
  line-height: 1.5;
}

/* ===== 区块标题 ===== */
.section-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 26rpx 14rpx;
}

.section-title { font-size: 28rpx; font-weight: bold; color: #2E3A2E; }
.section-hint  { font-size: 22rpx; color: #A5B4A5; }

/* ===== 参数网格 ===== */
.params-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  padding: 0 20rpx 20rpx;
}

.param-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 18rpx rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
}

.param-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14rpx;
}

.param-icon { font-size: 34rpx; }

.param-label {
  font-size: 22rpx;
  color: #9E9E9E;
  margin-bottom: 8rpx;
}

.param-value-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 12rpx;
}

.param-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #2E3A2E;
}

.param-unit {
  font-size: 18rpx;
  color: #BDBDBD;
  margin-left: 4rpx;
}

.param-status {
  font-size: 20rpx;
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  align-self: flex-start;
}

.st-good   { background: #E8F5E9; color: #388E3C; }
.st-medium { background: #FFF3E0; color: #E65100; }
.st-poor   { background: #FFEBEE; color: #C62828; }

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

.yellow-bg { background: #FFFDE7; }
.blue-bg   { background: #E3F2FD; }
.orange-bg { background: #FFF3E0; }

.title-icon { font-size: 32rpx; }

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #2E3A2E;
  flex: 1;
}

/* ===== 建议 ===== */
.advice-list { display: flex; flex-direction: column; gap: 16rpx; }

.advice-item {
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  background: #F8FBF8;
  border-radius: 18rpx;
}

.advice-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #4CAF50;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.advice-text {
  font-size: 26rpx;
  color: #424242;
  line-height: 1.5;
  flex: 1;
}

/* ===== 温度趋势（天气预报式） ===== */
.temp-range-text {
  font-size: 24rpx;
  color: #9E9E9E;
}

.svg-wrap {
  margin: 0 -4rpx 4rpx;
}

/* 温度数字行 */
.temp-num-row {
  display: flex;
  padding: 0 0 12rpx;
}

.temp-num-col {
  flex: 1;
  display: flex;
  justify-content: center;
}

.temp-num {
  font-size: 22rpx;
  font-weight: bold;
  color: #757575;
}

.num-max { color: #FF5722; }
.num-min { color: #42A5F5; }

/* 图标 + 时间行 */
.forecast-row {
  display: flex;
  border-top: 1rpx solid #F0F0F0;
  padding-top: 20rpx;
}

.forecast-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.fc-icon { font-size: 36rpx; }

.fc-time {
  font-size: 20rpx;
  color: #9E9E9E;
}

.bottom-space { height: 0; }
</style>
