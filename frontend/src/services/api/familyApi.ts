import type { FamilyInviteCode, FamilyMember } from '../../types/family';
import { getCurrentChildUserIdOrThrow } from '../session/authSession';
import { request } from './apiClient';

interface FamilyInviteCodeApiResponse {
  inviteCode?: string;
  active?: boolean;
}

interface FamilyMemberApiItem {
  groupMemberId?: number;
  userId?: number;
  nickname?: string | null;
  familyRelation?: string | null;
  relationLabel?: string | null;
}

function normalizeInviteCode(data: FamilyInviteCodeApiResponse): FamilyInviteCode {
  return {
    inviteCode: data.inviteCode ?? '',
    active: Boolean(data.active),
  };
}

function normalizeFamilyMember(item: FamilyMemberApiItem): FamilyMember {
  return {
    groupMemberId: item.groupMemberId ?? 0,
    userId: item.userId ?? 0,
    nickname: item.nickname?.trim() || '가족',
    familyRelation: item.familyRelation ?? null,
    relationLabel: item.relationLabel?.trim() || '가족',
  };
}

export async function fetchFamilyInviteCode() {
  getCurrentChildUserIdOrThrow();
  const data = await request<FamilyInviteCodeApiResponse>('/api/v1/family/invite-code', {
    method: 'GET',
  });

  return normalizeInviteCode(data);
}

export async function fetchFamilyMembers() {
  getCurrentChildUserIdOrThrow();
  const data = await request<FamilyMemberApiItem[]>('/api/v1/family/members', {
    method: 'GET',
  });

  return data.map(normalizeFamilyMember).filter((member) => member.groupMemberId > 0);
}

export async function regenerateFamilyInviteCode() {
  getCurrentChildUserIdOrThrow();
  const data = await request<FamilyInviteCodeApiResponse>('/api/v1/family/invite-code/regenerate', {
    method: 'PATCH',
  });

  return normalizeInviteCode(data);
}

export async function revokeFamilyInviteCode() {
  getCurrentChildUserIdOrThrow();
  const data = await request<FamilyInviteCodeApiResponse>('/api/v1/family/invite-code/revoke', {
    method: 'PATCH',
  });

  return normalizeInviteCode(data);
}

export async function removeFamilyMember(groupMemberId: number) {
  getCurrentChildUserIdOrThrow();
  await request<void>(`/api/v1/family/members/${groupMemberId}`, {
    method: 'DELETE',
  });
}
