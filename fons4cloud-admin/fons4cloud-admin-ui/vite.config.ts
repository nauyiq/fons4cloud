import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  base: '/admin-ui/',
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/admin/api': 'http://127.0.0.1:8002',
    },
  },
  test: {
    environment: 'happy-dom',
    globals: true,
    include: ['tests/*.spec.ts'],
  },
})
