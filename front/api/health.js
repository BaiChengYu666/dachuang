// 健康数据相关API
import request from './request.js'

/**
 * 健康数据API
 */
export default {
  
  /**
   * 获取最新健康数据
   * @param {Number} elderlyId 老人ID
   */
  getLatestData(elderlyId) {
    return request({
      url: `/api/data/latest/${elderlyId}`,
      method: 'GET'
    })
  },
  
  /**
   * 上传生理数据
   * @param {Object} data 生理数据
   */
  uploadPhysiologicalData(data) {
    return request({
      url: '/api/data/physiological',
      method: 'POST',
      data: data
    })
  },
  
  /**
   * 批量上传多模态数据
   * @param {Object} data 包含生理、行为、环境数据
   */
  uploadBatchData(data) {
    return request({
      url: '/api/data/batch',
      method: 'POST',
      data: data
    })
  },
  
  /**
   * 查询生理数据历史
   * @param {Number} elderlyId 老人ID
   * @param {String} startTime 开始时间（可选）
   * @param {String} endTime 结束时间（可选）
   */
  getPhysiologicalHistory(elderlyId, startTime, endTime) {
    let url = `/api/data/physiological/${elderlyId}`
    if (startTime && endTime) {
      url += `?startTime=${startTime}&endTime=${endTime}`
    }
    return request({
      url: url,
      method: 'GET'
    })
  },
  
  /**
   * 查询行为数据历史
   * @param {Number} elderlyId 老人ID
   */
  getBehaviorHistory(elderlyId) {
    return request({
      url: `/api/data/behavior/${elderlyId}`,
      method: 'GET'
    })
  },
  
  /**
   * 查询环境数据历史
   * @param {Number} elderlyId 老人ID
   */
  getEnvironmentHistory(elderlyId) {
    return request({
      url: `/api/data/environment/${elderlyId}`,
      method: 'GET'
    })
  }
}
