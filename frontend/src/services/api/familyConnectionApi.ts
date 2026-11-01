import { request } from './apiClient';
import { getCurrentChildUserIdOrThrow, getCurrentFamilyUserIdOrThrow, getCurrentUserIdOrThrow } from '../session/authSession';
import type { FamilyConnection, FamilyConnectionRequest } from '../../types/familyConnection';
import type { SharingUser } from '../../types/sharing';

export function searchChildren(query: string) {
  getCurrentFamilyUserIdOrThrow();
  return request<SharingUser[]>('/api/v1/family-connections/children', { query: { query } });
}
export function fetchFamilyConnections() {
  getCurrentUserIdOrThrow();
  return request<FamilyConnection[]>('/api/v1/family-connections');
}
export function fetchFamilyConnectionRequests() {
  getCurrentUserIdOrThrow();
  return request<FamilyConnectionRequest[]>('/api/v1/family-connections/requests');
}
export async function fetchFamilyConnectionState() {
  const [connections, requests] = await Promise.all([fetchFamilyConnections(), fetchFamilyConnectionRequests()]);
  return { connections, requests };
}
export function requestFamilyConnection(childUserId: number) {
  getCurrentFamilyUserIdOrThrow();
  return request<FamilyConnectionRequest>('/api/v1/family-connections/requests', {
    method: 'POST', body: JSON.stringify({ childUserId }),
  });
}
export function processFamilyConnection(requestId: number, action: 'APPROVE' | 'REJECT') {
  getCurrentChildUserIdOrThrow();
  return request<FamilyConnectionRequest>(`/api/v1/family-connections/requests/${requestId}`, {
    method: 'PATCH', body: JSON.stringify({ action }),
  });
}
