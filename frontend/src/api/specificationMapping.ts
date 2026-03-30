import request from './request'

export interface SpecificationMapping {
  id: string
  purchaseSpecId: string
  purchaseSpecName: string
  platformSpecId: string
  platformSpecName: string
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

export interface CreateSpecificationMappingRequest {
  purchaseSpecId: string
  platformSpecId: string
  status?: number
  remark?: string
}

export interface UpdateSpecificationMappingRequest {
  purchaseSpecId?: string
  platformSpecId?: string
  status?: number
  remark?: string
}

export const specificationMappingApi = {
  // 获取规格映射列表
  getSpecificationMappings: (params: {
    page?: number
    size?: number
    sortBy?: string
    sortDirection?: string
    purchaseSpecName?: string
    platformSpecName?: string
    status?: number
  }) => {
    return request.get<PageResponse<SpecificationMapping>>('/specification-mappings', { params })
  },

  // 获取单个规格映射
  getSpecificationMappingById: (id: string) => {
    return request.get<SpecificationMapping>(`/specification-mappings/${id}`)
  },

  // 创建规格映射
  createSpecificationMapping: (data: CreateSpecificationMappingRequest) => {
    return request.post('/specification-mappings', data)
  },

  // 更新规格映射
  updateSpecificationMapping: (id: string, data: UpdateSpecificationMappingRequest) => {
    return request.put(`/specification-mappings/${id}`, data)
  },

  // 删除规格映射
  deleteSpecificationMapping: (id: string) => {
    return request.delete(`/specification-mappings/${id}`)
  },
}