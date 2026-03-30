import request from './request'

export interface PlatformSpec {
  id: string
  name: string
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

export interface CreatePlatformSpecRequest {
  name: string
  status?: number
  remark?: string
}

export interface UpdatePlatformSpecRequest {
  name?: string
  status?: number
  remark?: string
}

export const platformSpecApi = {
  // 获取平台规格列表
  getPlatformSpecs: (params: {
    page?: number
    size?: number
    sortBy?: string
    sortDirection?: string
    name?: string
    status?: number
  }) => {
    return request.get<PageResponse<PlatformSpec>>('/platform-specs', { params })
  },

  // 获取所有启用的平台规格
  getAllPlatformSpecs: () => {
    return request.get<PlatformSpec[]>('/platform-specs/all')
  },

  // 获取单个平台规格
  getPlatformSpecById: (id: string) => {
    return request.get<PlatformSpec>(`/platform-specs/${id}`)
  },

  // 创建平台规格
  createPlatformSpec: (data: CreatePlatformSpecRequest) => {
    return request.post('/platform-specs', data)
  },

  // 更新平台规格
  updatePlatformSpec: (id: string, data: UpdatePlatformSpecRequest) => {
    return request.put(`/platform-specs/${id}`, data)
  },

  // 删除平台规格
  deletePlatformSpec: (id: string) => {
    return request.delete(`/platform-specs/${id}`)
  },
}