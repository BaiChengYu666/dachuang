// API配置文件
// 根据运行环境自动切换后端地址
const BASE_URL = process.env.NODE_ENV === 'development' 
  ? '/api'  // 开发环境使用代理
  : 'http://localhost:8080'  // 生产环境直连

// 请求封装
const request = (options) => {
  return new Promise((resolve, reject) => {
    // ⭐ 开发环境（浏览器）使用代理
    const url = BASE_URL + options.url
    
    console.log('🌐 请求URL:', url)
    
    uni.request({
      url: url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...options.header
      },
      success: (res) => {
        console.log('📦 响应数据:', res)
        
        if (res.statusCode === 200) {
          // 后端统一返回格式：{ code, message, data }
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else {
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        console.error('❌ API请求失败:', err)
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default request
export { BASE_URL }
