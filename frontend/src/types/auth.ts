export type AppRole = 'child' | 'family';

export type FamilyRelation =
  | 'mother'
  | 'father'
  | 'grandmother'
  | 'grandfather'
  | 'aunt'
  | 'uncle'
  | 'other';

export interface ChildSignupRequest {
  email: string;
  password: string;
  nickname: string;
}

export interface FamilySignupRequest {
  email: string;
  password: string;
  nickname: string;
  familyRelation: FamilyRelation;
  relationLabel: string;
}

export interface SignupResponse {
  userId: number;
  groupId: number;
  groupMemberId?: number;
  appRole: AppRole;
  memberRole: string;
  nickname?: string | null;
  familyRelation?: string | null;
  relationLabel?: string | null;
  inviteCode?: string | null;
  accessToken: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  userId: number;
  groupId: number;
  appRole: AppRole;
  memberRole: string;
  nickname?: string | null;
  relationLabel?: string | null;
  inviteCode?: string | null;
  accessToken: string;
}
