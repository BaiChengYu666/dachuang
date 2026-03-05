<template>
  <view class="container">

    <!-- 当前数值卡片 -->
    <view class="banner">
      <view class="banner-inner">
        <view class="banner-left">
          <text class="banner-title">{{ metaInfo.title }}</text>
          <view class="banner-val-row">
            <text class="banner-val">{{ currentDisplay }}</text>
            <text class="banner-unit"> {{ metaInfo.unit }}</text>
          </view>
          <view class="status-chip">
            <text class="status-dot">●</text>
            <text class="status-txt">{{ currentStatus }}</text>
          </view>
        </view>
        <text class="banner-icon">{{ metaInfo.icon }}</text>
      </view>
    </view>

    <!-- 统计摘要 -->
    <view class="stats-row">
      <view class="stat-item">
        <text class="stat-val blue-c">{{ stats.min }}</text>
        <text class="stat-lbl">最低</text>
      </view>
      <view class="stat-sep"></view>
      <view class="stat-item">
        <text class="stat-val green-c">{{ stats.avg }}</text>
        <text class="stat-lbl">平均</text>
      </view>
      <view class="stat-sep"></view>
      <view class="stat-item">
        <text class="stat-val red-c">{{ stats.max }}</text>
        <text class="stat-lbl">最高</text>
      </view>
    </view>

    <!-- 折线图 -->
    <view class="chart-card">
      <view class="chart-top-row">
        <text class="chart-card-title">趋势图</text>
        <view class="period-tabs">
          <view
            class="p-tab"
            :class="period === '今日' ? 'p-active' : ''"
            @click="switchPeriod('今日')">今日</view>
          <view
            class="p-tab"
            :class="period === '7天' ? 'p-active' : ''"
            @click="switchPeriod('7天')">7天</view>
        </view>
      </view>
      <canvas
        canvas-id="lineChart"
        class="chart-canvas"
        :style="{ width: canvasW + 'px', height: canvasH + 'px' }">
      </canvas>
    </view>

    <!-- 历史记录列表 -->
    <view class="list-card">
      <text class="list-title">历史记录</text>
      <view class="rec-item" v-for="(rec, i) in displayHistory" :key="i">
        <text class="rec-time">{{ rec.time }}</text>
        <text class="rec-val">{{ rec.display }}</text>
        <view class="rec-tag" :style="{ background: rec.tagBg, color: rec.tagColor }">
          {{ rec.statusText }}
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
      type: 'heartRate',
      period: '今日',
      canvasW: 310,
      canvasH: 180,
      rawHourly: [],
      rawHourlyFull: [],
      raw7day: [],
      raw7dayFull: [],
      stats: { min: '--', avg: '--', max: '--' },
      currentDisplay: '--',
      currentStatus: '正常',
      metaMap: {
        heartRate:     { title: '心率监测',   unit: 'bpm',  gradient: 'linear-gradient(160deg,#C62828,#EF5350)', icon: '❤️',  label: 'BPM' },
        bloodPressure: { title: '血压监测',   unit: 'mmHg', gradient: 'linear-gradient(160deg,#6A1B9A,#AB47BC)', icon: '🩸',  label: 'mmHg' },
        bloodOxygen:   { title: '血氧监测',   unit: '%',    gradient: 'linear-gradient(160deg,#1565C0,#42A5F5)', icon: '💧',  label: 'SpO₂' },
        temperature:   { title: '体温监测',   unit: '°C',   gradient: 'linear-gradient(160deg,#E65100,#FFA726)', icon: '🌡️', label: '°C' }
      }
    }
  },

  computed: {
    metaInfo() {
      return this.metaMap[this.type] || this.metaMap.heartRate
    },
    chartData() {
      return this.period === '今日' ? this.rawHourly : this.raw7day
    },
    displayHistory() {
      const result = []
      if (this.period === '今日') {
        for (let i = 23; i >= 0; i--) {
          const timeStr = String(i).padStart(2, '0') + ':00'
          result.push(this.makeRecord(this.rawHourlyFull[i], timeStr))
        }
      } else {
        const now = new Date()
        const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        for (let i = 0; i <= 6; i++) {
          const d = new Date(now)
          d.setDate(d.getDate() - i)
          const label = i === 0 ? '今天' : dayNames[d.getDay()]
          result.push(this.makeRecord(this.raw7dayFull[6 - i], label))
        }
      }
      return result
    }
  },

  onLoad(options) {
    this.type = options.type || 'heartRate'
    const sys = uni.getSystemInfoSync()
    this.canvasW = sys.windowWidth - 56
    this.canvasH = Math.round(this.canvasW * 0.52)
    this.generateData()
  },

  onReady() {
    this.$nextTick(() => {
      setTimeout(() => { this.drawChart() }, 100)
    })
  },

  methods: {
    // ── 数据生成 ──────────────────────────────────
    generateData() {
      const h24 = [], h24full = []
      for (let i = 0; i < 24; i++) {
        const v = this.genHourVal(i, 0)
        h24full.push(v)
        h24.push(this.toChartNum(v))
      }
      this.rawHourly = h24
      this.rawHourlyFull = h24full

      const d7 = [], d7full = []
      for (let day = 0; day < 7; day++) {
        const dayVals = Array.from({ length: 24 }, (_, h) => this.genHourVal(h, day - 6))
        const aggr = this.aggregateDay(dayVals)
        d7.push(this.toChartNum(aggr))
        d7full.push(aggr)
      }
      this.raw7day = d7
      this.raw7dayFull = d7full

      this.computeStats(this.rawHourly, this.rawHourlyFull)

      // 当前值 = 最新小时
      const latest = h24full[h24full.length - 1]
      this.currentDisplay = this.formatVal(latest)
      this.currentStatus = this.getStatus(latest).text
    },

    genHourVal(hour, dayOffset) {
      // dayOffset: 0 = today, -1 = yesterday, etc.
      const jitter = () => (Math.random() - 0.5) * 2
      switch (this.type) {
        case 'heartRate': {
          const base = hour < 6 ? 63 : hour < 9 ? 73 : hour < 12 ? 80 : hour < 14 ? 73 : hour < 18 ? 77 : hour < 21 ? 71 : 66
          return Math.round(base + jitter() * 5 + dayOffset * 0.5)
        }
        case 'bloodPressure': {
          const baseSys = hour < 6 ? 115 : hour < 9 ? 128 : hour < 18 ? 132 : 124
          const sys = Math.round(baseSys + jitter() * 6 + dayOffset * 0.3)
          const dia = Math.round(78 + jitter() * 4)
          return { sys, dia }
        }
        case 'bloodOxygen': {
          const base = hour < 6 ? 96.3 : 98.1
          return Math.min(100, Math.round((base + jitter() * 0.8) * 10) / 10)
        }
        case 'temperature': {
          const base = hour < 6 ? 36.1 : hour < 12 ? 36.45 : hour < 18 ? 36.75 : hour < 21 ? 36.6 : 36.3
          return Math.round((base + jitter() * 0.15) * 10) / 10
        }
      }
    },

    aggregateDay(dayVals) {
      if (this.type === 'bloodPressure') {
        const avgSys = Math.round(dayVals.reduce((a, v) => a + v.sys, 0) / 24)
        const avgDia = Math.round(dayVals.reduce((a, v) => a + v.dia, 0) / 24)
        return { sys: avgSys, dia: avgDia }
      }
      const sum = dayVals.reduce((a, v) => a + v, 0)
      if (this.type === 'heartRate') return Math.round(sum / 24)
      return Math.round(sum / 24 * 10) / 10
    },

    toChartNum(v) {
      return typeof v === 'object' ? v.sys : Number(v)
    },

    formatVal(v) {
      if (!v && v !== 0) return '--'
      if (this.type === 'bloodPressure') return `${v.sys}/${v.dia}`
      return String(v)
    },

    getStatus(v) {
      const n = typeof v === 'object' ? v.sys : Number(v)
      switch (this.type) {
        case 'heartRate':
          if (n < 60) return { text: '偏低', color: '#1565C0', bg: '#E3F2FD' }
          if (n > 100) return { text: '偏高', color: '#C62828', bg: '#FFEBEE' }
          return { text: '正常', color: '#2E7D32', bg: '#E8F5E9' }
        case 'bloodPressure':
          if (n < 90) return { text: '偏低', color: '#1565C0', bg: '#E3F2FD' }
          if (n > 140) return { text: '偏高', color: '#C62828', bg: '#FFEBEE' }
          return { text: '正常', color: '#2E7D32', bg: '#E8F5E9' }
        case 'bloodOxygen':
          if (n < 95) return { text: '偏低', color: '#C62828', bg: '#FFEBEE' }
          return { text: '正常', color: '#2E7D32', bg: '#E8F5E9' }
        case 'temperature':
          if (n < 36) return { text: '偏低', color: '#1565C0', bg: '#E3F2FD' }
          if (n >= 37.3) return { text: '发热', color: '#C62828', bg: '#FFEBEE' }
          return { text: '正常', color: '#2E7D32', bg: '#E8F5E9' }
      }
      return { text: '正常', color: '#2E7D32', bg: '#E8F5E9' }
    },

    makeRecord(v, timeLabel) {
      const s = this.getStatus(v)
      return {
        time: timeLabel,
        display: this.formatVal(v) + ' ' + this.metaInfo.unit,
        statusText: s.text,
        tagColor: s.color,
        tagBg: s.bg
      }
    },

    computeStats(nums, fullData) {
      if (!nums || !nums.length) return
      const min = Math.min(...nums)
      const max = Math.max(...nums)
      const avg = nums.reduce((a, b) => a + b, 0) / nums.length

      if (this.type === 'bloodPressure') {
        const dias = fullData.map(v => v.dia)
        const minD = Math.min(...dias), maxD = Math.max(...dias)
        const avgD = Math.round(dias.reduce((a, b) => a + b, 0) / dias.length)
        this.stats = {
          min: `${min}/${minD}`,
          avg: `${Math.round(avg)}/${avgD}`,
          max: `${max}/${maxD}`
        }
      } else if (this.type === 'temperature' || this.type === 'bloodOxygen') {
        this.stats = {
          min: min.toFixed(1),
          avg: avg.toFixed(1),
          max: max.toFixed(1)
        }
      } else {
        this.stats = {
          min: Math.round(min),
          avg: Math.round(avg),
          max: Math.round(max)
        }
      }
    },

    switchPeriod(p) {
      this.period = p
      const nums = p === '今日' ? this.rawHourly : this.raw7day
      const full = p === '今日' ? this.rawHourlyFull : this.raw7dayFull
      this.computeStats(nums, full)
      this.$nextTick(() => {
        setTimeout(() => { this.drawChart() }, 50)
      })
    },

    // ── Canvas 绘图 ────────────────────────────────
    drawChart() {
      const data = this.chartData
      if (!data || data.length < 2) return

      const W = this.canvasW
      const H = this.canvasH
      const pad = { top: 22, right: 16, bottom: 34, left: 46 }
      const cW = W - pad.left - pad.right
      const cH = H - pad.top - pad.bottom

      const vals = data.map(Number)
      let minV = Math.min(...vals)
      let maxV = Math.max(...vals)
      const margin = (maxV - minV) * 0.25 || 5
      minV = minV - margin
      maxV = maxV + margin
      const range = maxV - minV

      const lineColorMap = {
        heartRate: '#EF5350',
        bloodPressure: '#AB47BC',
        bloodOxygen: '#42A5F5',
        temperature: '#FFA726'
      }
      const fillColorMap = {
        heartRate: 'rgba(239,83,80,0.12)',
        bloodPressure: 'rgba(171,71,188,0.12)',
        bloodOxygen: 'rgba(66,165,245,0.12)',
        temperature: 'rgba(255,167,38,0.12)'
      }
      const lineColor = lineColorMap[this.type] || '#66BB6A'
      const fillColor = fillColorMap[this.type] || 'rgba(102,187,106,0.12)'

      const ctx = uni.createCanvasContext('lineChart', this)
      ctx.clearRect(0, 0, W, H)

      // 水平网格线 + Y 轴标签
      ctx.setFontSize(9)
      for (let i = 0; i <= 4; i++) {
        const y = pad.top + (cH / 4) * i
        ctx.setStrokeStyle('#EFEFEF')
        ctx.setLineWidth(1)
        ctx.beginPath()
        ctx.moveTo(pad.left, y)
        ctx.lineTo(pad.left + cW, y)
        ctx.stroke()

        const labelVal = maxV - (range / 4) * i
        let labelStr
        if (this.type === 'temperature' || this.type === 'bloodOxygen') {
          labelStr = labelVal.toFixed(1)
        } else {
          labelStr = String(Math.round(labelVal))
        }
        ctx.setFillStyle('#BDBDBD')
        ctx.fillText(labelStr, 2, y + 4)
      }

      // X 轴标签
      ctx.setFontSize(9)
      ctx.setFillStyle('#BDBDBD')
      if (this.period === '今日') {
        const xLabels = [0, 4, 8, 12, 16, 20, 23]
        xLabels.forEach(h => {
          const x = pad.left + (h / 23) * cW
          ctx.fillText(String(h), x - 4, H - 4)
        })
      } else {
        const dayLabels = ['7天前', '', '', '4天前', '', '', '今天']
        vals.forEach((_, i) => {
          const x = pad.left + (i / (vals.length - 1)) * cW
          if (dayLabels[i]) ctx.fillText(dayLabels[i], x - 10, H - 4)
        })
      }

      // 计算像素坐标
      const points = vals.map((v, i) => ({
        x: pad.left + (i / (vals.length - 1)) * cW,
        y: pad.top + (1 - (v - minV) / range) * cH
      }))

      // 填充区域
      ctx.beginPath()
      ctx.moveTo(points[0].x, pad.top + cH)
      points.forEach(p => ctx.lineTo(p.x, p.y))
      ctx.lineTo(points[points.length - 1].x, pad.top + cH)
      ctx.closePath()
      ctx.setFillStyle(fillColor)
      ctx.fill()

      // 折线
      ctx.beginPath()
      ctx.setStrokeStyle(lineColor)
      ctx.setLineWidth(2)
      ctx.setLineCap('round')
      ctx.setLineJoin('round')
      points.forEach((p, i) => {
        i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y)
      })
      ctx.stroke()

      // 数据点（24小时每4个显示一个，7天全显示）
      const dotInterval = this.period === '今日' ? 4 : 1
      points.forEach((p, i) => {
        if (i % dotInterval !== 0 && i !== points.length - 1) return
        ctx.beginPath()
        ctx.arc(p.x, p.y, 3.5, 0, Math.PI * 2)
        ctx.setFillStyle('#ffffff')
        ctx.setStrokeStyle(lineColor)
        ctx.setLineWidth(1.5)
        ctx.fill()
        ctx.stroke()
      })

      // 数值标签（显示在数据点上方或下方）
      ctx.setFontSize(10)
      ctx.setTextAlign('center')
      ctx.setFillStyle(lineColor)
      points.forEach((p, i) => {
        if (i % dotInterval !== 0 && i !== points.length - 1) return
        const val = vals[i]
        let labelStr
        if (this.type === 'temperature' || this.type === 'bloodOxygen') {
          labelStr = Number(val).toFixed(1)
        } else {
          labelStr = String(Math.round(val))
        }
        // 靠近顶部时标签放到点下方，否则放到点上方
        const yLabel = p.y - pad.top < 18 ? p.y + 14 : p.y - 7
        ctx.fillText(labelStr, p.x, yLabel)
      })

      ctx.draw()
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

/* ===== 当前数值卡片 ===== */
.banner {
  background: #ffffff;
  padding: 28rpx 28rpx 24rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 18rpx rgba(0,0,0,0.07);
}

.banner-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.banner-left { flex: 1; }

.banner-title {
  display: block;
  font-size: 26rpx;
  color: #9E9E9E;
  margin-bottom: 10rpx;
}

.banner-val-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 14rpx;
}

