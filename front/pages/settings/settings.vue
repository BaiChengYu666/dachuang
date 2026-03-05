<template>
  <view class="container">

    <!-- 用户信息卡片（横向布局） -->
    <view class="user-card">
      <view class="user-card-left" @click="changeAvatar">
        <view class="avatar-wrap">
          <image class="user-avatar" :src="userInfo.avatar" mode="aspectFill"></image>
          <view class="camera-badge">
            <text class="camera-icon">📷</text>
          </view>
        </view>
      </view>
      <view class="user-card-right">
        <text class="user-name">{{ userInfo.name }}</text>
        <text class="user-phone">{{ userInfo.phone }}</text>
        <text class="user-meta" v-if="userInfo.age">{{ userInfo.age }}岁 · {{ userInfo.gender }}</text>
        <text class="user-meta" v-else>{{ userInfo.gender }}</text>
      </view>
      <view class="edit-btn" @click="editProfile">
        <text class="edit-icon">✏️</text>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-grid">
      <view class="quick-btn" @click="goToPage('assessment')">
        <view class="quick-icon-wrap" style="background:#EDE7F6;">
          <text class="quick-icon">🔬</text>
        </view>
        <text class="quick-label">AI评估</text>
      </view>
      <view class="quick-btn" @click="goToPage('forecast')">
        <view class="quick-icon-wrap" style="background:#E8F5E9;">
          <text class="quick-icon">📈</text>
        </view>
        <text class="quick-label">趋势预测</text>
      </view>
      <view class="quick-btn" @click="goToPage('devices')">
        <view class="quick-icon-wrap" style="background:#E3F2FD;">
          <text class="quick-icon">📱</text>
        </view>
        <text class="quick-label">我的设备</text>
      </view>
      <view class="quick-btn" @click="goToPage('guardians')">
        <view class="quick-icon-wrap" style="background:#FCE4EC;">
          <text class="quick-icon">👥</text>
        </view>
        <text class="quick-label">联系人</text>
      </view>
    </view>

    <!-- 健康提醒 -->
    <view class="group">
      <view class="group-title">
        <text class="group-icon">🔔</text>
        <text class="group-label">健康提醒</text>
      </view>
      <view class="menu-list">
        <view class="menu-item" v-for="(item, key) in reminderConfig" :key="key">
          <view class="menu-left">
            <view class="mi-icon-wrap" :style="{background: item.bg}"><text>{{ item.icon }}</text></view>
            <text class="menu-label">{{ item.label }}</text>
          </view>
          <view class="menu-right">
            <switch :checked="reminders[key]" @change="toggleReminder(key)" color="#4CAF50"/>
          </view>
        </view>
      </view>
    </view>

    <!-- 数据与隐私 -->
    <view class="group">
      <view class="group-title">
        <text class="group-icon">🔒</text>
        <text class="group-label">数据与隐私</text>
      </view>
      <view class="menu-list">
        <view class="menu-item" @click="exportData">
          <view class="menu-left">
            <view class="mi-icon-wrap" style="background:#E8EAF6;"><text>📄</text></view>
            <text class="menu-label">导出健康报告</text>
          </view>
          <view class="menu-right"><text class="menu-arrow">›</text></view>
        </view>
        <view class="menu-item" @click="goToPage('privacy')">
          <view class="menu-left">
            <view class="mi-icon-wrap" style="background:#F3E5F5;"><text>🛡️</text></view>
            <text class="menu-label">隐私设置</text>
          </view>
          <view class="menu-right"><text class="menu-arrow">›</text></view>
        </view>
      </view>
    </view>

    <!-- 系统设置 -->
    <view class="group">
      <view class="group-title">
        <text class="group-icon">⚙️</text>
        <text class="group-label">系统设置</text>
      </view>
      <view class="menu-list">
        <view class="menu-item">
          <view class="menu-left">
            <view class="mi-icon-wrap" style="background:#E0F2F1;"><text>🌙</text></view>
            <text class="menu-label">深色模式</text>
          </view>
          <view class="menu-right">
            <switch :checked="darkMode" @change="toggleDarkMode" color="#4CAF50"/>
          </view>
        </view>
        <view class="menu-item" @click="clearCache">
          <view class="menu-left">
            <view class="mi-icon-wrap" style="background:#FFF8E1;"><text>🗑️</text></view>
            <text class="menu-label">清除缓存</text>
          </view>
          <view class="menu-right">
            <text class="menu-val">{{ cacheSize }}</text>
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 关于 -->
    <view class="group">
      <view class="group-title">
        <text class="group-icon">ℹ️</text>
        <text class="group-label">关于</text>
      </view>
      <view class="menu-list">
        <view class="menu-item" @click="checkUpdate">
          <view class="menu-left">
            <view class="mi-icon-wrap" style="background:#E8F5E9;"><text>🔄</text></view>
            <text class="menu-label">检查更新</text>
          </view>
          <view class="menu-right">
            <text class="menu-val">v{{ version }}</text>
            <text class="menu-arrow">›</text>
          </view>
        </view>
        <view class="menu-item" @click="goToPage('help')">
          <view class="menu-left">
            <view class="mi-icon-wrap" style="background:#E3F2FD;"><text>❓</text></view>
            <text class="menu-label">帮助与反馈</text>
          </view>
          <view class="menu-right"><text class="menu-arrow">›</text></view>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-wrap">
      <view class="logout-btn" @click="logout">
        <text class="logout-icon">🚪</text>
        <text class="logout-text">退出登录</text>
      </view>
    </view>

    <view class="bottom-space"></view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      userInfo: {
        avatar: '/static/default-avatar.webp',
        phone: '',
        gender: '',
        name: '用户',
        age: null
      },
      deviceCount: 3,
      guardianCount: 2,
      mainGuardian: '张明（儿子）',
      reminders: {
        medicine: true,
        water: true,
        exercise: false,
        sleep: true
      },
      reminderConfig: {
        medicine: { icon: '💊', label: '用药提醒', bg: '#FFEBEE' },
        water:    { icon: '💧', label: '饮水提醒', bg: '#E3F2FD' },
        exercise: { icon: '🏃', label: '运动提醒', bg: '#E8F5E9' },
        sleep:    { icon: '😴', label: '睡眠提醒', bg: '#EDE7F6' }
      },
      darkMode: false,
      cacheSize: '12.5MB',
      version: '1.0.0'
    }
  },

  onLoad() { this.loadUserInfo() },
  onShow() {
    this.loadUserInfo()
    this.syncTabBar()
    this.loadCounts()
  },

  methods: {
    syncTabBar() {
      const pages = getCurrentPages()
      const page = pages[pages.length - 1]
      if (page && page.getTabBar) {
        page.getTabBar()?.setSelected?.(4)
      }
    },

    loadUserInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) {
          this.userInfo.phone  = userInfo.phone || ''
          this.userInfo.gender = userInfo.gender === 'male' ? '男' : '女'
          this.userInfo.name   = userInfo.nickname || userInfo.phone || '用户'
          this.userInfo.age    = userInfo.age || null
          const av = userInfo.avatarUrl
          this.userInfo.avatar = (av && av !== '/static/logo.png') ? av : '/static/default-avatar.webp'
        }
      } catch (e) {}
    },

    async changeAvatar() {
      try {
        const chooseRes = await uni.chooseImage({ count: 1, sizeType: ['compressed'], sourceType: ['album', 'camera'] })
        const tempFilePath = chooseRes[1].tempFilePaths[0]
        uni.showLoading({ title: '上传中...' })
        const uploadRes = await uni.uploadFile({
          url: 'http://localhost:8080/api/user/' + this.userInfo.phone + '/avatar',
          filePath: tempFilePath,
          name: 'file'
        })
        uni.hideLoading()
        const data = JSON.parse(uploadRes[1].data)
        if (data.code === 200) {
          this.userInfo.avatar = data.data
          const userInfo = uni.getStorageSync('userInfo')
          userInfo.avatarUrl = data.data
          uni.setStorageSync('userInfo', userInfo)
          uni.showToast({ title: '头像更新成功', icon: 'success' })
        } else {
          uni.showToast({ title: data.message || '上传失败', icon: 'none' })
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '上传失败，请重试', icon: 'none' })
      }
    },

    editProfile() {
      uni.showActionSheet({
        itemList: ['修改昵称', '修改年龄', '修改性别', '更换头像'],
        success: (res) => {
          const fns = [this.editNickname, this.editAge, this.changeGender, this.changeAvatar]
          fns[res.tapIndex]?.()
        }
      })
    },

    editNickname() {
      uni.showModal({
        title: '修改昵称', editable: true, placeholderText: '请输入昵称',
        content: this.userInfo.name,
        success: async (res) => {
          if (res.confirm && res.content?.trim()) {
            const newName = res.content.trim()
            try {
              const result = await uni.request({
                url: `http://localhost:8080/api/user/${this.userInfo.phone}/profile`,
                method: 'PUT',
                header: { 'Content-Type': 'application/json' },
                data: { nickname: newName }
              })
              if (result.statusCode === 200 && result.data.code === 200) {
                this.userInfo.name = newName
                const userInfo = uni.getStorageSync('userInfo')
                userInfo.nickname = newName
                uni.setStorageSync('userInfo', userInfo)
                uni.showToast({ title: '昵称修改成功', icon: 'success' })
              } else {
                uni.showToast({ title: result.data.message || '修改失败', icon: 'none' })
              }
            } catch (e) {
              uni.showToast({ title: '网络错误', icon: 'none' })
            }
          }
        }
      })
    },

    editAge() {
      uni.showModal({
        title: '修改年龄', editable: true, placeholderText: '请输入年龄',
        success: async (res) => {
          if (res.confirm && res.content) {
            const age = parseInt(res.content)
            if (!isNaN(age) && age > 0 && age < 150) {
              try {
                const result = await uni.request({
                  url: `http://localhost:8080/api/user/${this.userInfo.phone}/profile`,
                  method: 'PUT',
                  header: { 'Content-Type': 'application/json' },
                  data: { age }
                })
                if (result.statusCode === 200 && result.data.code === 200) {
                  this.userInfo.age = age
                  const userInfo = uni.getStorageSync('userInfo')
                  userInfo.age = age
                  uni.setStorageSync('userInfo', userInfo)
                  uni.showToast({ title: '年龄修改成功', icon: 'success' })
                } else {
                  uni.showToast({ title: result.data.message || '修改失败', icon: 'none' })
                }
              } catch (e) {
                uni.showToast({ title: '网络错误', icon: 'none' })
              }
            } else {
              uni.showToast({ title: '请输入有效年龄', icon: 'none' })
            }
          }
        }
      })
    },

    changeGender() {
      const cur = this.userInfo.gender
      const next = cur === '男' ? '女' : '男'
      uni.showModal({
        title: '修改性别',
        content: `将性别从「${cur}」修改为「${next}」？`,
        success: async (res) => {
          if (res.confirm) {
            const genderVal = next === '男' ? 'male' : 'female'
            try {
              const result = await uni.request({
                url: `http://localhost:8080/api/user/${this.userInfo.phone}/gender`,
                method: 'PUT',
                header: { 'Content-Type': 'application/json' },
                data: { gender: genderVal }
              })
              if (result.statusCode === 200 && result.data.code === 200) {
                this.userInfo.gender = next
                const userInfo = uni.getStorageSync('userInfo')
                userInfo.gender = genderVal
                uni.setStorageSync('userInfo', userInfo)
                uni.showToast({ title: '性别修改成功', icon: 'success' })
              } else {
                uni.showToast({ title: result.data.message || '修改失败', icon: 'none' })
              }
            } catch (e) {
              uni.showToast({ title: '网络错误', icon: 'none' })
            }
          }
        }
      })
    },

    addDevice() {
      uni.navigateTo({ url: '/pages/device/device' })
    },

    editGuardian() {
      uni.navigateTo({ url: '/pages/guardian/guardian' })
    },

    toggleReminder(key) {
      this.reminders[key] = !this.reminders[key]
      uni.showToast({
        title: `${this.reminderConfig[key].label}已${this.reminders[key] ? '开启' : '关闭'}`,
        icon: 'success'
      })
    },

    exportData() {
      uni.navigateTo({ url: '/pages/report/report' })
    },

    toggleDarkMode() {
      this.darkMode = !this.darkMode
      uni.showToast({ title: `深色模式已${this.darkMode ? '开启' : '关闭'}`, icon: 'success' })
    },

    clearCache() {
      uni.showModal({
        title: '清除缓存', content: `确定要清除 ${this.cacheSize} 的缓存吗？`,
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '清除中...' })
            setTimeout(() => { uni.hideLoading(); this.cacheSize = '0MB'; uni.showToast({ title: '缓存已清除', icon: 'success' }) }, 1000)
          }
        }
      })
    },

    checkUpdate() {
      uni.showLoading({ title: '检查中...' })
      setTimeout(() => {
        uni.hideLoading()
        uni.showModal({ title: '当前已是最新版本', content: `版本：v${this.version}`, showCancel: false })
      }, 1000)
    },

    logout() {
      uni.showModal({
        title: '退出登录', content: '确定要退出当前账号吗？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('isLogin')
            uni.removeStorageSync('userInfo')
            uni.showToast({ title: '已退出登录', icon: 'success' })
            setTimeout(() => { uni.reLaunch({ url: '/pages/login/login' }) }, 1500)
          }
        }
      })
    },

    async loadCounts() {
      const phone = this.userInfo.phone
      if (!phone) return
      try {
        const [dRes, gRes] = await Promise.all([
          uni.request({ url: `http://localhost:8080/api/device/${phone}`, method: 'GET' }),
          uni.request({ url: `http://localhost:8080/api/guardian/${phone}`, method: 'GET' })
        ])
        if (dRes.statusCode === 200 && dRes.data.code === 200) {
          this.deviceCount = (dRes.data.data || []).length
        }
        if (gRes.statusCode === 200 && gRes.data.code === 200) {
          const contacts = gRes.data.data || []
          this.guardianCount = contacts.length
          const primary = contacts.find(c => c.isPrimary)
          this.mainGuardian = primary ? `${primary.name}（${primary.relationship || '联系人'}）` : (contacts[0] ? contacts[0].name : '未设置')
        }
      } catch (e) {}
    },

    goToPage(page) {
      const map = {
        devices: '/pages/device/device',
        guardians: '/pages/guardian/guardian',
        assessment: '/pages/assessment/assessment',
        forecast: '/pages/forecast/forecast'
      }
      if (map[page]) {
        uni.navigateTo({ url: map[page] })
      } else {
        uni.showToast({ title: '功能开发中', icon: 'none' })
      }
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #F1F5F1;
  padding: 20rpx 0 20rpx;
  box-sizing: border-box;
}

