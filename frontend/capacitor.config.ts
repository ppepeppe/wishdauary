import type { CapacitorConfig } from '@capacitor/cli';

const development = process.env.WISHDIARY_MOBILE_DEVELOPMENT === 'true';

const config: CapacitorConfig = {
  appId: 'com.wishdiary.app',
  appName: '위시다유어리',
  webDir: 'dist-native',
  backgroundColor: '#ffffff',
  server: {
    hostname: 'localhost',
    androidScheme: 'https',
    iosScheme: 'capacitor',
  },
  android: {
    allowMixedContent: development,
  },
  ios: {
    contentInset: 'never',
  },
  plugins: {
    SystemBars: {
      insetsHandling: 'css',
      initialViewportFitValueHint: 'cover',
      style: 'DARK',
    },
    Keyboard: {
      resize: 'native',
      style: 'LIGHT',
      resizeOnFullScreen: true,
    },
  },
};

export default config;
