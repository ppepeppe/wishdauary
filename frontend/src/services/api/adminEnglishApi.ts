import { getCurrentUserIdOrThrow } from '../session/authSession';
import { request } from './apiClient';
import type { EnglishContent, EnglishContentOverview, EnglishContentPayload } from '../../types/englishContent';

const CONTENTS = '/api/v1/admin/english/contents';
export function fetchEnglishContents() {
  getCurrentUserIdOrThrow();
  return request<EnglishContentOverview>(CONTENTS);
}
export function createEnglishContent(payload: EnglishContentPayload) {
  getCurrentUserIdOrThrow();
  return request<EnglishContent>(CONTENTS, { method: 'POST', body: JSON.stringify(payload) });
}
export function updateEnglishContent(contentId: number, payload: EnglishContentPayload, version: number) {
  getCurrentUserIdOrThrow();
  return request<EnglishContent>(`${CONTENTS}/${contentId}`, { method: 'PATCH', body: JSON.stringify({ ...payload, version }) });
}
