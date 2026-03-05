/**
 * 工具函数库
 */

/**
 * 格式化日期
 * @param {Date|number} date - 日期对象或时间戳
 * @param {string} format - 格式字符串，如 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  
  const d = date instanceof Date ? date : new Date(date)
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/**
 * 防抖函数
 * @param {Function} func - 要执行的函数
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait = 500) {
  let timeout
  return function(...args) {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      func.apply(this, args)
    }, wait)
  }
}

/**
 * 节流函数
 * @param {Function} func - 要执行的函数
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(func, wait = 500) {
  let timeout
  return function(...args) {
    if (!timeout) {
      timeout = setTimeout(() => {
        timeout = null
        func.apply(this, args)
      }, wait)
    }
  }
}

/**
 * 深拷贝
 * @param {*} obj - 要拷贝的对象
 * @returns {*} 拷贝后的对象
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  
  if (obj instanceof Date) return new Date(obj)
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  
  const clonedObj = {}
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      clonedObj[key] = deepClone(obj[key])
    }
  }
  return clonedObj
}

/**
 * 显示Toast提示
 * @param {string} title - 提示内容
 * @param {string} icon - 图标类型 success/error/none
 * @param {number} duration - 显示时长（毫秒）
 */
export function showToast(title, icon = 'none', duration = 2000) {
  uni.showToast({
    title,
    icon,
    duration
  })
}

/**
 * 显示加载提示
 * @param {string} title - 提示内容
 */
export function showLoading(title = '加载中...') {
  uni.showLoading({
    title,
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
export function hideLoading() {
  uni.hideLoading()
}

/**
 * 确认对话框
 * @param {string} content - 提示内容
 * @param {string} title - 标题
 * @returns {Promise<boolean>} 用户是否确认
 */
export function confirm(content, title = '提示') {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content,
      success: (res) => {
        resolve(res.confirm)
      }
    })
  })
}

/**
 * 拨打电话
 * @param {string} phoneNumber - 电话号码
 */
export function makePhoneCall(phoneNumber) {
  uni.makePhoneCall({
    phoneNumber
  })
}

/**
 * 获取健康状态文本和颜色
 * @param {number} value - 数值
 * @param {string} type - 类型（heartRate/bloodPressure等）
 * @returns {Object} { text, color, level }
 */
export function getHealthStatus(value, type) {
  const rules = {
    heartRate: [
      { min: 60, max: 100, text: '正常', color: '#4caf50', level: 'normal' },
      { min: 100, max: 120, text: '偏快', color: '#ff9800', level: 'warning' },
      { min: 0, max: 60, text: '偏慢', color: '#ff9800', level: 'warning' },
      { min: 120, max: 999, text: '异常', color: '#f44336', level: 'danger' }
    ],
    bloodOxygen: [
      { min: 95, max: 100, text: '正常', color: '#4caf50', level: 'normal' },
      { min: 90, max: 95, text: '偏低', color: '#ff9800', level: 'warning' },
      { min: 0, max: 90, text: '异常', color: '#f44336', level: 'danger' }
    ],
    temperature: [
      { min: 36.0, max: 37.3, text: '正常', color: '#4caf50', level: 'normal' },
      { min: 37.3, max: 38.0, text: '偏高', color: '#ff9800', level: 'warning' },
      { min: 38.0, max: 99, text: '发热', color: '#f44336', level: 'danger' },
      { min: 0, max: 36.0, text: '偏低', color: '#ff9800', level: 'warning' }
    ]
  }
  
  const rule = rules[type]
  if (!rule) return { text: '未知', color: '#999', level: 'unknown' }
  
  for (const item of rule) {
    if (value >= item.min && value < item.max) {
      return item
    }
  }
  
  return { text: '异常', color: '#f44336', level: 'danger' }
}

/**
 * 计算风险等级
 * @param {number} score - 风险评分（0-100）
 * @returns {Object} { level, text, color }
 */
export function getRiskLevel(score) {
  if (score < 30) {
    return { level: 'low', text: '低风险', color: '#4caf50' }
  } else if (score < 60) {
    return { level: 'medium', text: '中风险', color: '#ff9800' }
  } else {
    return { level: 'high', text: '高风险', color: '#f44336' }
  }
}

/**
 * 存储数据到本地
 * @param {string} key - 键名
 * @param {*} value - 值
 */
export function setStorage(key, value) {
  try {
    uni.setStorageSync(key, JSON.stringify(value))
  } catch (e) {
    console.error('存储失败:', e)
  }
}

/**
 * 从本地读取数据
 * @param {string} key - 键名
 * @returns {*} 读取的值
 */
export function getStorage(key) {
  try {
    const value = uni.getStorageSync(key)
    return value ? JSON.parse(value) : null
  } catch (e) {
    console.error('读取失败:', e)
    return null
  }
}

/**
 * 删除本地存储数据
 * @param {string} key - 键名
 */
export function removeStorage(key) {
  try {
    uni.removeStorageSync(key)
  } catch (e) {
    console.error('删除失败:', e)
  }
}

export default {
  formatDate,
  debounce,
  throttle,
  deepClone,
  showToast,
  showLoading,
  hideLoading,
  confirm,
  makePhoneCall,
  getHealthStatus,
  getRiskLevel,
  setStorage,
  getStorage,
  removeStorage
}
