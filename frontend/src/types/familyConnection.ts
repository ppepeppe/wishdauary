export interface FamilyConnection {
  userId: number;
  nickname: string;
  relationLabel?: string | null;
}
export interface FamilyConnectionRequest {
  requestId: number;
  familyUserId: number;
  familyNickname: string;
  relationLabel: string;
  childUserId: number;
  childNickname: string;
  status: 'pending' | 'approved' | 'rejected';
  createdAt: string;
  processedAt?: string | null;
}
