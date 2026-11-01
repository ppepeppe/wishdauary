import type { CommonResponse } from '../../types/common';
import { clearAuthSession, getCurrentAccessToken } from '../session/authSession';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || window.location.origin;

interface RequestOptions extends RequestInit {
  query?: Record<string, string | number | boolean | undefined>;
}

function createUrl(path: string, query?: RequestOptions['query']) {
  const url = new URL(path, API_BASE_URL);

  if (!query) {
    return url.toString();
  }

  Object.entries(query).forEach(([key, value]) => {
    if (value === undefined) {
      return;
    }

    url.searchParams.set(key, String(value));
  });

  return url.toString();
}

async function extractErrorMessage(response: Response) {
  try {
    const data = (await response.json()) as { code?: string; message?: string; error?: string };

    if (data.code === 'DUPLICATE_USER_EMAIL') {
      return '이미 가입된 이메일이에요.';
    }

    if (typeof data.message === 'string' && /^[가-힣]/.test(data.message)) {
      return data.message;
    }

    if (typeof data.error === 'string' && /^[가-힣]/.test(data.error)) {
      return data.error;
    }
  } catch {
    return null;
  }

  return null;
}

function hasCommonResponseData<T>(value: unknown): value is CommonResponse<T> {
  if (!value || typeof value !== 'object') {
    return false;
  }

  return 'success' in value && 'code' in value && 'message' in value && 'data' in value;
}

function getDefaultErrorMessage(status: number) {
  if (status >= 500) {
    return '서버 오류가 발생했어요. 잠시 후 다시 시도해 주세요.';
  }

  if (status === 404) {
    return '요청한 정보를 찾을 수 없어요.';
  }

  if (status === 400) {
    return '요청 형식을 다시 확인해 주세요.';
  }

  return '요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.';
}

export async function request<T>(path: string, options: RequestOptions = {}) {
  const { query, headers, ...init } = options;
  let response: Response;
  const accessToken = getCurrentAccessToken();
  const requestHeaders = new Headers({ 'Content-Type': 'application/json' });
  if (accessToken) {
    requestHeaders.set('Authorization', `Bearer ${accessToken}`);
  }
  new Headers(headers).forEach((value, key) => requestHeaders.set(key, value));

  try {
    response = await fetch(createUrl(path, query), {
      ...init,
      headers: requestHeaders,
    });
  } catch {
    throw new Error('네트워크 연결을 확인해 주세요.');
  }

  if (!response.ok) {
    if (response.status === 401 && accessToken
      && requestHeaders.get('Authorization') === `Bearer ${accessToken}`
      && getCurrentAccessToken() === accessToken) {
      clearAuthSession();
    }
    const message = (await extractErrorMessage(response)) ?? getDefaultErrorMessage(response.status);
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  let json: T | CommonResponse<T>;
  try {
    json = (await response.json()) as T | CommonResponse<T>;
  } catch {
    throw new Error('응답을 읽지 못했어요. 잠시 후 다시 시도해 주세요.');
  }

  if (hasCommonResponseData<T>(json)) {
    return json.data;
  }

  return json as T;
}
