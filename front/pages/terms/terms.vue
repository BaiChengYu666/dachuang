<template>
  <view class="terms-page">
    <view class="terms-content">
      <text class="terms-title">{{ isPrivacy ? '隐私政策' : '用户协议' }}</text>
      <text class="update-date">更新日期：2026年3月1日</text>

      <!-- 用户协议 -->
      <block v-if="!isPrivacy">
        <text class="section-title">一、总则</text>
        <text class="section-body">欢迎使用i龄守护（以下简称"本应用"）。本用户协议（以下简称"本协议"）由您与i龄守护运营方共同缔结，请您在使用本应用前仔细阅读本协议全部内容。</text>

        <text class="section-title">二、服务内容</text>
        <text class="section-body">本应用提供针对老年人的多维健康监测服务，包括但不限于：生理数据监测（心率、血压、血氧、体温）、行为状态识别、环境参数监测、健康风险评估及AI健康咨询等功能。</text>

        <text class="section-title">三、用户注册与账户安全</text>
        <text class="section-body">1. 您须提供真实、准确的手机号码进行注册，并对账户安全负责。
2. 请勿将账户信息泄露给他人，若发现账户被盗用，请立即联系我们。
3. 本应用有权在您违反本协议时暂停或终止您的账户。</text>

        <text class="section-title">四、用户行为规范</text>
        <text class="section-body">您使用本应用时，应遵守中华人民共和国相关法律法规，不得利用本应用从事违法活动，不得干扰本应用正常运行，不得侵犯他人合法权益。</text>

        <text class="section-title">五、免责声明</text>
        <text class="section-body">本应用提供的健康监测数据及AI建议仅供参考，不构成医疗诊断意见。如出现健康问题，请及时就医。本应用不对因参考相关数据或建议而造成的后果承担法律责任。</text>

        <text class="section-title">六、知识产权</text>
        <text class="section-body">本应用内所有内容（包括但不限于文字、图片、代码、设计）均受知识产权法律保护，未经授权不得复制、传播或用于商业用途。</text>

        <text class="section-title">七、协议修改</text>
        <text class="section-body">本应用有权不时修改本协议。修改后的协议将在应用内公示，您继续使用本应用即视为接受修改后的协议。</text>

        <text class="section-title">八、联系我们</text>
        <text class="section-body">如您对本协议有任何疑问，请通过应用内反馈功能与我们联系。</text>
      </block>

      <!-- 隐私政策 -->
      <block v-else>
        <text class="section-title">一、信息收集</text>
        <text class="section-body">为提供健康监测服务，本应用会收集以下信息：
• 账户信息：手机号、性别、年龄、昵称
• 健康数据：心率、血压、血氧、体温、行为状态
• 环境数据：室温、湿度、空气质量等
• 设备信息：设备型号、操作系统版本（用于兼容性优化）</text>

        <text class="section-title">二、信息使用</text>
        <text class="section-body">您的信息将用于：
1. 提供健康监测及风险评估服务
2. AI健康建议生成
3. 异常健康状况预警通知
4. 改善产品体验及功能优化

我们不会将您的个人信息出售给任何第三方。</text>

        <text class="section-title">三、信息存储与安全</text>
        <text class="section-body">您的数据存储于安全服务器，我们采用加密传输（HTTPS）及访问控制等措施保护您的信息安全。健康数据仅存储于您授权的设备和服务端，我们承诺不超出本政策范围使用您的数据。</text>

        <text class="section-title">四、信息共享</text>
        <text class="section-body">在以下情况下，我们可能共享您的信息：
• 您明确同意共享
• 依据法律法规要求
• 紧急情况下保护用户人身安全

除上述情况外，我们不会向第三方披露您的个人信息。</text>

        <text class="section-title">五、您的权利</text>
        <text class="section-body">您有权：
• 查询、更正或删除您的个人信息
• 撤回授权同意
• 注销账户（注销后数据将在30天内删除）

如需行使上述权利，请通过应用内设置页面操作，或联系我们的客服。</text>

        <text class="section-title">六、Cookie及类似技术</text>
        <text class="section-body">本应用使用本地缓存技术存储您的登录状态和偏好设置，以提升使用体验。您可通过清除应用缓存来删除相关数据。</text>

        <text class="section-title">七、未成年人保护</text>
        <text class="section-body">本应用主要面向老年人健康监护场景，若您代替未成年人使用本应用，请确保已取得监护人同意。</text>

        <text class="section-title">八、政策更新</text>
        <text class="section-body">本隐私政策如有重大变更，我们将在应用内以显著方式通知您。继续使用本应用即表示您接受更新后的隐私政策。</text>

        <text class="section-title">九、联系我们</text>
        <text class="section-body">如您对本隐私政策有任何疑问或投诉，请通过应用内反馈功能与我们取得联系。</text>
      </block>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      isPrivacy: false
    }
  },

  onLoad(options) {
    this.isPrivacy = options.type === 'privacy'
    uni.setNavigationBarTitle({
      title: this.isPrivacy ? '隐私政策' : '用户协议'
    })
  }
}
</script>

<style scoped>
.terms-page {
  min-height: 100vh;
  background: #ffffff;
}

.terms-content {
  padding: 40rpx 40rpx 80rpx;
  display: flex;
  flex-direction: column;
}

.terms-title {
  font-size: 44rpx;
  font-weight: bold;
  color: #1A1A1A;
  margin-bottom: 10rpx;
}

.update-date {
  font-size: 24rpx;
  color: #9E9E9E;
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #2E3A2E;
  margin-top: 36rpx;
  margin-bottom: 14rpx;
}

.section-body {
  font-size: 26rpx;
  color: #555555;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
