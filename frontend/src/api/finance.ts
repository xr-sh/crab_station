import request from './request'

export interface FinanceRecord {
  id: string
  recordDate: string
  amount: number
  type: '收入' | '支出'
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

export interface FinanceStatistics {
  income: number
  expense: number
  balance: number
}

export interface CreateFinanceRecordRequest {
  recordDate: string
  amount: number
  type: '收入' | '支出'
  remark?: string
}

export interface UpdateFinanceRecordRequest {
  recordDate?: string
  amount?: number
  type?: '收入' | '支出'
  remark?: string
}

export const financeApi = {
  // 获取财务记录列表
  getFinanceRecords: (params: {
    page?: number
    size?: number
    sortBy?: string
    sortDirection?: string
    type?: string
    keyword?: string
    startDate?: string
    endDate?: string
  }) => {
    return request.get<PageResponse<FinanceRecord>>('/finance', { params })
  },

  getFinanceStatistics: (params: {
    type?: string
    keyword?: string
    startDate?: string
    endDate?: string
  }) => {
    return request.get<FinanceStatistics>('/finance/statistics', { params })
  },

  // 获取单个财务记录
  getFinanceRecordById: (id: string) => {
    return request.get<FinanceRecord>(`/finance/${id}`)
  },

  // 创建财务记录
  createFinanceRecord: (data: CreateFinanceRecordRequest) => {
    return request.post('/finance', data)
  },

  // 更新财务记录
  updateFinanceRecord: (id: string, data: UpdateFinanceRecordRequest) => {
    return request.put(`/finance/${id}`, data)
  },

  // 删除财务记录
  deleteFinanceRecord: (id: string) => {
    return request.delete(`/finance/${id}`)
  },
}
