<template>
  <view class="ai-container">

    <!-- 聊天消息区域 -->
    <scroll-view
      class="chat-scroll"
      scroll-y
      scroll-with-animation
      :scroll-top="scrollTop">

      <!-- 欢迎卡片 -->
      <view v-if="messages.length === 0" class="welcome-card">
        <text class="welcome-title">您好！我是您的 AI 健康顾问 👋</text>

        <!-- AI Transformer 预测入口（醒目卡片） -->
        <view class="predict-card" @click="goToAssessment">
          <view class="predict-left">
            <view class="predict-icon-wrap">
              <text class="predict-icon">🔬</text>
            </view>
            <view class="predict-info">
              <text class="predict-title">AI 健康评估</text>
              <text class="predict-desc">Transformer 模型 · 输入指标即时预测风险</text>
            </view>
          </view>
          <view class="predict-arrow-wrap">
            <text class="predict-arrow">›</text>
          </view>
        </view>

        <text class="welcome-desc">也可以直接向我提问，例如：</text>
        <view class="suggest-list">
          <view
            class="suggest-item"
            v-for="(q, i) in suggestQuestions"
            :key="i"
            @click="sendSuggestQuestion(q)">
            <text class="suggest-icon">💬</text>
            <text class="suggest-text">{{ q }}</text>
            <text class="suggest-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 消息列表 -->
      <view class="message-list">
        <view
          v-for="(msg, index) in messages"
          :key="index"
          class="message-item"
          :class="msg.role === 'ai' ? 'ai-side' : 'user-side'">

          <!-- AI 头像 -->
          <image v-if="msg.role === 'ai'" class="msg-avatar ai-logo-img" src="/static/iage-logo.png" mode="aspectFill"/>

          <!-- 气泡 -->
          <view class="bubble" :class="msg.role === 'ai' ? 'ai-bubble' : 'user-bubble'">
            <text class="bubble-text">{{ msg.content }}</text>
            <text class="bubble-time">{{ msg.time }}</text>
          </view>

          <!-- 用户头像 -->
          <image
            v-if="msg.role === 'user'"
            class="msg-avatar user-avatar-img"
            :src="userAvatar"
            mode="aspectFill">
          </image>
        </view>

        <!-- 加载动画 -->
        <view v-if="loading" class="typing-row">
          <image class="msg-avatar ai-logo-img" src="/static/iage-logo.png" mode="aspectFill"/>
          <view class="typing-bubble">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
      </view>

    </scroll-view>

    <!-- 底部输入区 -->
    <view class="input-bar">
      <view class="input-row">
        <view class="input-wrap">
          <textarea
            class="input-area"
            v-model="inputContent"
            placeholder="向 i龄守护 提问..."
            placeholder-style="color:#C0C0C0;font-size:28rpx;"
            maxlength="500"
            :style="{height: inputHeight + 'rpx'}"
            @input="autoResize"
            @confirm="sendMessage">
          </textarea>
          <text v-if="inputContent.length > 400" class="char-badge">{{ inputContent.length }}/500</text>
        </view>
        <view
          class="send-btn"
          :class="inputContent.trim() && !loading ? 'send-active' : 'send-disabled'"
          @click="sendMessage">
          <text class="send-icon">↑</text>
        </view>
      </view>
    </view>

  </view>
</template>

