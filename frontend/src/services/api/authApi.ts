import type {
  ChildSignupRequest,
  FamilyRelation,
  FamilySignupRequest,
  LoginRequest,
  LoginResponse,
  SignupResponse,
} from '../../types/auth';
import { request } from './apiClient';

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
      email: payload.email,
      password: payload.password,
      nickname: payload.nickname,
    }),
  });
}

export async function signupFamily(payload: FamilySignupRequest) {
  return request<SignupResponse>('/api/v1/auth/signup/family', {
    method: 'POST',
    body: JSON.stringify({
      email: payload.email,
      password: payload.password,
      nickname: payload.nickname,
      familyRelation: toFamilyRelationValue(payload.familyRelation),
      relationLabel: payload.relationLabel,
    }),
  });
}

export async function login(payload: LoginRequest) {
  return request<LoginResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({
      email: payload.email,
      password: payload.password,
    }),
  });
}
