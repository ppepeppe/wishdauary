import assert from 'node:assert/strict';
import { test } from 'node:test';

globalThis.window = { location: { origin: 'http://localhost' } };
globalThis.localStorage = {
  getItem: () => JSON.stringify({ userId: 1, groupId: 2, appRole: 'child', accessToken: 'token' }),
};
const { fetchDiaryStatus, fetchTodayDiaryStatus } = await import('../src/services/api/diaryApi.ts');
const { fetchHomeSummary } = await import('../src/services/api/userApi.ts');

test('today API queries advance at Korean midnight without reloading modules', async (t) => {
  t.mock.timers.enable({ apis: ['Date'], now: new Date('2026-09-30T14:59:59Z') });
  const dates = [];
  globalThis.fetch = async (url) => {
    dates.push(new URL(url).searchParams.get('date'));
    return Response.json({ success: true, code: 'OK', message: '', data: {} });
  };
  await fetchDiaryStatus();
  await fetchTodayDiaryStatus();
  await fetchHomeSummary();
  t.mock.timers.tick(1000);
  await fetchDiaryStatus();
  await fetchTodayDiaryStatus();
  await fetchHomeSummary();
  assert.deepEqual(dates, [
    '2026-09-30', '2026-09-30', '2026-09-30',
    '2026-10-01', '2026-10-01', '2026-10-01',
  ]);
});

test('explicit past diary dates remain unchanged', async () => {
  globalThis.fetch = async (url) => {
    assert.equal(new URL(url).searchParams.get('date'), '2025-12-31');
    return Response.json({ exists: true, count: 1 });
  };
  assert.deepEqual(await fetchDiaryStatus('2025-12-31'), { exists: true, count: 1 });
});
