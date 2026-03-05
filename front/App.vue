<script>
export default {
  onLaunch: function () {
    console.log('App Launch')
    // 初始化WebSocket连接
    this.initWebSocket()
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  },
  methods: {
    initWebSocket() {
      // WebSocket连接逻辑
      const socketUrl = 'ws://your-websocket-server-url'
      
      // 注意：这里需要替换为实际的WebSocket服务器地址
      // 暂时注释掉避免连接错误
      /*
      uni.connectSocket({
        url: socketUrl,
        success: () => {
          console.log('WebSocket连接成功')
        },
        fail: (err) => {
          console.error('WebSocket连接失败:', err)
        }
      })
      
      uni.onSocketOpen(() => {
        console.log('WebSocket已打开')
      })
      
      uni.onSocketMessage((res) => {
        console.log('收到消息:', res.data)
        // 处理接收到的健康数据
      })
      
      uni.onSocketError((err) => {
        console.error('WebSocket错误:', err)
      })
      
      uni.onSocketClose(() => {
        console.log('WebSocket已关闭')
      })
      */
    }
  }
}
</script>

<style>
/*每个页面公共css */
page {
  background-color: #f5f5f5;
}

.uni-row {
  flex-direction: row;
}

.uni-column {
  flex-direction: column;
}

/* 通用样式 */
view, text, image {
  box-sizing: border-box;
}
</style>
