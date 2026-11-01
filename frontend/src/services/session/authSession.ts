import type { AppRole, LoginResponse, SignupResponse } from '../../types/auth';

const AUTH_SESSION_STORAGE_KEY = 'wishdiary.auth.session';

export interface StoredAuthSession {
  userId: number;
  appRole: AppRole;
  groupId: number;
  nickname?: string | null;
  relationLabel?: string | null;
  inviteCode?: string | null;
  accessToken: string;
}

function saveAuthSession(session: StoredAuthSession) {
  localStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(session));
}

export function saveSignupSession(payload: SignupResponse) {
  const session: StoredAuthSession = {
    userId: payload.userId,
    appRole: payload.appRole,
    groupId: payload.groupId,
    nickname: payload.nickname ?? null,
    relationLabel: payload.relationLabel ?? null,
    inviteCode: payload.inviteCode ?? null,
    accessToken: payload.accessToken,
  };

  saveAuthSession(session);
}

export function saveLoginSession(payload: LoginResponse) {
  const session: StoredAuthSession = {
    userId: payload.userId,
    appRole: payload.appRole,
    groupId: payload.groupId,
    nickname: payload.nickname ?? null,
    relationLabel: payload.relationLabel ?? null,
    inviteCode: payload.inviteCode ?? null,
    accessToken: payload.accessToken,
  };

  saveAuthSession(session);
}

function isStoredAuthSession(value: unknown): value is StoredAuthSession {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    return false;
  }
  const session = value as Record<string, unknown>;
  return typeof session.userId === 'number' && Number.isSafeInteger(session.userId) && session.userId > 0
    && typeof session.groupId === 'number' && Number.isSafeInteger(session.groupId) && session.groupId > 0
    && (session.appRole === 'child' || session.appRole === 'family')
    && typeof session.accessToken === 'string' && session.accessToken.trim().length > 0
    && ['nickname', 'relationLabel', 'inviteCode'].every(
      (field) => session[field] == null || typeof session[field] === 'string',
    );
}

export function getAuthSession(): StoredAuthSession | null {
  const rawSession = localStorage.getItem(AUTH_SESSION_STORAGE_KEY);

  if (!rawSession) {
    return null;
  }

  try {
    const session: unknown = JSON.parse(rawSession);
    if (isStoredAuthSession(session)) {
      return session;
    }
  } catch {
    // Invalid JSON and outdated session shapes both require a fresh login.
  }
  clearAuthSession();
  return null;
}

export function clearAuthSession() {
  localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
}

export function getDefaultRouteForRole(appRole?: AppRole | null) {
  return appRole === 'family' ? '/family-home' : '/home';
}

export function getCurrentSessionOrThrow() {
  const session = getAuthSession();

  if (!session) {
    throw new Error('로그인이 필요합니다.');
  }

  return session;
}

export function getCurrentUserIdOrThrow() {
  return getCurrentSessionOrThrow().userId;
}

export function getCurrentAccessToken() {
  return getAuthSession()?.accessToken ?? null;
}

export function getCurrentChildUserIdOrThrow() {
  const session = getCurrentSessionOrThrow();

  if (session.appRole !== 'child') {
    throw new Error('아이 계정으로만 접근할 수 있어요.');
  }

  return session.userId;
}

export function getCurrentFamilyUserIdOrThrow() {
  const session = getCurrentSessionOrThrow();

  if (session.appRole !== 'family') {
    throw new Error('가족 계정으로만 접근할 수 있어요.');
  }

  return session.userId;
}

export function patchAuthSession(partial: Partial<StoredAuthSession>) {
  const current = getAuthSession();
  if (!current) {
    return;
  }

  saveAuthSession({
    ...current,
    ...partial,
  });
}
