export type DiaryVisibilityScope = 'private' | 'family' | 'friends' | 'public' | 'group';
export type DiaryEmotion = 'happy' | 'sad' | 'angry' | 'excited' | 'tired';

export interface DiaryEntry {
  id: number;
  content: string;
  contentPreview: string;
  visibilityScope: DiaryVisibilityScope;
  sharingGroupIds?: number[];
  createdAt: string;
  writtenDate?: string;
  emotion?: DiaryEmotion | null;
  hearted?: boolean;
  heartCount?: number;
  commentCount?: number;
  actorUserId?: number;
  actorNickname?: string | null;
  actorRelationLabel?: string | null;
}

export interface DiaryDetail extends DiaryEntry {
  authorId?: number;
  content: string;
  writtenDate: string;
}

export interface TodayDiaryStatus {
  exists: boolean;
  count: number;
  diaryId?: number;
  emotion?: DiaryEmotion | null;
  heartCount: number;
  commentCount: number;
}

export interface CreateDiaryRequest {
  content: string;
  visibilityScope: DiaryVisibilityScope;
  sharingGroupIds?: number[];
  emotion: DiaryEmotion | null;
  writtenDate: string;
}

export interface UpdateDiaryRequest {
  content: string;
  visibilityScope: DiaryVisibilityScope;
  sharingGroupIds?: number[];
  emotion: DiaryEmotion | null;
}

export interface ToggleDiaryHeartResponse {
  diaryId: number;
  hearted: boolean;
  heartCount: number;
}

export interface DiaryComment {
  id: number;
  content: string;
  createdAt?: string;
  authorName?: string;
  actorUserId?: number;
  actorNickname?: string | null;
  actorRelationLabel?: string | null;
}

export interface CreateDiaryCommentRequest {
  diaryId: number;
  content: string;
}

export type ReactionFeedType = 'heart' | 'comment';

export interface ReactionFeedItem {
  id: number;
  type: ReactionFeedType;
  content?: string | null;
  createdAt?: string | null;
  actorUserId?: number;
  actorNickname?: string | null;
  actorRelationLabel?: string | null;
}
