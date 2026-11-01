const ROOT_PATHS = new Set(['/', '/home', '/family-home', '/login']);

export function getNativeBackAction(pathname: string, historyIndex: number, canGoBack: boolean) {
  if (ROOT_PATHS.has(pathname)) return 'minimize';
  return canGoBack && historyIndex > 0 ? 'back' : 'home';
}
