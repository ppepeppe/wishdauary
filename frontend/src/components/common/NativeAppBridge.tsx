import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Capacitor, type PluginListenerHandle } from '@capacitor/core';
import { App as NativeApp } from '@capacitor/app';
import { Keyboard } from '@capacitor/keyboard';
import { getAuthSession, getDefaultRouteForRole } from '../../services/session/authSession';
import { getNativeBackAction } from '../../utils/nativeNavigation';

function NativeAppBridge() {
  const navigate = useNavigate();

  useEffect(() => {
    if (!Capacitor.isNativePlatform()) return;
    const root = document.documentElement;
    root.classList.add('native-app');
    const listeners: Promise<PluginListenerHandle>[] = [
      NativeApp.addListener('appStateChange', ({ isActive }) => {
        if (isActive) window.dispatchEvent(new Event('focus'));
      }),
      Keyboard.addListener('keyboardDidShow', () => root.classList.add('native-keyboard-open')),
      Keyboard.addListener('keyboardDidHide', () => root.classList.remove('native-keyboard-open')),
    ];
    if (Capacitor.getPlatform() === 'android') {
      listeners.push(NativeApp.addListener('backButton', ({ canGoBack }) => {
        if (root.classList.contains('native-keyboard-open')) {
          void Keyboard.hide();
          return;
        }
        const action = getNativeBackAction(window.location.pathname, window.history.state?.idx ?? 0, canGoBack);
        if (action === 'back') navigate(-1);
        else if (action === 'minimize') void NativeApp.minimizeApp();
        else navigate(getDefaultRouteForRole(getAuthSession()?.appRole), { replace: true });
      }));
    }
    return () => {
      listeners.forEach((listener) => void listener.then((handle) => handle.remove()).catch(console.warn));
      root.classList.remove('native-app', 'native-keyboard-open');
    };
  }, [navigate]);

  return null;
}

export default NativeAppBridge;
