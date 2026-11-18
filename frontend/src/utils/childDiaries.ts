import { getDateKey } from '../constants/date';
import type { DiaryEntry } from '../types/diary';

export function getSelectedChildId(children: { userId: number }[], requested: string | null, remembered: string | null = null): number | undefined {
  const id = Number(requested);
  if (children.some(child => child.userId === id)) return id;
  const previous = Number(remembered);
  return children.some(child => child.userId === previous) ? previous : children[0]?.userId;
}

export function getChildDiaries(diaries: DiaryEntry[], childId?: number, writtenDate?: string) {
  return childId === undefined ? [] : diaries.filter(diary => diary.actorUserId === childId
    && (writtenDate === undefined || getDateKey(diary.writtenDate ?? diary.createdAt) === writtenDate));
}
