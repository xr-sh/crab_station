import request from './request'

export interface PlatformPackageItem {
  id: string
  platformSpecId: string
  platformSpecName: string
  platformSpecCategory: string | null
  sortNo: number
  createdAt: string
  updatedAt: string
}

export interface PlatformPackagePriceRule {
  id: string
  qty: number
  salePrice: number | null
  costPrice: number | null
  costMode: 'AUTO' | 'MANUAL'
  sortNo: number
  createdAt: string
  updatedAt: string
}

export interface PlatformPackage {
  id: string
  name: string
  price: number | null
  fixedCost: number | null
  totalCost: number | null
  status: number
  remark: string
  items: PlatformPackageItem[]
  priceRules: PlatformPackagePriceRule[]
  specCount: number
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

export interface PlatformSpec {
  id: string
  name: string
  category: string | null
  status: number
  remark: string
  createdAt: string
  updatedAt: string
}

export interface CreatePlatformPackageRequest {
  selections: Array<{
    category: string
    platformSpecId: string
    qty: number
  }>
  price?: number
  status?: number
  remark?: string
}

export interface UpdatePlatformPackageRequest {
  selections?: Array<{
    category: string
    platformSpecId: string
    qty: number
  }>
  price?: number
  status?: number
  remark?: string
}

export interface PlatformPackageCostConfig {
  fixedCost: number
  remark?: string
}

export const platformPackageApi = {
  getPlatformPackages: (params: {
    page?: number
    size?: number
    sortBy?: string
    sortDirection?: string
    name?: string
    status?: number
  }) => {
    return request.get<PageResponse<PlatformPackage>>('/platform-packages', { params })
  },

  getAllPlatformPackages: () => {
    return request.get<PlatformPackage[]>('/platform-packages/all')
  },

  getPlatformPackageById: (id: string) => {
    return request.get<PlatformPackage>(`/platform-packages/${id}`)
  },

  createPlatformPackage: (data: CreatePlatformPackageRequest) => {
    return request.post('/platform-packages', data)
  },

  updatePlatformPackage: (id: string, data: UpdatePlatformPackageRequest) => {
    return request.put(`/platform-packages/${id}`, data)
  },

  deletePlatformPackage: (id: string) => {
    return request.delete(`/platform-packages/${id}`)
  },

  getActivePlatformSpecs: (category?: string) => {
    return request.get<PlatformSpec[]>('/platform-specs/active', { params: { category } })
  },

  getCostConfig: () => {
    return request.get<PlatformPackageCostConfig>('/system-configs/platform-package-cost')
  },

  updateCostConfig: (data: PlatformPackageCostConfig) => {
    return request.put<PlatformPackageCostConfig>('/system-configs/platform-package-cost', data)
  },
}
