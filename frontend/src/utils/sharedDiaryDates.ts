import { getDateKey } from '../constants/date';
import type { SharedDiaryItem } from '../services/api/diaryApi';

// The API pages the whole feed, so collect every page before filtering by date.
export async function loadSharedDiaryDates(
  loadPage: (page: number) => Promise<SharedDiaryItem[]>,
  isActive: () => boolean,
) {
  const items: SharedDiaryItem[] = [];
  for (let page = 0; isActive(); page += 1) {
    const batch = await loadPage(page);
    if (!isActive()) return [];
    items.push(...batch);
    if (batch.length < 30) return items;
  }
  return [];
}

export function getSharedDiariesOnDate(items: SharedDiaryItem[], date: string) {
  return items.filter(item => getDateKey(item.writtenDate ?? item.createdAt) === date);
}
