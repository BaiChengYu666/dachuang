// 将ai.vue中的getAIResponse方法（约第168行）
// 替换为以下代码：

// 获取AI回复（调用DeepSeek API）
async getAIResponse(question) {
  try {
    // 调用后端接口
    const res = await uni.request({
      url: 'http://localhost:8080/api/ai/chat',
      method: 'POST',
      header: {
        'Content-Type': 'application/json'
      },
      data: {
        message: question
      }
    })
    
    console.log('✅ AI返回:', res)
    
    if (res.statusCode === 200 && res.data.code === 200) {
      const answer = res.data.data.reply
      
      const aiMsg = {
        role: 'ai',
        content: answer,
        time: this.formatTime(new Date())
      }
      this.messages.push(aiMsg)
    } else {
      // 错误处理
      const errorMsg = {
        role: 'ai',
        content: '抱歉，我暂时无法回答您的问题，请稍后再试。',
        time: this.formatTime(new Date())
      }
      this.messages.push(errorMsg)
    }
  } catch (error) {
    console.error('❌ AI请求失败:', error)
    const errorMsg = {
      role: 'ai',
      content: '抱歉，服务连接失败，请检查网络或稍后再试。',
      time: this.formatTime(new Date())
    }
    this.messages.push(errorMsg)
  }
},
