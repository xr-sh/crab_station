import request from './request'

export interface ApiConfig {
  id: string
  platformName: string
  apiKey: string
  secret: string
  baseUrl: string
  dynamicConfig: Record<string, any> | null
  status: number
  remark: string
  createdAt: string
  updatedAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface CreateApiConfigRequest {
  platformName: string
  apiKey?: string
  secret?: string
  baseUrl?: string
  dynamicConfig?: Record<string, any>
  status?: number
  remark?: string
}

export interface UpdateApiConfigRequest {
  platformName?: string
  apiKey?: string
  secret?: string
  baseUrl?: string
  dynamicConfig?: Record<string, any>
  status?: number
  remark?: string
}

export const apiConfigApi = {
  // 获取API配置列表
  getApiConfigs: (params: {
    page?: number
    size?: number
    sortBy?: string
    sortDirection?: string
    platformName?: string
    status?: number
  }) => {
    return request.get<PageResponse<ApiConfig>>('/api-configs', { params })
  },

  // 获取所有启用的API配置
  getAllApiConfigs: () => {
    return request.get<ApiConfig[]>('/api-configs/all')
  },

  // 获取单个API配置
  getApiConfigById: (id: string) => {
    return request.get<ApiConfig>(`/api-configs/${id}`)
  },

  // 按平台名称获取API配置
  getApiConfigByPlatformName: (platformName: string) => {
    return request.get<ApiConfig>(`/api-configs/platform/${platformName}`)
  },

  // 创建API配置
  createApiConfig: (data: CreateApiConfigRequest) => {
    return request.post('/api-configs', data)
  },

  // 更新API配置
  updateApiConfig: (id: string, data: UpdateApiConfigRequest) => {
    return request.put(`/api-configs/${id}`, data)
  },

  // 删除API配置
  deleteApiConfig: (id: string) => {
    return request.delete(`/api-configs/${id}`)
  },
}