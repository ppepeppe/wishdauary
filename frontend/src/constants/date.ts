export function getTodayDateInKorea() {
  return new Intl.DateTimeFormat('sv-SE', {
    timeZone: 'Asia/Seoul',
  }).format(new Date());
}

export function getDateKey(value?: string | null) {
  if (!value) {
    return '';
  }

  const matchedDate = value.match(/^\d{4}-\d{2}-\d{2}/)?.[0];
  return matchedDate ?? '';
}

export function formatKoreanDate(value: string, includeYear = true) {
  const dateKey = getDateKey(value);

  if (!dateKey) {
    return '';
  }

  return new Intl.DateTimeFormat('ko-KR', {
    timeZone: 'Asia/Seoul',
    ...(includeYear ? { year: 'numeric' as const } : {}),
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  }).format(new Date(`${dateKey}T00:00:00+09:00`));
}
