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

export const DIARY_PLACEHOLDER = `그날 있었던 일, 그냥 떠오르는 거 아무거나 적어도 좋아요 🙂
짧아도 괜찮고,
꼭 맞게 쓰지 않아도 괜찮아요.
그날 느꼈던 솔직한 마음을 적어보세요 ✨`;
