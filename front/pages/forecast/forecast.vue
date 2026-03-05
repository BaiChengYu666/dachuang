<template>
  <view class="container">

    <!-- 顶部卡片 -->
    <view class="header-card">
      <view class="header-row">
        <view>
          <text class="header-title">趋势预测</text>
          <text class="header-sub">Transformer · 注意力机制</text>
        </view>
        <view class="hour-toggle">
          <view class="toggle-btn" :class="{active: hours===24}" @click="setHours(24)">
            <text>24小时</text>
          </view>
          <view class="toggle-btn" :class="{active: hours===48}" @click="setHours(48)">
            <text>48小时</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载中 -->
    <view class="loading-card" v-if="loading">
      <view class="pulse-wrap">
        <view class="pulse-dot d1"></view>
        <view class="pulse-dot d2"></view>
        <view class="pulse-dot d3"></view>
      </view>
      <text class="loading-text">AI模型推理中...</text>
      <text class="loading-sub">正在利用Transformer分析历史数据</text>
    </view>

    <template v-else>
      <!-- 摘要卡片 -->
      <view class="summary-row" v-if="summary">
        <view class="sum-card" v-for="s in summaryItems" :key="s.label">
          <text class="sum-icon">{{ s.icon }}</text>
          <text class="sum-val" :style="{color: s.color}">{{ s.value }}</text>
          <text class="sum-label">{{ s.label }}</text>
        </view>
      </view>

      <!-- 心率预测图 -->
      <view class="chart-card">
        <view class="chart-header">
          <view class="chart-title-row">
            <text class="chart-icon">❤️</text>
            <text class="chart-name">心率预测（bpm）</text>
          </view>
          <view class="legend-row">
            <view class="leg"><view class="leg-dot" style="background:#E53935;"></view><text class="leg-t">&gt;100 偏高</text></view>
            <view class="leg"><view class="leg-dot" style="background:#4CAF50;"></view><text class="leg-t">正常区间</text></view>
          </view>
        </view>
        <scroll-view scroll-x="true" class="chart-scroll">
          <svg :width="svgW" height="130" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="hrDanger" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#FF5722" stop-opacity="0.18"/>
                <stop offset="100%" stop-color="#FF5722" stop-opacity="0"/>
              </linearGradient>
            </defs>
            <!-- 高风险区域 (>100) -->
            <rect x="0" :y="hrY(110)" :width="svgW" :height="hrY(100)-hrY(110)" fill="url(#hrDanger)"/>
            <!-- 阈值线 100 -->
            <line x1="0" :y1="hrY(100)" :x2="svgW" :y2="hrY(100)" stroke="#FF5722" stroke-width="1" stroke-dasharray="5,3" opacity="0.7"/>
            <text x="4" :y="hrY(100)-3" font-size="9" fill="#FF5722" opacity="0.8">100</text>
            <!-- 阈值线 60 -->
            <line x1="0" :y1="hrY(60)" :x2="svgW" :y2="hrY(60)" stroke="#FF9800" stroke-width="1" stroke-dasharray="5,3" opacity="0.7"/>
            <text x="4" :y="hrY(60)-3" font-size="9" fill="#FF9800" opacity="0.8">60</text>
            <!-- 预测折线 -->
            <polyline :points="hrPoints" fill="none" stroke="#E53935" stroke-width="2.5" stroke-dasharray="7,4" stroke-linecap="round"/>
            <!-- 数据点 -->
            <circle v-for="(d,i) in hrDots" :key="'hr'+i" :cx="d.x" :cy="d.y" r="4"
              :fill="d.high ? '#FF5722' : '#4CAF50'" stroke="#fff" stroke-width="1.5"/>
            <!-- 值标签（每4个一个 + 最后一个） -->
            <text v-for="(d,i) in hrDots" :key="'ht'+i"
              v-if="i % 4 === 0 || i === forecast.length - 1"
              :x="d.x"
              :y="d.y > 22 ? d.y - 7 : d.y + 16"
              font-size="11" :fill="d.high ? '#E53935' : '#43A047'"
              text-anchor="middle" font-weight="bold">{{ forecast[i] && forecast[i].heartRate }}</text>
          </svg>
        </scroll-view>
        <view class="x-labels">
          <text v-for="l in xLabels" :key="l" class="x-lbl">{{ l }}</text>
        </view>
      </view>

      <!-- 血压预测图 -->
      <view class="chart-card">
        <view class="chart-header">
          <view class="chart-title-row">
            <text class="chart-icon">💉</text>
            <text class="chart-name">血压预测（mmHg）</text>
          </view>
          <view class="legend-row">
            <view class="leg"><view class="leg-dot" style="background:#E53935;"></view><text class="leg-t">高压</text></view>
            <view class="leg"><view class="leg-dot" style="background:#1E88E5;"></view><text class="leg-t">低压</text></view>
            <view class="leg"><view class="leg-dot" style="background:#FF5722; width:20rpx; height:2rpx; border-radius:0;"></view><text class="leg-t">阈值140</text></view>
          </view>
        </view>
        <scroll-view scroll-x="true" class="chart-scroll">
          <svg :width="svgW" height="130" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="bpDanger" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#E53935" stop-opacity="0.12"/>
                <stop offset="100%" stop-color="#E53935" stop-opacity="0"/>
              </linearGradient>
            </defs>
            <!-- 高血压区域 (>140) -->
            <rect x="0" :y="bpY(175)" :width="svgW" :height="bpY(140)-bpY(175)" fill="url(#bpDanger)"/>
            <!-- 阈值线 140 -->
            <line x1="0" :y1="bpY(140)" :x2="svgW" :y2="bpY(140)" stroke="#FF5722" stroke-width="1" stroke-dasharray="5,3" opacity="0.7"/>
            <text x="4" :y="bpY(140)-3" font-size="9" fill="#FF5722" opacity="0.8">140</text>
            <!-- 高压折线 -->
            <polyline :points="bphPoints" fill="none" stroke="#E53935" stroke-width="2.5" stroke-dasharray="7,4" stroke-linecap="round"/>
            <!-- 低压折线 -->
            <polyline :points="bplPoints" fill="none" stroke="#1E88E5" stroke-width="2" stroke-dasharray="7,4" stroke-linecap="round"/>
            <!-- 高压点 -->
            <circle v-for="(d,i) in bphDots" :key="'bph'+i" :cx="d.x" :cy="d.y" r="3.5"
              :fill="d.high ? '#FF5722' : '#E53935'" stroke="#fff" stroke-width="1.5"/>
            <!-- 低压点 -->
            <circle v-for="(d,i) in bplDots" :key="'bpl'+i" :cx="d.x" :cy="d.y" r="3.5"
              fill="#1E88E5" stroke="#fff" stroke-width="1.5"/>
            <!-- 高压数值标签（每4个一个 + 最后一个） -->
            <text v-for="(d,i) in bphDots" :key="'bpht'+i"
              v-if="i % 4 === 0 || i === forecast.length - 1"
              :x="d.x"
              :y="d.y > 22 ? d.y - 7 : d.y + 16"
              font-size="11" :fill="d.high ? '#FF5722' : '#E53935'"
              text-anchor="middle" font-weight="bold">{{ forecast[i] && forecast[i].bloodPressureHigh }}</text>
            <!-- 低压数值标签（每6个一个 + 最后一个，错开避免与高压重叠） -->
            <text v-for="(d,i) in bplDots" :key="'bplt'+i"
              v-if="i % 6 === 0 || i === forecast.length - 1"
              :x="d.x"
              :y="d.y < 110 ? d.y + 15 : d.y - 7"
              font-size="11" fill="#1E88E5"
              text-anchor="middle">{{ forecast[i] && forecast[i].bloodPressureLow }}</text>
          </svg>
        </scroll-view>
        <view class="x-labels">
          <text v-for="l in xLabels" :key="l" class="x-lbl">{{ l }}</text>
        </view>
      </view>

      <!-- 风险评分预测折线图 -->
      <view class="chart-card">
        <view class="chart-header">
          <view class="chart-title-row">
            <text class="chart-icon">⚠️</text>
            <text class="chart-name">风险评分预测（0-100）</text>
          </view>
          <view class="legend-row">
            <view class="leg"><view class="leg-dot" style="background:#4CAF50;"></view><text class="leg-t">&lt;30 低</text></view>
            <view class="leg"><view class="leg-dot" style="background:#FF9800;"></view><text class="leg-t">30-60 中</text></view>
            <view class="leg"><view class="leg-dot" style="background:#F44336;"></view><text class="leg-t">&gt;60 高</text></view>
          </view>
        </view>
        <scroll-view scroll-x="true" class="chart-scroll">
          <svg :width="svgW" height="130" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="riskFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#FF9800" stop-opacity="0.25"/>
                <stop offset="100%" stop-color="#FF9800" stop-opacity="0.02"/>
              </linearGradient>
            </defs>
            <!-- 高风险阈值线 60 -->
            <line x1="0" :y1="riskY(60)" :x2="svgW" :y2="riskY(60)" stroke="#F44336" stroke-width="1" stroke-dasharray="5,3" opacity="0.7"/>
            <text x="4" :y="riskY(60)-3" font-size="9" fill="#F44336" opacity="0.85">60</text>
            <!-- 中风险阈值线 30 -->
            <line x1="0" :y1="riskY(30)" :x2="svgW" :y2="riskY(30)" stroke="#FF9800" stroke-width="1" stroke-dasharray="5,3" opacity="0.7"/>
            <text x="4" :y="riskY(30)-3" font-size="9" fill="#FF9800" opacity="0.85">30</text>
            <!-- 填充区域 -->
            <polyline
              :points="riskFillPoints"
              fill="url(#riskFill)" stroke="none"/>
            <!-- 折线 -->
            <polyline :points="riskLinePoints" fill="none" stroke="#FF9800" stroke-width="2.5" stroke-dasharray="7,4" stroke-linecap="round"/>
            <!-- 数据点（颜色跟随风险等级） -->
            <circle v-for="(d,i) in riskDots" :key="'rcd'+i" :cx="d.x" :cy="d.y" r="4"
              :fill="d.color" stroke="#fff" stroke-width="1.5"/>
            <!-- 数值标签（每4个一个 + 最后一个） -->
            <text v-for="(d,i) in riskDots" :key="'rct'+i"
              v-if="i % 4 === 0 || i === forecast.length - 1"
              :x="d.x"
              :y="d.y > 22 ? d.y - 7 : d.y + 16"
              font-size="11" :fill="d.color"
              text-anchor="middle" font-weight="bold">{{ forecast[i] && forecast[i].riskScore }}</text>
          </svg>
        </scroll-view>
        <view class="x-labels">
          <text v-for="l in xLabels" :key="l" class="x-lbl">{{ l }}</text>
        </view>

        <!-- 色块时间轴（辅助） -->
        <scroll-view scroll-x="true" style="margin-top:16rpx;">
          <view class="risk-row">
            <view v-for="p in forecast" :key="'rs'+p.hour" class="risk-block" :style="riskBlockStyle(p)">
              <text class="risk-score-txt">{{ p.riskScore }}</text>
              <text class="risk-h">{{ p.hour }}h</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 注意力权重 -->
      <view class="chart-card" v-if="attentionWeights.length > 0">
        <view class="chart-title-row" style="margin-bottom:8rpx;">
          <text class="chart-icon">🔍</text>
          <text class="chart-name">注意力分布</text>
        </view>
        <text class="chart-sub-text">历史各时间点对未来预测的贡献度（越亮越重要）</text>
        <scroll-view scroll-x="true" style="margin-top:20rpx;">
          <view class="attn-row">
            <view v-for="(w, i) in attentionWeights" :key="'aw'+i" class="attn-bar-wrap">
              <view class="attn-bar" :style="attnBarStyle(w)"></view>
              <text class="attn-lbl">-{{ attentionWeights.length - i }}h</text>
            </view>
          </view>
        </scroll-view>
        <view style="margin-top:12rpx;">
          <text class="attn-hint">高亮时段通常为数值异常或关键变化节点，模型对其给予更高权重</text>
        </view>
      </view>

      <!-- 预测摘要 -->
      <view class="findings-card" v-if="summary">
        <view class="chart-title-row" style="margin-bottom:24rpx;">
          <text class="chart-icon">📋</text>
          <text class="chart-name">预测摘要</text>
        </view>
        <view class="finding-row">
          <text class="finding-key">心率趋势</text>
          <view class="trend-tag" :style="trendStyle(summary.hrTrend)">
            <text>{{ trendText(summary.hrTrend) }}</text>
          </view>
        </view>
        <view class="finding-row">
          <text class="finding-key">血压趋势</text>
          <view class="trend-tag" :style="trendStyle(summary.bpTrend)">
            <text>{{ trendText(summary.bpTrend) }}</text>
          </view>
        </view>
        <view class="finding-row">
          <text class="finding-key">周期最高风险</text>
          <text class="finding-val" :style="{color: riskColor(summary.maxRisk)}">{{ summary.maxRisk }} 分</text>
        </view>
        <view class="finding-row">
          <text class="finding-key">平均风险评分</text>
          <text class="finding-val" :style="{color: riskColor(summary.avgRisk)}">{{ summary.avgRisk }} 分</text>
        </view>
        <view class="finding-row" v-if="summary.highRiskHours && summary.highRiskHours.length > 0">
          <text class="finding-key">高风险时段</text>
          <text class="finding-val" style="color:#F44336;">{{ summary.highRiskHours.map(h => h+'h后').join('、') }}</text>
        </view>
        <view class="finding-row" v-else>
          <text class="finding-key">高风险时段</text>
          <text class="finding-val" style="color:#4CAF50;">预测期间无高风险</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-card" v-if="!loading && forecast.length === 0">
        <text class="empty-icon">📊</text>
        <text class="empty-text">暂无足够历史数据进行预测</text>
        <text class="empty-sub">请确保后端已启动并有历史生理数据</text>
      </view>

    </template>

    <view class="bottom-space"></view>
  </view>
