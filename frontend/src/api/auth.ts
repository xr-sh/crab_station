import request from './request'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  user: {
    id: string
    username: string
    email: string
    avatar: string
  }
}

export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
  email?: string
  phone?: string
}

export const authApi = {
  login: (data: LoginRequest) => {
    return request.post<LoginResponse>('/auth/login', data)
  },

  register: (data: RegisterRequest) => {
    return request.post('/auth/register', data)
  },

  getCurrentUser: () => {
    return request.get('/auth/me')
  },

  logout: () => {
    return request.post('/auth/logout')
  },
}
