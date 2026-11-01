import { getTodayDateInKorea } from '../../constants/date';
import {
  getCurrentChildUserIdOrThrow,
  getCurrentFamilyUserIdOrThrow,
  getCurrentUserIdOrThrow,
} from '../session/authSession';
import type {
  CreateDiaryCommentRequest,
  DiaryComment,
  DiaryDetail,
  CreateDiaryRequest,
  DiaryEntry,
  ReactionFeedItem,
  ReactionFeedType,
  TodayDiaryStatus,
  ToggleDiaryHeartResponse,
  UpdateDiaryRequest,
} from '../../types/diary';
import { request } from './apiClient';

interface ReactionFeedApiItem {
  id?: number;
  reactionId?: number;
  type?: string;
  reactionType?: string;
  content?: string | null;
  commentContent?: string | null;
  createdAt?: string | null;
  reactedAt?: string | null;
  actorUserId?: number;
  actorNickname?: string | null;
  actorRelationLabel?: string | null;
}

interface DiaryEntryApiItem {
  id?: number;
  diaryId?: number;
  authorId?: number;
  authorNickname?: string | null;
  authorRelationLabel?: string | null;
  content?: string;
  contentPreview?: string;
  visibilityScope?: DiaryEntry['visibilityScope'];
  sharingGroupIds?: number[];
  createdAt?: string;
  writtenDate?: string;
  emotion?: DiaryEntry['emotion'];
  hearted?: boolean;
  heartCount?: number;
  commentCount?: number;
  actorUserId?: number;
  actorNickname?: string | null;
  actorRelationLabel?: string | null;
}

interface DiaryCommentApiItem {
  id?: number;
  commentId?: number;
  content?: string;
  createdAt?: string;
  authorName?: string;
  actorUserId?: number;
  actorNickname?: string | null;
  actorRelationLabel?: string | null;
}

function normalizeDiaryEntry(item: DiaryEntryApiItem): DiaryEntry {
  return {
    id: item.id ?? item.diaryId ?? 0,
    content: item.content ?? '',
    contentPreview: item.contentPreview ?? item.content ?? '',
    visibilityScope: item.visibilityScope ?? 'private',
    sharingGroupIds: item.sharingGroupIds ?? [],
    createdAt: item.createdAt ?? item.writtenDate ?? new Date(0).toISOString(),
    writtenDate: item.writtenDate,
    emotion: item.emotion ?? null,
    hearted: item.hearted ?? false,
    heartCount: item.heartCount ?? 0,
    commentCount: item.commentCount ?? 0,
    actorUserId: item.actorUserId ?? item.authorId,
    actorNickname: item.actorNickname ?? item.authorNickname ?? null,
    actorRelationLabel: item.actorRelationLabel ?? item.authorRelationLabel ?? null,
  };
}

function normalizeDiaryDetail(item: DiaryEntryApiItem): DiaryDetail {
  const entry = normalizeDiaryEntry(item);

  return {
    ...entry,
    authorId: item.authorId,
    content: item.content ?? entry.contentPreview,
    writtenDate: item.writtenDate ?? entry.createdAt,
  };
}

function normalizeDiaryComment(item: DiaryCommentApiItem): DiaryComment {
  return {
    id: item.id ?? item.commentId ?? 0,
    content: item.content ?? '',
    createdAt: item.createdAt,
    authorName: item.authorName,
    actorUserId: item.actorUserId,
    actorNickname: item.actorNickname ?? null,
    actorRelationLabel: item.actorRelationLabel ?? null,
  };
}

function normalizeReactionType(value?: string): ReactionFeedType {
  return value?.toLowerCase() === 'comment' ? 'comment' : 'heart';
}

function normalizeReactionFeedItem(item: ReactionFeedApiItem, index: number): ReactionFeedItem {
  return {
    id: item.id ?? item.reactionId ?? index,
    type: normalizeReactionType(item.type ?? item.reactionType),
    content: item.content ?? item.commentContent ?? null,
    createdAt: item.createdAt ?? item.reactedAt ?? null,
    actorUserId: item.actorUserId,
    actorNickname: item.actorNickname ?? null,
    actorRelationLabel: item.actorRelationLabel ?? null,
  };
}

function sortReactionFeed(items: ReactionFeedItem[]) {
  return [...items].sort((left, right) => {
    const leftTime = left.createdAt ? new Date(left.createdAt).getTime() : 0;
    const rightTime = right.createdAt ? new Date(right.createdAt).getTime() : 0;

    return rightTime - leftTime;
  });
}

