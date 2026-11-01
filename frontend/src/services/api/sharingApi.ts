import type { SharingGroup, SharingGroupInput, SharingUser } from '../../types/sharing';
import { getCurrentChildUserIdOrThrow } from '../session/authSession';
import { request } from './apiClient';

export function fetchSharingGroups() {
  getCurrentChildUserIdOrThrow();
  return request<SharingGroup[]>('/api/v1/sharing-groups');
}

export function searchSharingUsers(query: string) {
  getCurrentChildUserIdOrThrow();
  return request<SharingUser[]>('/api/v1/sharing-groups/users', { query: { query } });
}

export function saveSharingGroup(input: SharingGroupInput, id?: number) {
  getCurrentChildUserIdOrThrow();
  return request<SharingGroup>(`/api/v1/sharing-groups${id === undefined ? '' : `/${id}`}`, {
    method: id === undefined ? 'POST' : 'PATCH',
    body: JSON.stringify(input),
  });
}

export function deleteSharingGroup(id: number) {
  getCurrentChildUserIdOrThrow();
  return request<void>(`/api/v1/sharing-groups/${id}`, { method: 'DELETE' });
}
