import request from '@/utils/request'

export const dataImportApi = {
  /**
   * 获取BO定义列表
   */
  getBoList: (boType) => {
    const params = boType ? { boType } : {}
    return request({ url: '/api/v1/dataanalyzer/bo', method: 'GET', params })
  },

  /**
   * 获取BO定义详情
   */
  getBoDetail: (boCode) => {
    return request({ url: `/api/v1/dataanalyzer/bo/${boCode}`, method: 'GET' })
  },

  /**
   * 获取BO字段配置
   */
  getBoFields: (boCode) => {
    return request({ url: `/api/v1/dataanalyzer/bo/${boCode}/fields`, method: 'GET' })
  },

  /**
   * 分析数据文件（预览）
   */
  analyzeData: (data) => {
    return request({ url: '/api/v1/dataanalyzer/analyze', method: 'POST', data })
  },

  /**
   * 上传文件并分析
   */
  uploadAndAnalyze: (file, boCode, useAiEnhance = true) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('boCode', boCode)
    formData.append('useAiEnhance', useAiEnhance)
    return request({
      url: '/api/v1/dataanalyzer/upload-analyze',
      method: 'POST',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * AI智能字段映射
   */
  getFieldMapping: (boCode, headers) => {
    return request({
      url: '/api/v1/dataanalyzer/mapping',
      method: 'POST',
      data: { boCode, headers }
    })
  },

  /**
   * 导入数据
   */
  importData: (data) => {
    return request({ url: '/api/v1/dataanalyzer/import', method: 'POST', data })
  },

  /**
   * 上传并导入
   */
  uploadAndImport: (file, boCode, importMode = 'INSERT_UPDATE', useAiMapping = true, autoFix = false) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('boCode', boCode)
    formData.append('importMode', importMode)
    formData.append('useAiMapping', useAiMapping)
    formData.append('autoFix', autoFix)
    return request({
      url: '/api/v1/dataanalyzer/upload-import',
      method: 'POST',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * 获取导入批次列表
   */
  getBatches: (params) => {
    return request({ url: '/api/v1/dataanalyzer/batches', method: 'GET', params })
  },

  /**
   * 获取批次详情
   */
  getBatchDetail: (batchNo) => {
    return request({ url: `/api/v1/dataanalyzer/batches/${batchNo}`, method: 'GET' })
  },

  /**
   * 获取统计汇总
   */
  getStatsSummary: () => {
    return request({ url: '/api/v1/dataanalyzer/stats/summary', method: 'GET' })
  },

  /**
   * 获取今日统计
   */
  getTodayStats: (boCode) => {
    const params = boCode ? { boCode } : {}
    return request({ url: '/api/v1/dataanalyzer/stats/today', method: 'GET', params })
  },

  /**
   * 获取BO统计
   */
  getBoStats: (boCode) => {
    return request({ url: `/api/v1/dataanalyzer/stats/bo/${boCode}`, method: 'GET' })
  }
}

export default dataImportApi
