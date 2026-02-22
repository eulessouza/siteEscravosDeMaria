import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    hmr: {
      overlay: false,
    },
    proxy: {
      // Proxy `/auth` and `/api` requests to the backend (ngrok) to avoid CORS in dev
      '/auth': {
        target: 'https://bernita-napless-datedly.ngrok-free.dev',
        changeOrigin: true,
        secure: false,
      },
      '/api': {
        target: 'https://bernita-napless-datedly.ngrok-free.dev',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: 'dist',
  },
})
