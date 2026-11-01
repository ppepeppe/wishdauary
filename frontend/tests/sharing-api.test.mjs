import assert from 'node:assert/strict';
import { beforeEach, test } from 'node:test';

globalThis.window = { location: { origin: 'http://localhost' } };
let session;
globalThis.localStorage = { getItem: () => session ? JSON.stringify(session) : null, removeItem: () => { session = null; } };
const { createDiary, updateDiary, fetchDiaryDetail, fetchSharedDiaryFeed } = await import('../src/services/api/diaryApi.ts');
const { saveSharingGroup, fetchSharingGroups, searchSharingUsers, deleteSharingGroup } = await import('../src/services/api/sharingApi.ts');
beforeEach(() => { session = { userId: 1, groupId: 1, appRole: 'child', accessToken: 'token' }; });

function respond(data) { return Response.json({ success: true, code: 'OK', message: '', data }); }

test('create and update preserve multiple group selections and response IDs', async () => {
  const calls = [];
  globalThis.fetch = async (url, init) => {
    calls.push([new URL(url).pathname, init.method, JSON.parse(init.body)]);
    return respond({ diaryId: 4, authorId: 1, content: '마음', visibilityScope: 'group', sharingGroupIds: [2, 5] });
  };
  const payload = { content: '마음', emotion: 'happy', visibilityScope: 'group', sharingGroupIds: [2, 5], writtenDate: '2026-09-27' };
  assert.deepEqual((await createDiary(payload)).sharingGroupIds, [2, 5]);
  assert.deepEqual((await updateDiary(4, payload)).sharingGroupIds, [2, 5]);
  assert.deepEqual(calls.map((call) => call[2].sharingGroupIds), [[2, 5], [2, 5]]);
  assert.equal(calls[0][1], 'POST');
  assert.equal(calls[1][1], 'PATCH');
});

test('switching to public or private clears old audience selections in the request', async () => {
  const bodies = [];
  globalThis.fetch = async (_url, init) => { bodies.push(JSON.parse(init.body)); return respond({ diaryId: 1 }); };
  await createDiary({ content: 'hi', visibilityScope: 'public', sharingGroupIds: [2], emotion: null, writtenDate: '2026-09-27' });
  await updateDiary(1, { content: 'hi', visibilityScope: 'private', sharingGroupIds: [2], emotion: null });
  assert.deepEqual(bodies.map((body) => body.sharingGroupIds), [[], []]);
});

test('group API serializes names, users, CRUD methods and search text', async () => {
  const calls = [];
  globalThis.fetch = async (url, init) => { calls.push([new URL(url), init]); return respond({ id: 3, name: '친구', memberIds: [7, 8], members: [] }); };
  const payload = { name: '친구', memberIds: [7, 8] };
  await saveSharingGroup(payload);
  await saveSharingGroup(payload, 3);
  await deleteSharingGroup(3);
  await searchSharingUsers('별 & 달');
  assert.equal(calls[0][1].method, 'POST');
  assert.equal(calls[1][1].method, 'PATCH');
  assert.equal(calls[1][0].pathname, '/api/v1/sharing-groups/3');
  assert.equal(calls[2][1].method, 'DELETE');
  assert.deepEqual(JSON.parse(calls[0][1].body), payload);
  assert.equal(calls[3][0].searchParams.get('query'), '별 & 달');
});

test('family users can read shared feeds and diary details but cannot manage groups', async () => {
  session.appRole = 'family';
  globalThis.fetch = async (url) => {
    if (new URL(url).pathname.endsWith('shared-feed')) return respond([]);
    return respond({ diaryId: 1, authorId: 2, content: 'public', visibilityScope: 'public' });
  };
  assert.deepEqual(await fetchSharedDiaryFeed(1), []);
  assert.equal((await fetchDiaryDetail(1)).visibilityScope, 'public');
  assert.throws(fetchSharingGroups, /아이 계정/);
  assert.throws(() => saveSharingGroup({ name: 'no', memberIds: [] }), /아이 계정/);
});

test('family feed preserves author identity and group visibility', async () => {
  session.appRole = 'family';
  const { fetchFamilyFeed } = await import('../src/services/api/diaryApi.ts');
  globalThis.fetch = async () => respond([{ diaryId: 5, authorId: 9, authorNickname: '별이', authorRelationLabel: null, contentPreview: '공유한 이야기', visibilityScope: 'group' }]);
  const [item] = await fetchFamilyFeed();
  assert.equal(item.actorUserId, 9);
  assert.equal(item.actorNickname, '별이');
  assert.equal(item.visibilityScope, 'group');
});
