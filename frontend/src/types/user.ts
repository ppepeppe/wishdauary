export interface UserStreak {
  currentStreak: number;
  maxStreak: number;
}

export interface HomeSummary {
  hasTodayDiary: boolean;
  todayDiaryId?: number;
  todayDiaryEmotion?: import('./diary').DiaryEmotion | null;
  todayDiaryCount: number;
  todayDiaryHeartCount: number;
  todayDiaryCommentCount: number;
  cloverBalance: number;
  currentStreak: number;
  maxStreak: number;
}

export interface MyProfile {
  userId: number;
  email: string;
  nickname: string;
  appRole: string;
  avatarPreset: string;
  avatarCharacter: string;
  avatarImage: string | null;
}