/* ===== 用户卡片（横向长条） ===== */
.user-card {
  display: flex;
  align-items: center;
  background: #ffffff;
  margin: 0 20rpx 0;
  padding: 28rpx 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 18rpx rgba(0,0,0,0.08);
}

.user-card-left {
  margin-right: 24rpx;
  flex-shrink: 0;
}

.avatar-wrap { position: relative; }

.user-avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  border: 4rpx solid #E8F5E9;
  display: block;
}

.camera-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}

.camera-icon { font-size: 22rpx; }

.user-card-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.user-name {
  display: block;
  font-size: 34rpx;
  font-weight: bold;
  color: #2E3A2E;
  margin-bottom: 6rpx;
}

.user-phone {
  display: block;
  font-size: 24rpx;
  color: #9E9E9E;
  margin-bottom: 4rpx;
}

.user-meta {
  display: block;
  font-size: 22rpx;
  color: #BDBDBD;
}

.edit-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.edit-icon { font-size: 28rpx; }

/* ===== 快捷入口 ===== */
.quick-grid {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 20rpx;
  margin: 16rpx 20rpx 20rpx;
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 18rpx rgba(0,0,0,0.06);
}

.quick-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.quick-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.quick-icon { font-size: 44rpx; }

.quick-label {
  font-size: 24rpx;
  color: #616161;
  font-weight: 500;
}

