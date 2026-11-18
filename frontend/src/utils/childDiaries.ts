import { getDateKey } from '../constants/date';
import type { DiaryEntry } from '../types/diary';

export function getSelectedChildId(children: { userId: number }[], requested: string | null): number | undefined {
  const id = Number(requested);
  return children.some(child => child.userId === id) ? id : children[0]?.userId;
}

export function getChildDiaries(diaries: DiaryEntry[], childId?: number, writtenDate?: string) {
  return childId === undefined ? [] : diaries.filter(diary => diary.actorUserId === childId
    && (writtenDate === undefined || getDateKey(diary.writtenDate ?? diary.createdAt) === writtenDate));
}
