import type { DiaryVisibilityScope } from '../types/diary';

export function selectDiaryAudience(scope: DiaryVisibilityScope, groupIds: number[]) {
  return { scope, groupIds: scope === 'group' ? groupIds : [] };
}

export function toggleDiaryAudienceGroup(scope: DiaryVisibilityScope, groupIds: number[], groupId: number, checked: boolean) {
  const selected = scope === 'group' ? groupIds : [];
  return { scope: 'group' as const, groupIds: checked
    ? [...new Set([...selected, groupId])]
    : selected.filter(id => id !== groupId) };
}
