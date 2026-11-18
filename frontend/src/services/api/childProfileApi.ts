import { request } from './apiClient';
import { getCurrentFamilyUserIdOrThrow } from '../session/authSession';
import type { CloverHistoryEntry } from './cloverApi';

export interface ChildProfile {
  childId: number;
  nickname: string;
  clovers: { balance: number };
  history: { entries: CloverHistoryEntry[] };
  giftRequests: {
    notificationId: number; productTitle: string; cloverPrice: number;
    status: string; createdAt: string; receivedAt?: string | null;
  }[];
}

export function fetchChildProfile(childId: number) {
  getCurrentFamilyUserIdOrThrow();
  if (!Number.isSafeInteger(childId) || childId <= 0) throw new Error('아이를 확인해 주세요.');
  return request<ChildProfile>(`/api/v1/family/children/${childId}`);
}
