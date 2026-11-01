import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => ({
  plugins: [react()],
  build: {
    outDir: mode === 'native' ? 'dist-native' : 'dist',
  },
  server: {
    port: 5187,
    strictPort: true,
    proxy: {
      '/api': {
        target: loadEnv(mode, '.', 'BACKEND_URL').BACKEND_URL || 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
}));
