import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    open: true,
    headers: {
      'Referrer-Policy': 'no-referrer',
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8064',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});