export function resolveMobileOptions(platform, { development = false, apiUrl } = {}) {
  if (!['android', 'ios'].includes(platform)) {
    throw new Error('플랫폼을 지정해 주세요: android 또는 ios');
  }
  const localUrl = platform === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';
  const value = apiUrl?.trim() || (development ? localUrl : '');
  if (!value) throw new Error('--api-url 또는 MOBILE_API_BASE_URL에 백엔드 주소를 지정해 주세요.');
  let url;
  try { url = new URL(value); } catch { throw new Error('백엔드 주소는 올바른 http(s) 주소여야 해요.'); }
  if (!['http:', 'https:'].includes(url.protocol) || url.username || url.password
      || url.pathname !== '/' || url.search || url.hash) {
    throw new Error('백엔드 주소에는 http(s) 서버 주소만 입력해 주세요. 경로·인증 정보는 넣지 않아요.');
  }
  if (!development && (url.protocol !== 'https:' || ['localhost', '127.0.0.1', '[::1]', '10.0.2.2'].includes(url.hostname))) {
    throw new Error('배포용 앱에는 외부에서 접속 가능한 HTTPS 백엔드 주소가 필요해요. 로컬 테스트에는 --development를 사용하세요.');
  }
  return { platform, development, apiBaseUrl: url.origin };
}