export async function fetchDiaryStatus(date = getTodayDateInKorea()) {
  return request<TodayDiaryStatus>('/api/v1/diaries/existence', {
    method: 'GET',
    query: {
      date,
    },
  });
}

export function fetchTodayDiaryStatus() {
  return fetchDiaryStatus(getTodayDateInKorea());
}

export async function fetchDiaryList() {
  const items = await request<DiaryEntryApiItem[]>('/api/v1/diaries', {
    method: 'GET',
  });

  return items.map(normalizeDiaryEntry);
}

export async function fetchDiaryDetail(diaryId: number) {
  getCurrentUserIdOrThrow();
  const diary = await request<DiaryEntryApiItem>(`/api/v1/diaries/${diaryId}`, {
    method: 'GET',
  });

  return normalizeDiaryDetail(diary);
}

export async function updateDiary(diaryId: number, payload: UpdateDiaryRequest) {
  getCurrentChildUserIdOrThrow();
  const diary = await request<DiaryEntryApiItem>(`/api/v1/diaries/${diaryId}`, {
    method: 'PATCH',
    body: JSON.stringify({
      content: payload.content,
      visibilityScope: payload.visibilityScope,
      sharingGroupIds: payload.visibilityScope === 'group' ? payload.sharingGroupIds ?? [] : [],
      emotion: payload.emotion,
    }),
  });

  return normalizeDiaryDetail(diary);
}

export async function deleteDiary(diaryId: number) {
  getCurrentChildUserIdOrThrow();
  await request<void>(`/api/v1/diaries/${diaryId}`, {
    method: 'DELETE',
  });
}

export async function fetchFamilyFeed() {
  getCurrentFamilyUserIdOrThrow();
  const items = await request<DiaryEntryApiItem[]>('/api/v1/diaries/family-feed', {
    method: 'GET',
  });

  return items.map(normalizeDiaryEntry);
}

export async function fetchReactionFeed() {
  getCurrentChildUserIdOrThrow();
  const items = await request<ReactionFeedApiItem[]>('/api/v1/reactions/feed', {
    method: 'GET',
  });

  return sortReactionFeed(items.map(normalizeReactionFeedItem));
}

export async function toggleDiaryHeart(diaryId: number) {
  getCurrentFamilyUserIdOrThrow();

  return request<ToggleDiaryHeartResponse>('/api/v1/reactions/hearts/toggle', {
    method: 'POST',
    body: JSON.stringify({
      diaryId,
    }),
  });
}

export async function fetchDiaryComments(diaryId: number) {
  const items = await request<DiaryCommentApiItem[]>('/api/v1/reactions/comments', {
    method: 'GET',
    query: {
      diaryId,
    },
  });

  return items.map(normalizeDiaryComment);
}

export async function createDiaryComment(payload: CreateDiaryCommentRequest) {
  getCurrentFamilyUserIdOrThrow();
  const comment = await request<DiaryCommentApiItem>('/api/v1/reactions/comments', {
    method: 'POST',
    body: JSON.stringify({
      diaryId: payload.diaryId,
      content: payload.content,
    }),
  });

  return normalizeDiaryComment(comment);
}

export async function createDiary(payload: CreateDiaryRequest) {
  const requestBody: CreateDiaryRequest = {
    content: payload.content,
    visibilityScope: payload.visibilityScope,
    sharingGroupIds: payload.visibilityScope === 'group' ? payload.sharingGroupIds ?? [] : [],
    writtenDate: payload.writtenDate,
    emotion: payload.emotion,
  };

  const diary = await request<DiaryEntryApiItem>('/api/v1/diaries', {
    method: 'POST',
    body: JSON.stringify(requestBody),
  });

  return normalizeDiaryEntry(diary);
}

export interface SharedDiaryItem {
  diaryId: number;
  authorNickname: string;
  contentPreview: string;
  visibilityScope: DiaryEntry['visibilityScope'];
  emotion: DiaryEntry['emotion'];
  writtenDate: string;
  createdAt: string;
}

export function fetchSharedDiaryFeed(page = 0) {
  getCurrentUserIdOrThrow();
  return request<SharedDiaryItem[]>('/api/v1/diaries/shared-feed', { query: { page } });
}