.banner-val {
  font-size: 68rpx;
  font-weight: bold;
  color: #2E3A2E;
  line-height: 1;
}

.banner-unit {
  font-size: 24rpx;
  color: #9E9E9E;
  margin-left: 6rpx;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  background: #E8F5E9;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
}

.status-dot {
  font-size: 18rpx;
  color: #4CAF50;
  margin-right: 8rpx;
}

.status-txt {
  font-size: 24rpx;
  color: #388E3C;
  font-weight: 500;
}

.banner-icon {
  font-size: 72rpx;
  opacity: 0.9;
  margin-left: 16rpx;
  flex-shrink: 0;
}

/* ===== 统计摘要 ===== */
.stats-row {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 24rpx;
  margin: 0 20rpx 20rpx;
  padding: 24rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-val {
  font-size: 34rpx;
  font-weight: bold;
  margin-bottom: 6rpx;
}

.stat-lbl {
  font-size: 22rpx;
  color: #9E9E9E;
}

.blue-c  { color: #1E88E5; }
.green-c { color: #43A047; }
.red-c   { color: #E53935; }

.stat-sep {
  width: 1rpx;
  height: 60rpx;
  background: #F0F0F0;
}

/* ===== 图表卡片 ===== */
.chart-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  margin: 0 20rpx 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}

.chart-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.chart-card-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #2E3A2E;
}

.period-tabs {
  display: flex;
  background: #F5F5F5;
  border-radius: 20rpx;
  overflow: hidden;
}

.p-tab {
  padding: 10rpx 28rpx;
  font-size: 24rpx;
  color: #9E9E9E;
  border-radius: 20rpx;
  transition: all 0.2s;
}

.p-active {
  background: #4CAF50;
  color: #ffffff;
  font-weight: bold;
}

.chart-canvas {
  display: block;
  width: 100%;
}

/* ===== 历史列表 ===== */
.list-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  margin: 0 20rpx 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}

.list-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #2E3A2E;
  margin-bottom: 16rpx;
}

.rec-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F5F5F5;
}

.rec-item:last-child { border-bottom: none; }

.rec-time {
  font-size: 26rpx;
  color: #9E9E9E;
  width: 110rpx;
  flex-shrink: 0;
}

.rec-val {
  flex: 1;
  font-size: 28rpx;
  font-weight: bold;
  color: #2E3A2E;
}

.rec-tag {
  font-size: 22rpx;
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
}

.bottom-space { height: 20rpx; }
</style>
