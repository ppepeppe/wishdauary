import test from 'node:test';
import assert from 'node:assert/strict';
import { loadSharedDiaryDates, getSharedDiariesOnDate } from '../src/utils/sharedDiaryDates.ts';

test('calendar includes older pages and filters by written date rather than creation date', async () => {
  const pages = [Array.from({ length: 30 }, (_, id) => ({ diaryId: id, writtenDate: '2026-11-18' })),
    [{ diaryId: 31, writtenDate: '2026-11-13', createdAt: '2026-11-18T12:00:00' },
      { diaryId: 32, writtenDate: '2026-11-13' }]];
  const calls = [];
  const items = await loadSharedDiaryDates(async page => { calls.push(page); return pages[page]; }, () => true);
  assert.deepEqual(calls, [0, 1]);
  assert.deepEqual(getSharedDiariesOnDate(items, '2026-11-13').map(item => item.diaryId), [31, 32]);
  assert.deepEqual(getSharedDiariesOnDate(items, '2026-11-14'), []);
});

test('changing group stops loading the previous group pages', async () => {
  let active = true;
  let calls = 0;
  const items = await loadSharedDiaryDates(async () => { calls++; active = false; return Array(30).fill({}); }, () => active);
  assert.equal(calls, 1);
  assert.deepEqual(items, []);
});
