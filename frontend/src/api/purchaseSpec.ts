import request from './request'

export interface PurchaseSpec {
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

export interface CreatePurchaseSpecRequest {
  name: string
  status?: number
  remark?: string
}

export interface UpdatePurchaseSpecRequest {
  name?: string
  status?: number
  remark?: string
}

export const purchaseSpecApi = {
  // 获取进货规格列表
  getPurchaseSpecs: (params: {
    page?: number
    size?: number
    sortBy?: string
    sortDirection?: string
    name?: string
    status?: number
  }) => {
    return request.get<PageResponse<PurchaseSpec>>('/purchase-specs', { params })
  },

  // 获取所有启用的进货规格
  getAllPurchaseSpecs: () => {
    return request.get<PurchaseSpec[]>('/purchase-specs/all')
  },

  // 获取单个进货规格
  getPurchaseSpecById: (id: string) => {
    return request.get<PurchaseSpec>(`/purchase-specs/${id}`)
  },

  // 创建进货规格
  createPurchaseSpec: (data: CreatePurchaseSpecRequest) => {
    return request.post('/purchase-specs', data)
  },

  // 更新进货规格
  updatePurchaseSpec: (id: string, data: UpdatePurchaseSpecRequest) => {
    return request.put(`/purchase-specs/${id}`, data)
  },

  // 删除进货规格
  deletePurchaseSpec: (id: string) => {
    return request.delete(`/purchase-specs/${id}`)
  },
}