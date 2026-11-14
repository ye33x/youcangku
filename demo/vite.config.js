import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  base: './',
  server: {
    port: 5175,
    host: '127.0.0.1',
    strictPort: true,
    watch: {
      // 编辑器保存时会在 src 下产生临时目录，Vite 的文件监听会因 EBUSY 崩溃，直接忽略
      ignored: ['**/*.tmpdir/**', '**/.*.tmp*', '**/dist/**', '**/.tmp/**']
    }
  }
})