</template>

<script>
const COL_W = 30   // SVG x pixels per hour
const PAD_L = 15   // left padding in SVG
const SVG_H = 130
const PAD_T = 12
const PAD_B = 18
const CHART_H = SVG_H - PAD_T - PAD_B   // 100

export default {
  data() {
    return {
      hours: 24,
      loading: true,
      forecast: [],
      attentionWeights: [],
      summary: null,
      historicalCount: 0
    }
  },

  computed: {
    svgW() {
      return this.forecast.length * COL_W + PAD_L * 2
    },

    xLabels() {
      // Show a label every 4 hours
      const labels = []
      for (let h = 1; h <= this.hours; h += 4) {
        labels.push(h + 'h')
      }
      return labels
    },

    summaryItems() {
      if (!this.summary) return []
      return [
        {
          icon: '📈',
          value: this.summary.maxRisk,
          color: this.riskColor(this.summary.maxRisk),
          label: '峰值风险'
        },
        {
          icon: '📊',
          value: this.summary.avgRisk,
          color: this.riskColor(this.summary.avgRisk),
          label: '均值风险'
        },
        {
          icon: '⚡',
          value: (this.summary.highRiskHours || []).length,
          color: (this.summary.highRiskHours || []).length > 0 ? '#F44336' : '#4CAF50',
          label: '高风险时段'
        }
      ]
    },

    // Heart rate SVG
    hrDots() {
      return this.forecast.map((p, i) => ({
        x: PAD_L + i * COL_W + COL_W / 2,
        y: this.hrY(p.heartRate),
        high: p.heartRate > 100 || p.heartRate < 60
      }))
    },
    hrPoints() {
      return this.hrDots.map(d => `${d.x},${d.y}`).join(' ')
    },

    // Blood pressure SVG
    bphDots() {
      return this.forecast.map((p, i) => ({
        x: PAD_L + i * COL_W + COL_W / 2,
        y: this.bpY(p.bloodPressureHigh),
        high: p.bloodPressureHigh > 140
      }))
    },
    bphPoints() {
      return this.bphDots.map(d => `${d.x},${d.y}`).join(' ')
    },
    bplDots() {
      return this.forecast.map((p, i) => ({
        x: PAD_L + i * COL_W + COL_W / 2,
        y: this.bpY(p.bloodPressureLow)
      }))
    },
    bplPoints() {
      return this.bplDots.map(d => `${d.x},${d.y}`).join(' ')
    },

    // Risk score SVG
    riskDots() {
      return this.forecast.map((p, i) => ({
        x: PAD_L + i * COL_W + COL_W / 2,
        y: this.riskY(p.riskScore),
        color: p.riskScore >= 60 ? '#F44336' : p.riskScore >= 30 ? '#FF9800' : '#4CAF50'
      }))
    },
    riskLinePoints() {
      return this.riskDots.map(d => `${d.x},${d.y}`).join(' ')
    },
    riskFillPoints() {
      if (!this.riskDots.length) return ''
      const first = this.riskDots[0]
      const last  = this.riskDots[this.riskDots.length - 1]
      const bottom = PAD_T + (SVG_H - PAD_T - PAD_B)
      return `${first.x},${bottom} ` +
             this.riskDots.map(d => `${d.x},${d.y}`).join(' ') +
             ` ${last.x},${bottom}`
    }
  },

  onLoad() {
    this.loadForecast()
  },

  methods: {
    setHours(h) {
      this.hours = h
      this.loadForecast()
    },

    // Y coordinate for risk score (range 0-100)
    riskY(val) {
      val = Math.max(0, Math.min(100, val))
      return PAD_T + (1 - val / 100) * CHART_H
    },

    // Y coordinate for heart rate (range 55-110)
    hrY(val) {
      val = Math.max(55, Math.min(110, val))
      return PAD_T + (1 - (val - 55) / 55) * CHART_H
    },

    // Y coordinate for blood pressure (range 55-175)
    bpY(val) {
      val = Math.max(55, Math.min(175, val))
      return PAD_T + (1 - (val - 55) / 120) * CHART_H
    },

    riskColor(score) {
      if (score >= 60) return '#F44336'
      if (score >= 30) return '#FF9800'
      return '#4CAF50'
    },

    riskBlockStyle(p) {
      const color = this.riskColor(p.riskScore)
      const opacity = 0.15 + (p.riskScore / 100) * 0.6
      return {
        background: color,
        opacity: opacity.toFixed(2),
        borderColor: color
      }
    },

    attnBarStyle(w) {
      const maxW = Math.max(...this.attentionWeights)
      const ratio = maxW > 0 ? w / maxW : 0
      const h = Math.max(8, Math.round(ratio * 60))
      const opacity = 0.3 + ratio * 0.7
      return {
        height: h + 'px',
        background: `rgba(67, 160, 71, ${opacity.toFixed(2)})`
      }
    },

    trendText(t) {
      return t === 'rising' ? '↑ 上升' : t === 'falling' ? '↓ 下降' : '→ 平稳'
    },

    trendStyle(t) {
      const colors = { rising: '#FF9800', falling: '#1E88E5', stable: '#4CAF50' }
      const bgs = { rising: '#FFF3E0', falling: '#E3F2FD', stable: '#E8F5E9' }
      return {
        background: bgs[t] || '#F5F5F5',
        color: colors[t] || '#666',
        border: `1px solid ${colors[t] || '#DDD'}`
      }
    },

    async loadForecast() {
      this.loading = true
      this.forecast = []
      this.attentionWeights = []
      this.summary = null

      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/forecast/1?hours=${this.hours}`,
          method: 'GET'
        })

        if (res.statusCode === 200 && res.data.code === 200) {
          const d = res.data.data
          this.forecast = d.forecast || []
          this.attentionWeights = d.attentionWeights || []
          this.summary = d.summary || null
          this.historicalCount = d.historicalCount || 0
        } else {
          this.useMockData()
        }
      } catch (e) {
        this.useMockData()
      } finally {
        this.loading = false
      }
    },

    // Client-side mock when both backend and AI service are offline
    useMockData() {
      const rng = (seed) => {
        let s = seed
        return () => {
          s = (s * 1103515245 + 12345) & 0x7fffffff
          return s / 0x7fffffff
        }
      }
      const rand = rng(12345)

      let hr = 72, bph = 125, bpl = 80, temp = 36.5
      const forecast = []
      for (let h = 1; h <= this.hours; h++) {
        hr  = Math.max(62, Math.min(98,  Math.round(hr  + (rand() - 0.5) * 4)))
        bph = Math.max(112, Math.min(142, Math.round(bph + (rand() - 0.5) * 4)))
        bpl = Math.max(66, Math.min(94,  Math.round(bpl + (rand() - 0.5) * 3)))
        temp = Math.round((temp + (rand() - 0.5) * 0.1) * 10) / 10
        temp = Math.max(36.1, Math.min(37.3, temp))
        const oxygen = 96 + Math.round(rand() * 3)
        const risk = bph > 138 ? 32 + Math.round(rand() * 12) : 15 + Math.round(rand() * 14)
        forecast.push({
          hour: h, heartRate: hr, bloodPressureHigh: bph, bloodPressureLow: bpl,
          bloodOxygen: oxygen, bodyTemperature: temp,
          riskScore: risk, riskLevel: bph > 138 ? 'medium' : 'low'
        })
      }

      // Exponential attention weights
      const n = Math.min(20, this.hours)
      let attnWeights = Array.from({length: n}, (_, i) => Math.exp(-2 + 2 * i / (n - 1)))
      const attnSum = attnWeights.reduce((a, b) => a + b, 0)
      attnWeights = attnWeights.map(w => Math.round(w / attnSum * 1000) / 1000)

      this.forecast = forecast
      this.attentionWeights = attnWeights
      this.summary = {
        hrTrend: 'stable',
        bpTrend: 'stable',
        maxRisk: Math.max(...forecast.map(p => p.riskScore)),
        avgRisk: Math.round(forecast.reduce((s, p) => s + p.riskScore, 0) / forecast.length),
        highRiskHours: forecast.filter(p => p.riskLevel === 'high').map(p => p.hour)
      }
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #F1F5F1;
  padding: 20rpx 24rpx;
  box-sizing: border-box;
}

/* ===== 顶部卡片 ===== */
.header-card {
  background: linear-gradient(135deg, #43A047, #66BB6A);
  border-radius: 28rpx;
  padding: 36rpx 32rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(67, 160, 71, 0.3);
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  letter-spacing: 2rpx;
}
.header-sub {
  display: block;
  font-size: 22rpx;
  color: rgba(255,255,255,0.8);
  margin-top: 6rpx;
}
.hour-toggle {
  display: flex;
  background: rgba(255,255,255,0.2);
  border-radius: 30rpx;
  padding: 4rpx;
}
.toggle-btn {
  padding: 12rpx 24rpx;
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
  border-radius: 26rpx;
  transition: all 0.25s;
}
.toggle-btn.active {
  background: #fff;
  color: #43A047;
  font-weight: bold;
}

/* ===== 加载 ===== */
.loading-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 80rpx 40rpx;
  text-align: center;
  margin-bottom: 20rpx;
}
.pulse-wrap {
  display: flex;
  justify-content: center;
  gap: 14rpx;
  margin-bottom: 28rpx;
}
.pulse-dot {
  width: 18rpx;
  height: 18rpx;
  background: #43A047;
  border-radius: 50%;
  animation: pulse 1.2s ease-in-out infinite;
}
.d1 { animation-delay: 0s; }
.d2 { animation-delay: 0.2s; }
.d3 { animation-delay: 0.4s; }
@keyframes pulse {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1.2); opacity: 1; }
}
.loading-text { display: block; font-size: 30rpx; font-weight: bold; color: #333; }
.loading-sub { display: block; font-size: 22rpx; color: #888; margin-top: 8rpx; }

/* ===== 摘要行 ===== */
.summary-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}
.sum-card {
  flex: 1;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx 12rpx;
  text-align: center;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.sum-icon { display: block; font-size: 36rpx; margin-bottom: 8rpx; }
.sum-val { display: block; font-size: 34rpx; font-weight: bold; }
.sum-label { display: block; font-size: 20rpx; color: #888; margin-top: 4rpx; }

/* ===== 图表卡片 ===== */
.chart-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx 28rpx 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
}
.chart-header {
  margin-bottom: 16rpx;
}
.chart-title-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
}
.chart-icon { font-size: 30rpx; }
.chart-name { font-size: 28rpx; font-weight: bold; color: #2E3A2E; }
.legend-row {
  display: flex;
  gap: 20rpx;
  margin-top: 10rpx;
}
.leg {
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.leg-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
}
.leg-t { font-size: 20rpx; color: #888; }

.chart-scroll {
  width: 100%;
  white-space: nowrap;
  overflow-x: scroll;
}

/* X轴标签 */
.x-labels {
  display: flex;
  justify-content: space-between;
  padding: 0 4rpx;
  margin-top: 4rpx;
}
.x-lbl { font-size: 18rpx; color: #BDBDBD; }

/* ===== 风险时间轴 ===== */
.risk-row {
  display: flex;
  gap: 4rpx;
  padding: 4rpx 0;
}
.risk-block {
  min-width: 52rpx;
  height: 80rpx;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-width: 1rpx;
  border-style: solid;
}
.risk-score-txt { font-size: 18rpx; font-weight: bold; color: #fff; }
.risk-h { font-size: 16rpx; color: rgba(255,255,255,0.8); }
.risk-legend {
  display: flex;
  gap: 24rpx;
  margin-top: 16rpx;
  justify-content: center;
}

/* ===== 注意力权重 ===== */
.chart-sub-text { font-size: 22rpx; color: #888; margin-top: 4rpx; display: block; }
.attn-row {
  display: flex;
  align-items: flex-end;
  gap: 4rpx;
  height: 80px;
}
.attn-bar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 28rpx;
}
.attn-bar {
  width: 16rpx;
  border-radius: 4rpx 4rpx 0 0;
  transition: height 0.4s ease;
}
.attn-lbl { font-size: 14rpx; color: #BDBDBD; margin-top: 4rpx; }
.attn-hint { font-size: 20rpx; color: #BDBDBD; line-height: 1.5; display: block; }

/* ===== 预测摘要 ===== */
.findings-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
}
.finding-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 0;
  border-bottom: 1rpx solid #F5F5F5;
}
.finding-row:last-child { border-bottom: none; }
.finding-key { font-size: 26rpx; color: #555; }
.finding-val { font-size: 26rpx; font-weight: bold; }
.trend-tag {
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
}

/* ===== 空状态 ===== */
.empty-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 80rpx 40rpx;
  text-align: center;
  margin-bottom: 20rpx;
}
.empty-icon { display: block; font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { display: block; font-size: 28rpx; color: #555; font-weight: bold; }
.empty-sub { display: block; font-size: 22rpx; color: #BDBDBD; margin-top: 10rpx; }

.bottom-space { height: 60rpx; }
</style>
