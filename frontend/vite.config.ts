import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // 精确匹配 /api/ 开头的路径，避免匹配 /api-config 等前端路由
      '^/api/.*': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  },
})