<script>
export default {
  data() {
    return {
      messages: [],
      inputContent: '',
      loading: false,
      scrollTop: 0,
      inputHeight: 80,
      userAvatar: '/static/default-avatar.webp',
      suggestQuestions: [
        '老年人正常心率范围是多少？',
        '高血压患者饮食需要注意什么？',
        '如何预防老年人跌倒？',
        '血氧偏低有什么症状？'
      ]
    }
  },

  onLoad() {
    try {
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo && userInfo.avatarUrl && userInfo.avatarUrl !== '/static/logo.png') {
        this.userAvatar = userInfo.avatarUrl
      }
    } catch (e) {}
  },

  onShow() {
    this.syncTabBar()
  },

  methods: {
    syncTabBar() {
      const pages = getCurrentPages()
      const page = pages[pages.length - 1]
      if (page && page.getTabBar) {
        page.getTabBar()?.setSelected?.(2)
      }
    },

    autoResize(e) {
      const h = e.detail.height
      this.inputHeight = Math.min(Math.max(h, 80), 200)
    },

    async sendMessage() {
      const content = this.inputContent.trim()
      if (!content || this.loading) return

      this.messages.push({ role: 'user', content, time: this.now() })
      this.inputContent = ''
      this.inputHeight = 80
      this.scrollToBottom()
      this.loading = true

      try {
        await this.getAIResponse(content)
      } catch (e) {
        this.messages.push({
          role: 'ai',
          content: '抱歉，暂时无法回答，请稍后再试。',
          time: this.now()
        })
      } finally {
        this.loading = false
        this.scrollToBottom()
      }
    },

    async getAIResponse(question) {
      try {
        const res = await uni.request({
          url: 'http://localhost:8080/api/ai/chat',
          method: 'POST',
          header: { 'Content-Type': 'application/json' },
          data: { message: question }
        })
        if (res.statusCode === 200 && res.data.code === 200) {
          this.messages.push({ role: 'ai', content: res.data.data.reply, time: this.now() })
        } else {
          this.messages.push({ role: 'ai', content: '抱歉，服务暂时不可用，请稍后再试。', time: this.now() })
        }
      } catch (e) {
        this.messages.push({ role: 'ai', content: '网络连接失败，请检查网络后重试。', time: this.now() })
      }
    },

    sendSuggestQuestion(q) {
      this.inputContent = q
      this.sendMessage()
    },

    scrollToBottom() {
      setTimeout(() => { this.scrollTop = 999999 }, 100)
    },

    now() {
      const d = new Date()
      return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
    },

    goToAssessment() {
      uni.navigateTo({ url: '/pages/assessment/assessment' })
    }
  }
}
</script>

<style scoped>
/* 锁死页面本身不可滚动 */
page {
  height: 100%;
  overflow: hidden;
}

.ai-container {
  height: 100%;
  background: #F1F5F1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

/* ===== 聊天区 ===== */
.chat-scroll {
  flex: 1;
  height: 0;
  overflow: hidden;
  padding: 16rpx 20rpx 0;
  margin-bottom: 8rpx;
  box-sizing: border-box;
}

/* ===== 欢迎卡片 ===== */
.welcome-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 36rpx 28rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
  margin-bottom: 20rpx;
}

.welcome-title {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #2E3A2E;
  margin-bottom: 20rpx;
}

/* ===== AI 预测入口卡片 ===== */
.predict-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #E8F5E9, #C8E6C9);
  border-radius: 22rpx;
  padding: 22rpx 20rpx;
  margin-bottom: 22rpx;
  border: 2rpx solid #A5D6A7;
  box-shadow: 0 4rpx 14rpx rgba(76,175,80,0.15);
}
.predict-card:active { opacity: 0.85; }

.predict-left { display: flex; align-items: center; flex: 1; }

