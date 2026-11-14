import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 后端默认跑在 8080，前端开发服务器 5173
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
