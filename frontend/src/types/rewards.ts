export interface EnglishStudy {
  requestId: number;
  childUserId: number;
  childNickname: string;
  receiverFamilyUserId: number;
  receiverName: string;
  studyDate: string;
  sentence: string;
  meaning: string;
  status: 'pending' | 'approved' | 'rejected';
  createdAt: string;
  processedAt?: string | null;
}
export interface TodayEnglish {
  studyDate: string;
  content: { contentId: number | null; version: number | null; sentence: string; meaning: string };
  request: EnglishStudy | null;
}
export interface CloverAwardPayload {
  childUserId: number;
  amount: number;
  reason: string;
  requestKey: string;
}
export interface CloverAwardResult {
  ledgerId: number;
  childUserId: number;
  amount: number;
  note: string;
  balance: number;
}
