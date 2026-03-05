// 临时修改方案：前端ai.vue的getAIResponse方法
// 将URL改为测试接口

async getAIResponse(question) {
  try {
    // 调用测试接口（不调用DeepSeek，先测试通畅）
    const res = await uni.request({
      url: 'http://localhost:8080/api/ai/chat-test',  // ← 改成测试接口
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

// 测试通过后，再改回：
// url: 'http://localhost:8080/api/ai/chat',
