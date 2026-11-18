import test from 'node:test';
import assert from 'node:assert/strict';
import { getSharedFeedGroupTabs } from '../src/utils/sharedFeedGroups.ts';

test('same-name groups become one tab while retaining every group identity and child', () => {
  const groups = [{ id: 1, name: '우리 가족', ownerName: '첫째' }, { id: 2, name: '우리 가족', ownerName: '둘째' },
    { id: 3, name: '친구', ownerName: '첫째' }, { id: 4, name: '친구', ownerName: '둘째' }];
  const original = structuredClone(groups);
  assert.deepEqual(getSharedFeedGroupTabs(groups), [
    { id: 1, name: '우리 가족', groupIds: [1, 2] }, { id: 3, name: '친구', groupIds: [3, 4] },
  ]);
  assert.deepEqual(groups, original);
});

test('different names remain separate and repeated group identities are not duplicated', () => {
  assert.deepEqual(getSharedFeedGroupTabs([{ id: 1, name: '우리 가족' }, { id: 1, name: '우리 가족' }, { id: 2, name: '함께 읽기' }]), [
    { id: 1, name: '우리 가족', groupIds: [1] }, { id: 2, name: '함께 읽기', groupIds: [2] },
  ]);
  assert.deepEqual(getSharedFeedGroupTabs([]), []);
});
