import request from './request'

/**
 * 快递类别常量
 */
export const EXPRESS_CATEGORIES = ['顺丰', '京东'] as const
export type ExpressCategory = typeof EXPRESS_CATEGORIES[number]

/**
 * 快递分析数据类型
 */
export interface ExpressAnalysis {
  id: string
  category: string
  fileName: string
  sheetName: string
  rowNum: number
  duration: string | null
  durationHours: number | null
  dynamicFields: Record<string, any>
  importedAt: string
  createdAt: string
  updatedAt: string
}

/**
 * 导入结果类型
 */
export interface ImportResult {
  category: string
  fileName: string
  totalRows: number
  successRows: number
  failedRows: number
  columns: string[]
  errors: string[]
  success: boolean
  message: string
}

/**
 * 分页响应类型
 */
export interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number: number
  first: boolean
  last: boolean
}

/**
 * 统计信息类型
 */
export interface Statistics {
  totalRecords: number
  fileCount: number
  columnCount: number
  categoryStats: Record<string, number>
  averageFee: number
}

/**
 * 快递分析API
 */
export const expressApi = {
  /**
   * 导入Excel文件（支持多文件上传）
   */
  importExcel: async (files: File[], category: string, hasHeader: boolean = true): Promise<ImportResult[]> => {
    const formData = new FormData()
    
    // 只添加有效的 File 对象
    files.forEach((file, index) => {
      if (file && file instanceof File) {
        formData.append('files', file)
      } else {
        console.warn(`文件 ${index} 无效，跳过`)
      }
    })
    
    formData.append('category', category)
    formData.append('hasHeader', String(hasHeader))
    
    return request.post('/express/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * 分页查询快递分析数据
   * 支持按类别、导入时间范围、收件地址模糊搜索、寄件时间范围筛选
   */
  getList: (params: {
    page?: number
    size?: number
    sortBy?: string
    sortDirection?: string
    category?: string
    startDate?: string
    endDate?: string
    receiverAddress?: string
    sentTimeStart?: string
    sentTimeEnd?: string
  }) => {
    return request.get<PageResponse<ExpressAnalysis>>('/express', { params })
  },

  /**
   * 获取单条记录
   */
  getById: (id: string) => {
    return request.get<ExpressAnalysis>(`/express/${id}`)
  },

  /**
   * 获取所有列名
   */
  getColumns: () => {
    return request.get<string[]>('/express/columns')
  },

  /**
   * 获取所有文件名
   */
  getFileNames: () => {
    return request.get<string[]>('/express/file-names')
  },

  /**
   * 获取所有类别
   */
  getCategories: () => {
    return request.get<string[]>('/express/categories')
  },

  /**
   * 删除单条记录
   */
  delete: (id: string) => {
    return request.delete(`/express/${id}`)
  },

  /**
   * 清空所有数据
   */
  clearAll: () => {
    return request.delete('/express/clear')
  },

  /**
   * 删除指定文件的所有记录
   */
  deleteByFileName: (fileName: string) => {
    return request.delete(`/express/file/${encodeURIComponent(fileName)}`)
  },

  /**
   * 获取统计信息
   */
  getStatistics: () => {
    return request.get<Statistics>('/express/statistics')
  },
}