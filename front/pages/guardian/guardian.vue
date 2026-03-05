<template>
  <view class="container">

    <!-- 顶部信息栏 -->
    <view class="page-topbar">
      <view class="topbar-left">
        <view class="topbar-icon-wrap">
          <text class="topbar-icon">👥</text>
        </view>
        <text class="topbar-sub">已添加 {{ contacts.length }} 位联系人</text>
      </view>
      <view class="topbar-add-btn" @click="showAddForm">
        <text class="topbar-add-icon">+</text>
        <text class="topbar-add-text">添加</text>
      </view>
    </view>

    <!-- 联系人列表 -->
    <view v-if="contacts.length === 0" class="empty-box">
      <text class="empty-icon">👨‍👩‍👧</text>
      <text class="empty-text">暂无紧急联系人</text>
      <view class="empty-btn" @click="showAddForm">
        <text>+ 添加联系人</text>
      </view>
    </view>

    <view class="contact-list" v-else>
      <view class="contact-card" v-for="(c, index) in contacts" :key="c.id">
        <view class="contact-avatar" :style="{ background: relationColor(c.relationship) }">
          <text class="contact-avatar-text">{{ c.name ? c.name[0] : '?' }}</text>
        </view>
        <view class="contact-info">
          <view class="contact-name-row">
            <text class="contact-name">{{ c.name }}</text>
            <view v-if="c.isPrimary" class="primary-tag">⭐ 紧急联系人</view>
          </view>
          <text class="contact-rel">{{ c.relationship || '联系人' }}</text>
          <text class="contact-phone">📞 {{ c.contactPhone }}</text>
        </view>
        <view class="contact-actions">
          <view class="action-btn call-btn" @click="callContact(c.contactPhone)">
            <text>拨打</text>
          </view>
          <view class="action-btn primary-set-btn" v-if="!c.isPrimary" @click="setPrimary(c, index)">
            <text>设为紧急</text>
          </view>
          <view class="action-btn edit-btn" @click="editContact(c)">
            <text>编辑</text>
          </view>
          <view class="action-btn del-btn" @click="deleteContact(c.id, index)">
            <text>删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 添加表单弹窗 -->
    <view v-if="showForm" class="form-mask" @click.self="showForm = false">
      <view class="form-sheet">
        <text class="form-title">{{ editingId ? '编辑联系人' : '添加联系人' }}</text>

        <view class="form-item">
          <text class="form-label">姓名</text>
          <input class="form-input" v-model="form.name" placeholder="请输入姓名" />
        </view>
        <view class="form-item">
          <text class="form-label">手机号</text>
          <input class="form-input" v-model="form.contactPhone" type="number" placeholder="请输入手机号" maxlength="11" />
        </view>
        <view class="form-item">
          <text class="form-label">关系</text>
          <view class="rel-tabs">
            <view
              class="rel-tab"
              :class="form.relationship === r ? 'rel-active' : ''"
              v-for="r in relOptions" :key="r"
              @click="form.relationship = r">{{ r }}</view>
          </view>
        </view>
        <view class="form-item form-switch-row">
          <text class="form-label">设为主要联系人</text>
          <switch :checked="form.isPrimary" @change="form.isPrimary = !form.isPrimary" color="#4CAF50" />
        </view>

        <view class="form-btns">
          <view class="form-cancel" @click="showForm = false">取消</view>
          <view class="form-confirm" @click="submitForm">{{ editingId ? '保存' : '添加' }}</view>
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
      contacts: [],
      userPhone: '',
      showForm: false,
      editingId: null,
      form: { name: '', contactPhone: '', relationship: '子女', isPrimary: false },
      relOptions: ['子女', '配偶', '父母', '兄弟姐妹', '朋友', '其他']
    }
  },

  onLoad() {
    try {
      const info = uni.getStorageSync('userInfo')
      this.userPhone = info ? info.phone : ''
    } catch (e) {}
    this.loadContacts()
  },

  methods: {
    async loadContacts() {
      if (!this.userPhone) return
      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/guardian/${this.userPhone}`,
          method: 'GET'
        })
        if (res.statusCode === 200 && res.data.code === 200) {
          this.contacts = res.data.data || []
        }
      } catch (e) {}
    },

    relationColor(rel) {
      const map = {
        '子女': '#E8F5E9', '配偶': '#FCE4EC', '父母': '#E3F2FD',
        '兄弟姐妹': '#FFF3E0', '朋友': '#F3E5F5', '其他': '#ECEFF1'
      }
      return map[rel] || '#ECEFF1'
    },

    showAddForm() {
      this.editingId = null
      this.form = { name: '', contactPhone: '', relationship: '子女', isPrimary: false }
      this.showForm = true
    },

    editContact(c) {
      this.editingId = c.id
      this.form = {
        name: c.name,
        contactPhone: c.contactPhone,
        relationship: c.relationship || '子女',
        isPrimary: c.isPrimary || false
      }
      this.showForm = true
    },

    async submitForm() {
      if (!this.form.name.trim()) {
        uni.showToast({ title: '请输入姓名', icon: 'none' }); return
      }
      if (!/^\d{11}$/.test(this.form.contactPhone)) {
        uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return
      }
      try {
        uni.showLoading({ title: '保存中...' })
        let res
        if (this.editingId) {
          res = await uni.request({
            url: `http://localhost:8080/api/guardian/${this.editingId}`,
            method: 'PUT',
            header: { 'Content-Type': 'application/json' },
            data: { ...this.form }
          })
        } else {
          res = await uni.request({
            url: 'http://localhost:8080/api/guardian',
            method: 'POST',
            header: { 'Content-Type': 'application/json' },
            data: { userPhone: this.userPhone, ...this.form }
          })
        }
        uni.hideLoading()
        if (res.statusCode === 200 && res.data.code === 200) {
          this.showForm = false
          this.loadContacts()
          uni.showToast({ title: this.editingId ? '更新成功' : '添加成功', icon: 'success' })
        } else {
          uni.showToast({ title: res.data.message || '操作失败', icon: 'none' })
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '网络错误', icon: 'none' })
      }
    },

    deleteContact(id, index) {
      uni.showModal({
        title: '删除联系人', content: '确定要删除该联系人吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            const result = await uni.request({
              url: `http://localhost:8080/api/guardian/${id}?userPhone=${this.userPhone}`,
              method: 'DELETE'
            })
            if (result.statusCode === 200 && result.data.code === 200) {
              this.contacts.splice(index, 1)
              uni.showToast({ title: '删除成功', icon: 'success' })
            }
          } catch (e) {}
        }
      })
    },

    async setPrimary(contact, index) {
      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/guardian/${contact.id}`,
          method: 'PUT',
          header: { 'Content-Type': 'application/json' },
          data: { name: contact.name, contactPhone: contact.contactPhone, relationship: contact.relationship, isPrimary: true }
        })
        if (res.statusCode === 200 && res.data.code === 200) {
          this.contacts.forEach(c => { c.isPrimary = false })
          this.contacts[index].isPrimary = true
          uni.showToast({ title: '已设为主要联系人', icon: 'success' })
        }
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    callContact(phone) {
      uni.showModal({
        title: '拨打电话',
        content: `是否拨打 ${phone}？`,
        success: (res) => {
          if (res.confirm) uni.makePhoneCall({ phoneNumber: phone })
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
  background: #F3E5F5;
  display: flex; align-items: center; justify-content: center;
  margin-right: 16rpx;
}

.topbar-icon { font-size: 38rpx; }

.topbar-sub { font-size: 26rpx; color: #616161; }

.topbar-add-btn {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #AB47BC, #6A1B9A);
  border-radius: 30rpx;
  padding: 14rpx 28rpx;
}

.topbar-add-icon { font-size: 30rpx; color: #fff; margin-right: 6rpx; font-weight: 300; }
.topbar-add-text { font-size: 26rpx; color: #fff; font-weight: bold; }

/* ===== 空状态 ===== */
.empty-box { display: flex; flex-direction: column; align-items: center; padding: 80rpx 0; }
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #9E9E9E; margin-bottom: 30rpx; }
.empty-btn { background: linear-gradient(135deg, #AB47BC, #6A1B9A); padding: 18rpx 50rpx; border-radius: 40rpx; font-size: 28rpx; color: #fff; }

/* ===== 联系人卡片 ===== */
.contact-list { padding: 0 20rpx; }

.contact-card {
  display: flex; align-items: flex-start;
  background: #fff; border-radius: 24rpx;
  padding: 24rpx; margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}

.contact-avatar {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin-right: 20rpx; flex-shrink: 0;
}

.contact-avatar-text { font-size: 36rpx; font-weight: bold; color: #616161; }

.contact-info { flex: 1; }

.contact-name-row { display: flex; align-items: center; margin-bottom: 6rpx; }

.contact-name { font-size: 30rpx; font-weight: bold; color: #2E3A2E; margin-right: 12rpx; }

.primary-tag {
  background: #E8F5E9; color: #2E7D32;
  font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 10rpx;
  font-weight: bold;
}

.contact-rel  { display: block; font-size: 24rpx; color: #9E9E9E; margin-bottom: 6rpx; }
.contact-phone { display: block; font-size: 24rpx; color: #616161; }

.contact-actions {
  display: flex; flex-direction: column; gap: 8rpx;
  margin-left: 12rpx;
}

.action-btn { padding: 8rpx 16rpx; border-radius: 12rpx; font-size: 22rpx; text-align: center; }
.call-btn        { background: #E8F5E9; color: #2E7D32; }
.primary-set-btn { background: #FFF8E1; color: #F57F17; }
.edit-btn        { background: #E3F2FD; color: #1E88E5; }
.del-btn         { background: #FFEBEE; color: #E53935; }

/* ===== 弹窗表单 ===== */
.form-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4); z-index: 100;
  display: flex; align-items: flex-end;
}

.form-sheet {
  width: 100%;
  background: #fff; border-radius: 32rpx 32rpx 0 0;
  padding: 40rpx 30rpx 60rpx;
}

.form-title {
  display: block; font-size: 34rpx; font-weight: bold; color: #2E3A2E;
  text-align: center; margin-bottom: 36rpx;
}

.form-item { margin-bottom: 28rpx; }

.form-label { display: block; font-size: 26rpx; color: #616161; margin-bottom: 12rpx; }

.form-input {
  width: 100%;
  height: 88rpx; background: #F5F5F5;
  border-radius: 18rpx; padding: 0 24rpx;
  font-size: 28rpx; color: #2E3A2E;
  box-sizing: border-box;
}

.rel-tabs { display: flex; flex-wrap: wrap; gap: 12rpx; }

.rel-tab {
  padding: 12rpx 24rpx; border-radius: 20rpx;
  background: #F5F5F5; color: #757575; font-size: 26rpx;
}

.rel-active { background: #F3E5F5; color: #8E24AA; font-weight: bold; }

.form-switch-row { display: flex; align-items: center; justify-content: space-between; }

.form-btns { display: flex; gap: 20rpx; margin-top: 36rpx; }

.form-cancel {
  flex: 1; height: 88rpx; border-radius: 44rpx;
  background: #F5F5F5; color: #757575; font-size: 30rpx;
  display: flex; align-items: center; justify-content: center;
}

.form-confirm {
  flex: 1; height: 88rpx; border-radius: 44rpx;
  background: linear-gradient(135deg, #AB47BC, #8E24AA);
  color: #fff; font-size: 30rpx; font-weight: bold;
  display: flex; align-items: center; justify-content: center;
}

.bottom-space { height: 20rpx; }
</style>
