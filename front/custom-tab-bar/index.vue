<template>
  <view class="tab-bar">
    <view class="tab-bar-border"></view>
    <view
      v-for="(item, index) in list"
      :key="index"
      class="tab-bar-item"
      :class="index === 2 ? 'center-item' : ''"
      @click="switchTab(index)">

      <!-- 中间 AI 凸起按钮 -->
      <view v-if="index === 2" class="center-btn-wrap">
        <view class="center-btn" :class="selected === 2 ? 'center-btn-active' : ''">
          <image
            class="center-icon-img"
            :src="selected === 2 ? item.activeIcon : item.icon"
            mode="aspectFit">
          </image>
        </view>
        <text class="center-label" :class="selected === 2 ? 'center-label-active' : ''">{{ item.text }}</text>
      </view>

      <!-- 其余 Tab -->
      <view v-else class="tab-item-inner">
        <image
          class="item-icon-img"
          :src="selected === index ? item.activeIcon : item.icon"
          mode="aspectFit">
        </image>
        <text class="item-text" :class="selected === index ? 'text-active' : ''">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selected: 0,
      list: [
        { pagePath: '/pages/index/index',       text: '首页', icon: '/static/icon/home.png',     activeIcon: '/static/icon/home-active.png' },
        { pagePath: '/pages/behavior/behavior', text: '行为', icon: '/static/icon/behavior.png', activeIcon: '/static/icon/behavior-active.png' },
        { pagePath: '/pages/ai/ai',             text: 'AI',   icon: '/static/icon/ai.png',       activeIcon: '/static/icon/ai-active.png' },
        { pagePath: '/pages/risk/risk',         text: '风险', icon: '/static/icon/risk.png',     activeIcon: '/static/icon/risk-active.png' },
        { pagePath: '/pages/settings/settings', text: '设置', icon: '/static/icon/we.png',       activeIcon: '/static/icon/we-active.png' }
      ]
    }
  },
  methods: {
    switchTab(index) {
      const item = this.list[index]
      this.selected = index
      uni.switchTab({ url: item.pagePath })
    },
    setSelected(index) {
      this.selected = index
    }
  }
}
</script>

<style>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  padding-bottom: env(safe-area-inset-bottom);
  background: #ffffff;
  display: flex;
  align-items: center;
  z-index: 9999;
  box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.08);
}

.tab-bar-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1rpx;
  background: #eeeeee;
}

.tab-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  position: relative;
}

.center-item {
  position: relative;
  overflow: visible;
}

.center-btn-wrap {
  position: absolute;
  top: -44rpx;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.center-btn {
  width: 110rpx;
  height: 110rpx;
  border-radius: 55rpx;
  background: linear-gradient(145deg, #66BB6A, #388E3C);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 24rpx rgba(56, 142, 60, 0.45);
  border: 4rpx solid #ffffff;
  margin-bottom: 6rpx;
}

.center-btn-active {
  background: linear-gradient(145deg, #43A047, #2E7D32);
  box-shadow: 0 8rpx 28rpx rgba(46, 125, 50, 0.55);
}

.center-icon-img {
  width: 56rpx;
  height: 56rpx;
}

.center-label {
  font-size: 20rpx;
  color: #aaaaaa;
  margin-top: 2rpx;
}

.center-label-active {
  color: #4CAF50;
  font-weight: 600;
}

.tab-item-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.item-icon-img {
  width: 48rpx;
  height: 48rpx;
  margin-bottom: 6rpx;
}

.item-text {
  font-size: 22rpx;
  color: #aaaaaa;
}

.text-active {
  color: #4CAF50;
  font-weight: 600;
}
</style>
