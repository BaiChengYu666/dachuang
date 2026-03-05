<template>
  <view class="login-container">

    <!-- 顶部背景装饰 -->
    <view class="top-bg">
      <view class="circle-lg"></view>
      <view class="circle-sm"></view>
    </view>

    <!-- Logo 区域 -->
    <view class="logo-section">
      <view class="logo-wrap">
        <image class="logo" src="/static/iage-logo.png" mode="aspectFit"></image>
      </view>
      <text class="app-name">i龄守护</text>
      <text class="app-desc">智慧守护，健康相伴</text>
    </view>

    <!-- Tab 切换 -->
    <view class="tab-section">
      <view
        class="tab-item"
        :class="{ active: currentTab === 'login' }"
        @click="switchTab('login')">
        <text>登录</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'register' }"
        @click="switchTab('register')">
        <text>注册</text>
      </view>
    </view>

    <!-- 登录表单 -->
    <view class="form-card" v-if="currentTab === 'login'">
      <view class="form-item">
        <view class="input-icon">📱</view>
        <input
          class="form-input"
          type="number"
          v-model="loginForm.phone"
          placeholder="请输入手机号"
          maxlength="11"
        />
      </view>
      <view class="form-item">
        <view class="input-icon">🔒</view>
        <input
          class="form-input"
          type="password"
          v-model="loginForm.password"
          placeholder="请输入密码"
        />
      </view>
      <!-- 协议勾选 -->
      <view class="agree-row">
        <view class="agree-check" :class="{ checked: agreed }" @click="agreed = !agreed">
          <text v-if="agreed" class="check-icon">✓</text>
        </view>
        <text class="agree-text">我已阅读并同意</text>
        <text class="agree-link" @click.stop="openTerms('agreement')">《用户协议》</text>
        <text class="agree-text">和</text>
        <text class="agree-link" @click.stop="openTerms('privacy')">《隐私政策》</text>
      </view>
      <view class="submit-btn" @click="handleLogin"><text class="submit-btn-text">登 录</text></view>
    </view>

    <!-- 注册表单 -->
    <view class="form-card" v-if="currentTab === 'register'">
      <view class="form-item">
        <view class="input-icon">📱</view>
        <input
          class="form-input"
          type="number"
          v-model="registerForm.phone"
          placeholder="请输入11位手机号"
          maxlength="11"
        />
      </view>
      <view class="form-item">
        <view class="input-icon">🔒</view>
        <input
          class="form-input"
          type="password"
          v-model="registerForm.password"
          placeholder="请输入6-20位密码"
        />
      </view>
      <view class="form-item gender-item">
        <view class="input-icon">👤</view>
        <view class="gender-selector">
          <view
            class="gender-option"
            :class="{ selected: registerForm.gender === 'male' }"
            @click="registerForm.gender = 'male'">
            <text>男</text>
          </view>
          <view
            class="gender-option"
            :class="{ selected: registerForm.gender === 'female' }"
            @click="registerForm.gender = 'female'">
            <text>女</text>
          </view>
        </view>
      </view>
      <!-- 协议勾选 -->
      <view class="agree-row">
        <view class="agree-check" :class="{ checked: agreed }" @click="agreed = !agreed">
          <text v-if="agreed" class="check-icon">✓</text>
        </view>
        <text class="agree-text">我已阅读并同意</text>
        <text class="agree-link" @click.stop="openTerms('agreement')">《用户协议》</text>
        <text class="agree-text">和</text>
        <text class="agree-link" @click.stop="openTerms('privacy')">《隐私政策》</text>
      </view>
      <view class="submit-btn" @click="handleRegister"><text class="submit-btn-text">注 册</text></view>
    </view>

    <!-- 底部版权 -->
    <view class="footer">
      <text class="footer-text">i龄守护 · 智慧守护每一天</text>
    </view>

  </view>
</template>