.predict-icon-wrap {
  width: 80rpx; height: 80rpx; border-radius: 22rpx;
  background: linear-gradient(135deg, #66BB6A, #2E7D32);
  display: flex; align-items: center; justify-content: center;
  margin-right: 20rpx; flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(46,125,50,0.3);
}
.predict-icon { font-size: 40rpx; }

.predict-info { flex: 1; }
.predict-title {
  display: block;
  font-size: 30rpx; font-weight: bold; color: #1B5E20;
  margin-bottom: 6rpx;
}
.predict-desc {
  display: block;
  font-size: 22rpx; color: #388E3C; line-height: 1.5;
}

.predict-arrow-wrap {
  width: 52rpx; height: 52rpx; border-radius: 50%;
  background: #2E7D32;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.predict-arrow { font-size: 36rpx; color: #fff; font-weight: bold; }

.welcome-desc {
  display: block;
  font-size: 26rpx;
  color: #9E9E9E;
  margin-bottom: 16rpx;
}

.suggest-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.suggest-item {
  display: flex;
  align-items: center;
  padding: 18rpx 20rpx;
  background: #F1F8F1;
  border-radius: 18rpx;
  border: 1rpx solid #E8F5E9;
}

.suggest-icon { font-size: 28rpx; margin-right: 14rpx; }

.suggest-text {
  flex: 1;
  font-size: 26rpx;
  color: #388E3C;
}

.suggest-arrow {
  font-size: 30rpx;
  color: #A5D6A7;
}

/* ===== 消息列表 ===== */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding-bottom: 20rpx;
}

.message-item {
  display: flex;
  align-items: flex-end;
}

.ai-side  { flex-direction: row; }
.user-side { flex-direction: row; justify-content: flex-end; }

.msg-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-logo-img {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #fff;
  border: 2rpx solid #C8E6C9;
  margin-right: 14rpx;
  flex-shrink: 0;
  overflow: hidden;
}

.user-avatar-img {
  background: #E0E0E0;
  margin-left: 14rpx;
  flex-shrink: 0;
}

.bubble {
  max-width: 72%;
  border-radius: 24rpx;
  padding: 18rpx 22rpx;
  display: flex;
  flex-direction: column;
}

.ai-bubble {
  background: #ffffff;
  border-top-left-radius: 6rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.07);
}

.user-bubble {
  background: #ffffff;
  border-top-right-radius: 6rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.07);
}

.bubble-text {
  font-size: 28rpx;
  line-height: 1.65;
  display: block;
  margin-bottom: 8rpx;
}

.ai-bubble .bubble-text { color: #2E3A2E; }
.user-bubble .bubble-text { color: #2E3A2E; }

.bubble-time {
  font-size: 20rpx;
  color: #BDBDBD;
  display: block;
  text-align: right;
}

.user-bubble .bubble-time { color: #BDBDBD; }

/* ===== 打字动画 ===== */
.typing-row {
  display: flex;
  align-items: flex-end;
}

.typing-bubble {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: #ffffff;
  border-radius: 24rpx;
  border-top-left-radius: 6rpx;
  padding: 18rpx 24rpx;
  margin-left: 14rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.07);
}

.dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #A5D6A7;
  animation: bounce 1.2s infinite;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-10rpx); }
}

/* ===== 底部输入框 ===== */
.input-bar {
  background: #ffffff;
  padding: 14rpx 20rpx 28rpx;
  box-shadow: 0 -2rpx 18rpx rgba(0,0,0,0.07);
  flex-shrink: 0;
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
}

.input-wrap {
  flex: 1;
  background: #F4F6F4;
  border-radius: 28rpx;
  padding: 14rpx 20rpx;
  border: 1.5rpx solid transparent;
  transition: border-color 0.2s, background 0.2s;
  position: relative;
  box-sizing: border-box;
}

.input-wrap:focus-within {
  border-color: #A5D6A7;
  background: #FAFFFE;
}

.input-area {
  width: 100%;
  min-height: 72rpx;
  max-height: 200rpx;
  font-size: 28rpx;
  color: #2E3A2E;
  background: transparent;
  line-height: 1.6;
}

.char-badge {
  position: absolute;
  bottom: 8rpx;
  right: 16rpx;
  font-size: 18rpx;
  color: #FF9800;
}

.send-btn {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.25s;
}

.send-active {
  background: linear-gradient(145deg, #66BB6A, #43A047);
  box-shadow: 0 6rpx 20rpx rgba(76,175,80,0.45);
}

.send-disabled {
  background: #EBEBEB;
}

.send-icon {
  font-size: 38rpx;
  color: #ffffff;
  font-weight: bold;
  line-height: 1;
}

.send-disabled .send-icon {
  color: #C0C0C0;
}

.bottom-space { height: 20rpx; }
</style>
