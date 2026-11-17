import test from 'node:test';
import assert from 'node:assert/strict';
import { getNextStreakReward } from '../src/utils/streakReward.ts';

test('shows the nearest upcoming streak bonus and advances at each milestone', () => {
  for (const days of [0, 1, 2]) assert.deepEqual(getNextStreakReward(days), { days: 3, clovers: 1 });
  for (const days of [3, 4]) assert.deepEqual(getNextStreakReward(days), { days: 5, clovers: 2 });
  for (const days of [5, 6]) assert.deepEqual(getNextStreakReward(days), { days: 7, clovers: 3 });
});

test('does not promise an additional bonus after the last milestone', () => {
  for (const days of [7, 8, 14, 100]) assert.equal(getNextStreakReward(days), null);
});
