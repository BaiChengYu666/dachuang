<template>
  <view class="container">

    <!-- 顶部信息栏 -->
    <view class="page-topbar">
      <view class="topbar-left">
        <view class="topbar-icon-wrap">
          <text class="topbar-icon">📱</text>
        </view>
        <text class="topbar-sub">已绑定 {{ devices.length }} 台设备</text>
      </view>
      <view class="topbar-add-btn" @click="showAddDialog">
        <text class="topbar-add-icon">+</text>
        <text class="topbar-add-text">添加</text>
      </view>
    </view>

    <!-- 设备列表 -->
    <view v-if="devices.length === 0" class="empty-box">
      <text class="empty-icon">📭</text>
      <text class="empty-text">暂无绑定设备</text>
      <view class="empty-btn" @click="showAddDialog">
        <text>+ 添加设备</text>
      </view>
    </view>

    <view class="device-list" v-else>
      <view class="device-card" v-for="(dev, index) in devices" :key="dev.id">
        <view class="device-icon-wrap" :style="{ background: typeColor(dev.deviceType).bg }">
          <text class="device-icon">{{ typeColor(dev.deviceType).icon }}</text>
        </view>
        <view class="device-info">
          <text class="device-name">{{ dev.deviceName }}</text>
          <text class="device-type">{{ dev.deviceType || '未知类型' }}</text>
          <text class="device-sn" v-if="dev.deviceSn">SN: {{ dev.deviceSn }}</text>
        </view>
        <view class="device-right">
          <view class="status-dot-wrap">
            <view class="status-dot" :class="dev.status"></view>
            <text class="status-text">{{ statusText(dev.status) }}</text>
          </view>
          <view class="device-actions">
            <view class="action-btn edit-btn" @click="editDevice(dev)">
              <text>编辑</text>
            </view>
            <view class="action-btn del-btn" @click="deleteDevice(dev.id, index)">
              <text>删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 常见设备类型提示 -->
    <view class="tips-card">
      <text class="tips-title">支持的设备类型</text>
      <view class="device-type-grid">
        <view class="dtype-item" v-for="t in deviceTypes" :key="t.name" @click="quickAdd(t)">
          <view class="dtype-icon-wrap" :style="{ background: t.bg }">
            <text class="dtype-icon">{{ t.icon }}</text>
          </view>
          <text class="dtype-name">{{ t.name }}</text>
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
      devices: [],
      userPhone: '',
      deviceTypes: [
        { name: '智能手环', icon: '⌚', bg: '#E3F2FD', color: '#1E88E5' },
        { name: '血压计',   icon: '🩸', bg: '#FCE4EC', color: '#E91E63' },
        { name: '血氧仪',   icon: '💧', bg: '#E8F5E9', color: '#43A047' },
        { name: '体温计',   icon: '🌡️', bg: '#FFF3E0', color: '#FB8C00' },
        { name: '心率仪',   icon: '❤️', bg: '#FFEBEE', color: '#E53935' },
        { name: '其他',     icon: '📟', bg: '#F3E5F5', color: '#8E24AA' }
      ]
    }
  },

  onLoad() {
    try {
      const info = uni.getStorageSync('userInfo')
      this.userPhone = info ? info.phone : ''
    } catch (e) {}
    this.loadDevices()
  },

  methods: {
    async loadDevices() {
      if (!this.userPhone) return
      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/device/${this.userPhone}`,
          method: 'GET'
        })
        if (res.statusCode === 200 && res.data.code === 200) {
          this.devices = res.data.data || []
        }
      } catch (e) {}
    },

    typeColor(type) {
      const map = {
        '智能手环': { icon: '⌚', bg: '#E3F2FD' },
        '血压计':   { icon: '🩸', bg: '#FCE4EC' },
        '血氧仪':   { icon: '💧', bg: '#E8F5E9' },
        '体温计':   { icon: '🌡️', bg: '#FFF3E0' },
        '心率仪':   { icon: '❤️', bg: '#FFEBEE' },
        '其他':     { icon: '📟', bg: '#F3E5F5' }
      }
      return map[type] || { icon: '📱', bg: '#ECEFF1' }
    },

    statusText(s) {
      return { online: '在线', offline: '离线', binding: '配对中' }[s] || s
    },

    showAddDialog() {
      uni.showActionSheet({
        itemList: this.deviceTypes.map(t => t.name),
        success: (res) => {
          this.quickAdd(this.deviceTypes[res.tapIndex])
        }
      })
    },

    quickAdd(typeObj) {
      uni.showModal({
        title: `添加${typeObj.name}`,
        editable: true,
        placeholderText: '请输入设备名称',
        content: typeObj.name,
        success: async (res) => {
          if (!res.confirm || !res.content?.trim()) return
          try {
            uni.showLoading({ title: '添加中...' })
            const result = await uni.request({
              url: 'http://localhost:8080/api/device',
              method: 'POST',
              header: { 'Content-Type': 'application/json' },
              data: {
                userPhone: this.userPhone,
                deviceName: res.content.trim(),
                deviceType: typeObj.name,
                status: 'binding'
              }
            })
            uni.hideLoading()
            if (result.statusCode === 200 && result.data.code === 200) {
              this.devices.push(result.data.data)
              uni.showToast({ title: '设备添加成功', icon: 'success' })
            } else {
              uni.showToast({ title: result.data.message || '添加失败', icon: 'none' })
            }
          } catch (e) {
            uni.hideLoading()
            uni.showToast({ title: '网络错误', icon: 'none' })
          }
        }
      })
    },

    editDevice(dev) {
      uni.showModal({
        title: '编辑设备名称',
        editable: true,
        content: dev.deviceName,
        success: async (res) => {
          if (!res.confirm || !res.content?.trim()) return
          try {
            const result = await uni.request({
              url: `http://localhost:8080/api/device/${dev.id}`,
              method: 'PUT',
              header: { 'Content-Type': 'application/json' },
              data: { deviceName: res.content.trim() }
            })
            if (result.statusCode === 200 && result.data.code === 200) {
              dev.deviceName = result.data.data.deviceName
              uni.showToast({ title: '更新成功', icon: 'success' })
            }
          } catch (e) {}
        }
      })
    },

    deleteDevice(id, index) {
      uni.showModal({
        title: '删除设备', content: '确定要删除该设备吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            const result = await uni.request({
              url: `http://localhost:8080/api/device/${id}?userPhone=${this.userPhone}`,
              method: 'DELETE'
            })
            if (result.statusCode === 200 && result.data.code === 200) {
              this.devices.splice(index, 1)
              uni.showToast({ title: '删除成功', icon: 'success' })
            }
          } catch (e) {}
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

