// 老人信息相关API
import request from './request.js'

/**
 * 老人信息API
 */
export default {
  
  /**
   * 获取所有老人列表
   */
  getList() {
    return request({
      url: '/api/elderly/list',
      method: 'GET'
    })
  },
  
  /**
   * 获取老人详情
   * @param {Number} id 老人ID
   */
  getDetail(id) {
    return request({
      url: `/api/elderly/${id}`,
      method: 'GET'
    })
  },
  
  /**
   * 新增老人
   * @param {Object} data 老人信息
   */
  create(data) {
    return request({
      url: '/api/elderly',
      method: 'POST',
      data: data
    })
  },
  
  /**
   * 更新老人信息
   * @param {Number} id 老人ID
   * @param {Object} data 更新的信息
   */
  update(id, data) {
    return request({
      url: `/api/elderly/${id}`,
      method: 'PUT',
      data: data
    })
  },
  
  /**
   * 删除老人
   * @param {Number} id 老人ID
   */
  delete(id) {
    return request({
      url: `/api/elderly/${id}`,
      method: 'DELETE'
    })
  }
}
