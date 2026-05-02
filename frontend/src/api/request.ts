import axios, { AxiosInstance, AxiosError } from 'axios'
import { useAuthStore } from '../stores/authStore'

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 获取 token 的辅助函数 - 直接从 localStorage 获取确保可靠性
const getToken = (): string | null => {
  try {
    // 直接从 localStorage 获取，避免 hydration 时机问题
    const authStorage = localStorage.getItem('auth-storage')
    if (authStorage) {
      const parsed = JSON.parse(authStorage)
      const token = parsed.state?.token
      if (token) {
        return token
      }
    }
  } catch (e) {
    console.error('[Request] Failed to parse auth-storage:', e)
  }
  return null
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout()
      window.location.href = '/login'
    }
    const data: any = error.response?.data
    return Promise.reject(data?.message ? data : { message: error.message })
  }
)

export default request