<script>
export default {
  data() {
    return {
      currentTab: 'login',
      agreed: false,
      loginForm: { phone: '', password: '' },
      registerForm: { phone: '', password: '', gender: 'female' }
    }
  },

  methods: {
    switchTab(tab) {
      this.currentTab = tab
    },

    openTerms(type) {
      uni.navigateTo({ url: `/pages/terms/terms?type=${type}` })
    },

    async handleLogin() {
      const { phone, password } = this.loginForm
      if (!this.agreed) return uni.showToast({ title: '请先阅读并同意用户协议和隐私政策', icon: 'none' })
      if (!phone) return uni.showToast({ title: '请输入手机号', icon: 'none' })
      if (!password) return uni.showToast({ title: '请输入密码', icon: 'none' })
      if (!/^1[0-9]{10}$/.test(phone)) return uni.showToast({ title: '手机号格式不正确', icon: 'none' })

      try {
        uni.showLoading({ title: '登录中...' })
        const res = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:8080/api/auth/login',
            method: 'POST',
            data: { username: phone, password },
            success: resolve,
            fail: reject
          })
        })
        uni.hideLoading()

        if (res.statusCode === 200 && res.data.code === 200) {
          const userInfo = res.data.data
          uni.setStorageSync('isLogin', true)
          uni.setStorageSync('userInfo', {
            phone: userInfo.phone,
            username: userInfo.phone,
            nickname: userInfo.nickname || '',
            age: userInfo.age || null,
            gender: userInfo.gender,
            avatarUrl: userInfo.avatarUrl || '/static/default-avatar.webp'
          })
          uni.showToast({ title: '登录成功', icon: 'success' })
          setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 1200)
        } else {
          uni.showToast({ title: res.data.message || '账号或密码错误', icon: 'none' })
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '无法连接服务器，请检查网络', icon: 'none' })
      }
    },

    async handleRegister() {
      const { phone, password, gender } = this.registerForm
      if (!this.agreed) return uni.showToast({ title: '请先阅读并同意用户协议和隐私政策', icon: 'none' })
      if (!phone) return uni.showToast({ title: '请输入手机号', icon: 'none' })
      if (!/^1[0-9]{10}$/.test(phone)) return uni.showToast({ title: '手机号格式不正确', icon: 'none' })
      if (!password) return uni.showToast({ title: '请输入密码', icon: 'none' })
      if (password.length < 6 || password.length > 20) return uni.showToast({ title: '密码长度为6-20位', icon: 'none' })

      try {
        uni.showLoading({ title: '注册中...' })
        const res = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:8080/api/auth/register',
            method: 'POST',
            data: { phone, password, gender },
            success: resolve,
            fail: reject
          })
        })
        uni.hideLoading()

        if (res.statusCode === 200 && res.data.code === 200) {
          uni.showToast({ title: '注册成功', icon: 'success' })
          setTimeout(() => {
            this.currentTab = 'login'
            this.loginForm.phone = phone
            this.loginForm.password = password
          }, 1500)
        } else {
          uni.showToast({ title: res.data.message || '注册失败', icon: 'none' })
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '无法连接服务器，请检查网络', icon: 'none' })
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: #F5F8F5;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  position: relative;
}

/* ===== 顶部装饰 ===== */
.top-bg {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 520rpx;
  background: linear-gradient(160deg, #43A047 0%, #66BB6A 60%, #A5D6A7 100%);
  border-radius: 0 0 60% 60% / 0 0 80rpx 80rpx;
  overflow: hidden;
}

.circle-lg {
  position: absolute;
  width: 400rpx; height: 400rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
  top: -120rpx; right: -80rpx;
}

.circle-sm {
  position: absolute;
  width: 220rpx; height: 220rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.07);
  bottom: 40rpx; left: -40rpx;
}

/* ===== Logo ===== */
.logo-section {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 120rpx;
  margin-bottom: 50rpx;
}

