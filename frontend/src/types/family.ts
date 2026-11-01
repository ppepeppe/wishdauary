export interface FamilyInviteCode {
  inviteCode: string;
  active: boolean;
}

export interface FamilyMember {
  groupMemberId: number;
  userId: number;
  nickname: string;
  familyRelation?: string | null;
  relationLabel?: string | null;
}