/* ===== 顶部栏 ===== */
.page-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 20rpx 24rpx;
  margin: 0 20rpx 20rpx;
  box-shadow: 0 4rpx 18rpx rgba(0,0,0,0.06);
}

.topbar-left {
  display: flex;
  align-items: center;
}

.topbar-icon-wrap {
  width: 72rpx; height: 72rpx;
  border-radius: 20rpx;
  background: #E3F2FD;
  display: flex; align-items: center; justify-content: center;
  margin-right: 16rpx;
}

.topbar-icon { font-size: 38rpx; }

.topbar-sub { font-size: 26rpx; color: #616161; }

.topbar-add-btn {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #42A5F5, #1565C0);
  border-radius: 30rpx;
  padding: 14rpx 28rpx;
}

.topbar-add-icon { font-size: 30rpx; color: #fff; margin-right: 6rpx; font-weight: 300; }
.topbar-add-text { font-size: 26rpx; color: #fff; font-weight: bold; }

/* ===== 空状态 ===== */
.empty-box {
  display: flex; flex-direction: column; align-items: center;
  padding: 80rpx 0;
}

.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #9E9E9E; margin-bottom: 30rpx; }

.empty-btn {
  background: linear-gradient(135deg, #42A5F5, #1565C0);
  padding: 18rpx 50rpx;
  border-radius: 40rpx;
  font-size: 28rpx; color: #ffffff;
}

/* ===== 设备卡片 ===== */
.device-list { padding: 0 20rpx; }

.device-card {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}

.device-icon-wrap {
  width: 80rpx; height: 80rpx;
  border-radius: 22rpx;
  display: flex; align-items: center; justify-content: center;
  margin-right: 20rpx; flex-shrink: 0;
}

.device-icon { font-size: 40rpx; }

.device-info { flex: 1; }

.device-name {
  display: block;
  font-size: 30rpx; font-weight: bold; color: #2E3A2E;
  margin-bottom: 6rpx;
}

.device-type { display: block; font-size: 24rpx; color: #9E9E9E; margin-bottom: 4rpx; }
.device-sn { display: block; font-size: 20rpx; color: #BDBDBD; }

.device-right { display: flex; flex-direction: column; align-items: flex-end; }

.status-dot-wrap { display: flex; align-items: center; margin-bottom: 12rpx; }

.status-dot {
  width: 14rpx; height: 14rpx; border-radius: 50%;
  margin-right: 6rpx;
}

.status-dot.online  { background: #4CAF50; }
.status-dot.offline { background: #9E9E9E; }
.status-dot.binding { background: #FFA726; }

.status-text { font-size: 22rpx; color: #757575; }

.device-actions { display: flex; gap: 10rpx; }

.action-btn {
  padding: 8rpx 20rpx;
  border-radius: 14rpx;
  font-size: 22rpx;
}

.edit-btn { background: #E3F2FD; color: #1E88E5; }
.del-btn  { background: #FFEBEE; color: #E53935; }

/* ===== 类型提示 ===== */
.tips-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin: 0 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}

.tips-title {
  display: block;
  font-size: 28rpx; font-weight: bold; color: #2E3A2E;
  margin-bottom: 20rpx;
}

.device-type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.dtype-item {
  display: flex; flex-direction: column; align-items: center;
  padding: 20rpx 0;
}

.dtype-icon-wrap {
  width: 80rpx; height: 80rpx;
  border-radius: 22rpx;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 10rpx;
}

.dtype-icon { font-size: 38rpx; }
.dtype-name { font-size: 22rpx; color: #616161; }

.bottom-space { height: 20rpx; }
</style>
