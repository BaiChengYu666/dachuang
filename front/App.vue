<script>
export default {
  // ======================================================
  // 全局共享数据：所有页面通过 getApp().globalData 读取
  // ======================================================
  globalData: {
    currentBehavior:  'Standing',  // Standing | Walking | Falling
    isFalling:        false,
    behaviorStartTime: 0,          // 当前行为开始时间戳（ms）
    behaviorHistory:  []           // [{time,activity,location,type}, ...]
  },

  onLaunch: function () {
    console.log('App Launch')
    this.globalData.behaviorStartTime = Date.now()
    this.globalData.behaviorHistory = [{
      time:     this._fmtTime(new Date()),
      activity: '站立',
      location: '客厅',
      type:     'stand'
    }]
  },

  onShow: function () { console.log('App Show') },
  onHide: function () { console.log('App Hide') },

  methods: {
    // ----------------------------------------------------------
    // pushBehavior(activityType)
    // 任何页面调用 getApp().pushBehavior('Walking') 即可：
    //   1. 更新 globalData.currentBehavior
    //   2. 追加一条带时间戳的轨迹记录
    // ----------------------------------------------------------
    pushBehavior(activityType) {
      if (!activityType) return
      const gd  = this.globalData
      const key = activityType.toLowerCase()

      // 同一行为不重复记录
      if (gd.currentBehavior && gd.currentBehavior.toLowerCase() === key) return

      gd.currentBehavior   = activityType
      gd.isFalling         = (key === 'falling' || activityType === '跌倒')
      gd.behaviorStartTime = Date.now()

      const labelMap = { standing: '站立', walking: '步行', falling: '跌倒' }
      const typeMap  = { standing: 'stand', walking: 'walk', falling: 'fall' }

      gd.behaviorHistory.unshift({
        time:     this._fmtTime(new Date()),
        activity: labelMap[key] || activityType,
        location: '客厅',
        type:     typeMap[key]  || 'stand'
      })

      if (gd.behaviorHistory.length > 30) gd.behaviorHistory.pop()
    },

    // 当前行为已持续时长
    getBehaviorDuration() {
      const ms = Date.now() - (this.globalData.behaviorStartTime || Date.now())
      const s  = Math.floor(ms / 1000)
      if (s < 60) return `${s}秒`
      const m = Math.floor(s / 60)
      if (m < 60) return `${m}分钟`
      return `${Math.floor(m / 60)}小时${m % 60}分钟`
    },

    _fmtTime(date) {
      return date.getHours().toString().padStart(2,'0') + ':' +
             date.getMinutes().toString().padStart(2,'0')
    }
  }
}
</script>

<style>
/* 每个页面公共 css */
page { background-color: #f5f5f5; }

.uni-row    { flex-direction: row; }
.uni-column { flex-direction: column; }

view, text, image { box-sizing: border-box; }
</style>
