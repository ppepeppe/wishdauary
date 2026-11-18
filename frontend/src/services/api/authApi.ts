import type {
  ChildSignupRequest,
  FamilyRelation,
  FamilySignupRequest,
  LoginRequest,
  LoginResponse,
  SignupResponse,
} from '../../types/auth';
import { request } from './apiClient';
import { getFamilyRelation } from '../../constants/signup';

function toFamilyRelationValue(relation: FamilyRelation) {
  if (relation === 'other') {
    return 'OTHER_FAMILY';
  }

  return relation.toUpperCase();
}

export async function signupChild(payload: ChildSignupRequest) {
  return request<SignupResponse>('/api/v1/auth/signup/child', {
    method: 'POST',
    body: JSON.stringify({
      loginId: payload.loginId,
      password: payload.password,
      nickname: payload.nickname,
    }),
  });
}

export async function signupFamily(payload: FamilySignupRequest) {
  return request<SignupResponse>('/api/v1/auth/signup/family', {
    method: 'POST',
    body: JSON.stringify({
      loginId: payload.loginId,
      password: payload.password,
      nickname: payload.nickname,
      familyRelation: toFamilyRelationValue(payload.familyRelation ?? getFamilyRelation(payload.relationLabel)),
      relationLabel: payload.relationLabel.trim(),
    }),
  });
}

export async function login(payload: LoginRequest) {
  return request<LoginResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({
      loginId: payload.loginId,
      password: payload.password,
    }),
  });
}

export interface SignupAvailability { available: boolean; message: string }
export function checkSignupAvailability(field: 'LOGIN_ID' | 'NICKNAME', value: string, signal?: AbortSignal) {
  return request<SignupAvailability>('/api/v1/auth/signup/availability', { query: { field, value }, signal });
}
