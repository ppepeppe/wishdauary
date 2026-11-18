import type { AppRole } from './auth';

export interface SharingUser {
  userId: number;
  nickname: string;
  appRole: AppRole;
  avatarPreset?: string | null;
  avatarCharacter?: string | null;
  avatarImage?: string | null;
}

export interface SharingGroup {
  id: number;
  name: string;
  memberIds: number[];
  members: SharingUser[];
  defaultGroup?: boolean;
}

export interface SharingGroupInput {
  name: string;
  memberIds: number[];
}
