import { request } from './apiClient';
import { getCurrentChildUserIdOrThrow, getCurrentFamilyUserIdOrThrow } from '../session/authSession';
import type { CloverAwardPayload, CloverAwardResult, EnglishStudy, TodayEnglish } from '../../types/rewards';

export function awardClovers(payload: CloverAwardPayload) {
  getCurrentFamilyUserIdOrThrow();
  return request<CloverAwardResult>('/api/v1/clovers/awards', { method: 'POST', body: JSON.stringify(payload) });
}
export function fetchTodayEnglish() {
  getCurrentChildUserIdOrThrow();
  return request<TodayEnglish>('/api/v1/english/today');
}
export function requestEnglishReview(receiverFamilyUserId: number, studyDate: string, contentId: number | null, contentVersion: number | null) {
  getCurrentChildUserIdOrThrow();
  return request<EnglishStudy>('/api/v1/english/requests', {
    method: 'POST', body: JSON.stringify({ receiverFamilyUserId, studyDate, contentId, contentVersion }),
  });
}
export function fetchEnglishReviews() {
  getCurrentFamilyUserIdOrThrow();
  return request<EnglishStudy[]>('/api/v1/english/requests');
}
export function reviewEnglish(requestId: number, action: 'APPROVE' | 'REJECT') {
  getCurrentFamilyUserIdOrThrow();
  return request<EnglishStudy>(`/api/v1/english/requests/${requestId}`, { method: 'PATCH', body: JSON.stringify({ action }) });
}
