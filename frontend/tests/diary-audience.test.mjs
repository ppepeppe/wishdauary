import test from 'node:test';
import assert from 'node:assert/strict';
import { selectDiaryAudience, toggleDiaryAudienceGroup } from '../src/utils/diaryAudience.ts';

test('private, family and public choices clear group selections instead of combining audiences', () => {
  for (const scope of ['private', 'family', 'public']) {
    assert.deepEqual(selectDiaryAudience(scope, [1, 2]), { scope, groupIds: [] });
  }
  assert.deepEqual(selectDiaryAudience('group', [1, 2]), { scope: 'group', groupIds: [1, 2] });
});

test('choosing a group activates group sharing without reviving old hidden selections', () => {
  assert.deepEqual(toggleDiaryAudienceGroup('private', [1], 2, true), { scope: 'group', groupIds: [2] });
  assert.deepEqual(toggleDiaryAudienceGroup('family', [1], 2, true), { scope: 'group', groupIds: [2] });
  assert.deepEqual(toggleDiaryAudienceGroup('public', [1], 2, true), { scope: 'group', groupIds: [2] });
});

test('group sharing supports multiple groups, deselection and no duplicate selections', () => {
  assert.deepEqual(toggleDiaryAudienceGroup('group', [1], 2, true), { scope: 'group', groupIds: [1, 2] });
  assert.deepEqual(toggleDiaryAudienceGroup('group', [1, 2], 1, false), { scope: 'group', groupIds: [2] });
  assert.deepEqual(toggleDiaryAudienceGroup('group', [1], 1, true), { scope: 'group', groupIds: [1] });
  assert.deepEqual(toggleDiaryAudienceGroup('group', [1], 1, false), { scope: 'group', groupIds: [] });
});
