import type { SharedFeedGroup } from '../services/api/diaryApi';

export function getSharedFeedGroupTabs(groups: SharedFeedGroup[]) {
  const tabs = new Map<string, { id: number; name: string; groupIds: number[] }>();
  for (const group of groups) {
    const name = group.name.trim().normalize('NFC');
    const tab = tabs.get(name);
    if (tab) {
      if (!tab.groupIds.includes(group.id)) tab.groupIds.push(group.id);
    } else tabs.set(name, { id: group.id, name, groupIds: [group.id] });
  }
  return [...tabs.values()];
}
