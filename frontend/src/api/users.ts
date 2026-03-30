import request from './request'

export interface User {
  id: string
  username: string
  email: string
  phone: string
  avatar: string
  status: number
  lastLoginTime: string
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

export interface CreateUserRequest {
  username: string
  password: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
}

export interface UpdateUserRequest {
  email?: string
  phone?: string
  avatar?: string
  status?: number
  password?: string
}

export const userApi = {
  // 获取用户列表
  getUsers: (params: {
    page?: number
    size?: number
    sortBy?: string
    sortDirection?: string
    keyword?: string
  }) => {
    return request.get<PageResponse<User>>('/users', { params })
  },

  // 获取单个用户
  getUserById: (id: string) => {
    return request.get<User>(`/users/${id}`)
  },

  // 创建用户
  createUser: (data: CreateUserRequest) => {
    return request.post('/users', data)
  },

  // 更新用户
  updateUser: (id: string, data: UpdateUserRequest) => {
    return request.put(`/users/${id}`, data)
  },

  // 删除用户
  deleteUser: (id: string) => {
    return request.delete(`/users/${id}`)
  },

  // 更新用户状态
  updateUserStatus: (id: string, status: number) => {
    return request.patch(`/users/${id}/status?status=${status}`)
  },
}
