import type { AppRole } from './auth';

export interface SharingUser {
  userId: number;
  nickname: string;
  appRole: AppRole;
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