.logo-wrap {
  width: 180rpx;
  height: 180rpx;
  border-radius: 44rpx;
  background: rgba(255,255,255,0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 40rpx rgba(0,0,0,0.15);
  margin-bottom: 28rpx;
  overflow: hidden;
}

.logo {
  width: 160rpx;
  height: 160rpx;
}

.app-name {
  display: block;
  font-size: 52rpx;
  font-weight: bold;
  color: #ffffff;
  letter-spacing: 4rpx;
  margin-bottom: 12rpx;
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.15);
}

.app-desc {
  display: block;
  font-size: 26rpx;
  color: rgba(255,255,255,0.85);
  letter-spacing: 2rpx;
}

/* ===== Tab ===== */
.tab-section {
  position: relative;
  z-index: 1;
  display: flex;
  background: #E8F5E9;
  border-radius: 50rpx;
  padding: 6rpx;
  margin-bottom: 30rpx;
  width: 320rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  font-size: 30rpx;
  color: #888888;
  border-radius: 44rpx;
  transition: all 0.3s;
}

.tab-item.active {
  background: #43A047;
  color: #ffffff;
  font-weight: bold;
  box-shadow: 0 4rpx 14rpx rgba(67,160,71,0.35);
}

/* ===== 表单卡片 ===== */
.form-card {
  position: relative;
  z-index: 1;
  width: 690rpx;
  background: #ffffff;
  border-radius: 36rpx;
  padding: 50rpx 44rpx 44rpx;
  box-shadow: 0 16rpx 60rpx rgba(0,0,0,0.1);
}

.form-item {
  display: flex;
  align-items: center;
  background: #F7FAF7;
  border-radius: 28rpx;
  padding: 0 24rpx;
  margin-bottom: 24rpx;
  border: 2rpx solid transparent;
  transition: border-color 0.2s;
}

.form-item:focus-within {
  border-color: #66BB6A;
}

.input-icon {
  font-size: 30rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  height: 96rpx;
  font-size: 28rpx;
  color: #2E3A2E;
  background: transparent;
  border: none;
}

/* 性别选择 */
.gender-item { padding: 20rpx 24rpx; }

.gender-selector {
  flex: 1;
  display: flex;
  gap: 16rpx;
}

.gender-option {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  background: #EEEEEE;
  border-radius: 20rpx;
  font-size: 28rpx;
  color: #757575;
  border: 2rpx solid transparent;
  transition: all 0.25s;
}

.gender-option.selected {
  background: #E8F5E9;
  color: #388E3C;
  border-color: #66BB6A;
  font-weight: bold;
}

/* ===== 协议勾选 ===== */
.agree-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  padding: 8rpx 0 28rpx;
}

.agree-check {
  width: 38rpx;
  height: 38rpx;
  border: 2rpx solid #BDBDBD;
  border-radius: 8rpx;
  margin-right: 12rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.agree-check.checked {
  background: #43A047;
  border-color: #43A047;
}

.check-icon {
  color: #ffffff;
  font-size: 24rpx;
  font-weight: bold;
  line-height: 1;
}

.agree-text {
  font-size: 24rpx;
  color: #757575;
}

.agree-link {
  font-size: 24rpx;
  color: #1565C0;
  text-decoration: underline;
}

/* ===== 按钮 ===== */
.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #43A047, #66BB6A);
  border-radius: 48rpx;
  margin-top: 12rpx;
  box-shadow: 0 10rpx 30rpx rgba(76,175,80,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.submit-btn-text {
  color: #ffffff;
  font-size: 32rpx;
  font-weight: bold;
  letter-spacing: 6rpx;
}

/* ===== 底部 ===== */
.footer {
  position: relative;
  z-index: 1;
  margin-top: auto;
  padding: 40rpx 0 60rpx;
}

.footer-text {
  font-size: 22rpx;
  color: #BDBDBD;
  letter-spacing: 1rpx;
}
</style>
