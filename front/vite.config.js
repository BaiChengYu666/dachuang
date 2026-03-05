import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  
  // 开发服务器配置
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  },
  
  // ⭐ 添加优化配置
  optimizeDeps: {
    include: ['@dcloudio/uni-h5']
  },
  
  // ⭐ 构建配置
  build: {
    sourcemap: false,
    minify: 'terser'
  }
})
