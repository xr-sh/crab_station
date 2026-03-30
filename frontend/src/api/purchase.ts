import request from './request'

export interface PurchaseItem {
  id: string
  purchaseSpecId: string
  purchaseSpecName: string
  weight: number
  unitPrice: number
  amount: number
  createdAt: string
  updatedAt: string
}

export interface PurchaseRecord {
  id: string
  purchaseDate: string
  supplier: string
  totalWeight: number
  totalAmount: number
  remark: string
  items: PurchaseItem[]
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

export interface CreatePurchaseItemRequest {
  purchaseSpecId: string
  weight: number
  unitPrice: number
}

export interface CreatePurchaseRecordRequest {
  purchaseDate: string
  supplier?: string
  remark?: string
  items: CreatePurchaseItemRequest[]
}

export interface UpdatePurchaseItemRequest {
  id?: string
  purchaseSpecId: string
  weight: number
  unitPrice: number
}

export interface UpdatePurchaseRecordRequest {
  purchaseDate?: string
  supplier?: string
  remark?: string
  items?: UpdatePurchaseItemRequest[]
}

export const purchaseApi = {
  // 获取进货记录列表
  getPurchaseRecords: (params: {
    page?: number
    size?: number
    sortBy?: string
    sortDirection?: string
    supplier?: string
    startDate?: string
    endDate?: string
  }) => {
    return request.get<PageResponse<PurchaseRecord>>('/purchases', { params })
  },

  // 获取单个进货记录
  getPurchaseRecordById: (id: string) => {
    return request.get<PurchaseRecord>(`/purchases/${id}`)
  },

  // 创建进货记录
  createPurchaseRecord: (data: CreatePurchaseRecordRequest) => {
    return request.post('/purchases', data)
  },

  // 更新进货记录
  updatePurchaseRecord: (id: string, data: UpdatePurchaseRecordRequest) => {
    return request.put(`/purchases/${id}`, data)
  },

  // 删除进货记录
  deletePurchaseRecord: (id: string) => {
    return request.delete(`/purchases/${id}`)
  },
}