/* ===== 分组 ===== */
.group { margin: 0 20rpx 20rpx; }

.group-title {
  display: flex;
  align-items: center;
  padding: 0 10rpx 12rpx;
}

.group-icon { font-size: 28rpx; margin-right: 10rpx; }

.group-label {
  font-size: 26rpx;
  font-weight: bold;
  color: #616161;
}

/* ===== 菜单列表 ===== */
.menu-list {
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 18rpx rgba(0,0,0,0.06);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 24rpx;
  border-bottom: 1rpx solid #F5F5F5;
}

.menu-item:last-child { border-bottom: none; }

.menu-left { display: flex; align-items: center; flex: 1; }

.mi-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  font-size: 32rpx;
}

.menu-label { font-size: 28rpx; color: #2E3A2E; }

.menu-right { display: flex; align-items: center; }

.menu-val { font-size: 26rpx; color: #9E9E9E; margin-right: 8rpx; }

.menu-arrow { font-size: 36rpx; color: #BDBDBD; }

/* ===== 退出按钮 ===== */
.logout-wrap {
  padding: 0 20rpx 20rpx;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 18rpx rgba(0,0,0,0.06);
  border: 2rpx solid #FFCDD2;
}

.logout-icon { font-size: 32rpx; margin-right: 14rpx; }

.logout-text {
  font-size: 30rpx;
  font-weight: bold;
  color: #E53935;
}

.bottom-space { height: 0; }
</style>
