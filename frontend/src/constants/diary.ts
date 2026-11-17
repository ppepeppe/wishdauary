import type { DiaryVisibilityScope } from '../types/diary';

export const DIARY_VISIBILITY_OPTIONS: Array<{
  value: DiaryVisibilityScope;
  label: string;
}> = [
  { value: 'private', label: '나만 보기' },
  { value: 'family', label: '가족 공개' },
  { value: 'public', label: '전체공개' },
  { value: 'group', label: '그룹 공개' },
  { value: 'friends', label: '이전 친구 공개' },
];

export function getVisibilityScopeLabel(scope?: string | null) {
  const option = DIARY_VISIBILITY_OPTIONS.find((candidate) => candidate.value === scope);
  return option?.label ?? '나만 보기';
}

export const DIARY_PLACEHOLDER = '오늘은 어떤 일이 있었나요?';
