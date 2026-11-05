import { getTodayDateInKorea } from '../../constants/date';
import { getAvatarCharacter } from '../../constants/avatar';
import type { DiaryEmotion } from '../../types/diary';
import type { HomeSummary, MyProfile, UserStreak } from '../../types/user';
import { getCurrentUserIdOrThrow } from '../session/authSession';
import { request } from './apiClient';

interface UserStreakApiResponse {
  currentStreak?: number;
  maxStreak?: number;
}

function normalizeUserStreak(data: UserStreakApiResponse): UserStreak {
  return {
    currentStreak: Math.max(0, data.currentStreak ?? 0),
    maxStreak: Math.max(0, data.maxStreak ?? data.currentStreak ?? 0),
  };
}

interface HomeSummaryApiResponse {
  hasTodayDiary?: boolean;
  todayDiaryId?: number;
  todayDiaryEmotion?: DiaryEmotion | null;
  todayDiaryCount?: number;
  todayDiaryHeartCount?: number;
  todayDiaryCommentCount?: number;
  cloverBalance?: number;
  currentStreak?: number;
  maxStreak?: number;
}

function normalizeHomeSummary(data: HomeSummaryApiResponse): HomeSummary {
  return {
    hasTodayDiary: Boolean(data.hasTodayDiary),
    todayDiaryId: data.todayDiaryId,
    todayDiaryEmotion: data.todayDiaryEmotion ?? null,
    todayDiaryCount: Math.max(0, data.todayDiaryCount ?? 0),
    todayDiaryHeartCount: Math.max(0, data.todayDiaryHeartCount ?? 0),
    todayDiaryCommentCount: Math.max(0, data.todayDiaryCommentCount ?? 0),
    cloverBalance: Math.max(0, data.cloverBalance ?? 0),
    currentStreak: Math.max(0, data.currentStreak ?? 0),
    maxStreak: Math.max(0, data.maxStreak ?? data.currentStreak ?? 0),
  };
}

export async function fetchUserStreak() {
  getCurrentUserIdOrThrow();
  const data = await request<UserStreakApiResponse>('/api/v1/users/streak', {
    method: 'GET',
  });

  return normalizeUserStreak(data);
}

export async function fetchHomeSummary() {
  getCurrentUserIdOrThrow();
  const data = await request<HomeSummaryApiResponse>('/api/v1/users/home-summary', {
    method: 'GET',
    query: {
      date: getTodayDateInKorea(),
    },
  });

  return normalizeHomeSummary(data);
}

interface MyProfileApiResponse {
  userId?: number;
  email?: string | null;
  nickname?: string | null;
  appRole?: string | null;
  avatarPreset?: string | null;
  avatarCharacter?: string | null;
  avatarImage?: string | null;
}

function normalizeMyProfile(data: MyProfileApiResponse): MyProfile {
  return {
    userId: data.userId ?? 0,
    email: data.email?.trim() || '',
    nickname: data.nickname?.trim() || '',
    appRole: data.appRole?.trim() || '',
    avatarPreset: data.avatarPreset?.trim() || 'peach',
    avatarCharacter: getAvatarCharacter(data.avatarCharacter).key,
    avatarImage: data.avatarImage?.trim() || null,
  };
}

export async function fetchMyProfile() {
  getCurrentUserIdOrThrow();
  const data = await request<MyProfileApiResponse>('/api/v1/users/me', {
    method: 'GET',
  });
  return normalizeMyProfile(data);
}

export async function updateMyProfile(payload: {
  nickname: string;
  avatarPreset: string;
  avatarCharacter?: string;
  avatarImage: string | null;
}) {
  getCurrentUserIdOrThrow();
  const data = await request<MyProfileApiResponse>('/api/v1/users/me', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
  return normalizeMyProfile(data);
}
