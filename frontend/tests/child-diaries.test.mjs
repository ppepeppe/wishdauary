import assert from 'node:assert/strict';
import { test } from 'node:test';
import { getChildDiaries, getSelectedChildId } from '../src/utils/childDiaries.ts';

const children = [{userId: 10}, {userId: 20}];
test('child tabs accept only connected children and recover after removal', () => {
  assert.equal(getSelectedChildId(children, '20'), 20);
  for (const invalid of [null, 'nope', '30', '0']) assert.equal(getSelectedChildId(children, invalid), 10);
  assert.equal(getSelectedChildId([{userId: 10}], '20'), 10);
  assert.equal(getSelectedChildId([], '20'), undefined);
});
test('each child diary tab filters by identity before selecting recent entries', () => {
  const diaries = [{id:1,actorUserId:20},{id:2,actorUserId:20},{id:3,actorUserId:20},
    {id:4,actorUserId:10},{id:5,actorUserId:10},{id:6}];
  assert.deepEqual(getChildDiaries(diaries,10).slice(0,3).map(d=>d.id),[4,5]);
  assert.deepEqual(getChildDiaries(diaries,20).map(d=>d.id),[1,2,3]);
  assert.deepEqual(getChildDiaries(diaries),[]);
  assert.equal(diaries.length,6);
});

test('family calendar shows all entries for the selected child and written date', () => {
  const diaries = [
    { id: 1, actorUserId: 10, writtenDate: '2026-11-13', createdAt: '2026-11-18T10:00:00' },
    { id: 2, actorUserId: 10, writtenDate: '2026-11-13', createdAt: '2026-11-13T11:00:00' },
    { id: 3, actorUserId: 20, writtenDate: '2026-11-13' },
    { id: 4, actorUserId: 10, writtenDate: '2026-11-14' },
    { id: 5, actorUserId: 10, createdAt: '2026-11-13T12:00:00' },
  ];
  assert.deepEqual(getChildDiaries(diaries, 10, '2026-11-13').map(d => d.id), [1, 2, 5]);
  assert.deepEqual(getChildDiaries(diaries, 20, '2026-11-13').map(d => d.id), [3]);
  assert.deepEqual(getChildDiaries(diaries, 10, '2026-11-15'), []);
  assert.deepEqual(getChildDiaries(diaries, undefined, '2026-11-13'), []);
});

test('shared child selection restores remembered children and recovers after disconnection', () => {
  assert.equal(getSelectedChildId(children, null, '20'), 20);
  assert.equal(getSelectedChildId(children, '10', '20'), 10);
  assert.equal(getSelectedChildId(children, '999', '20'), 20);
  assert.equal(getSelectedChildId([{ userId: 10 }], null, '20'), 10);
  assert.equal(getSelectedChildId(children, null, 'broken'), 10);
  assert.equal(getSelectedChildId([], null, '20'), undefined);
});

test('remembered selection is scoped to the adult account and tolerates unavailable storage', async () => {
  const { readSelectedChild, saveSelectedChild } = await import('../src/services/session/selectedChildSession.ts');
  const values = new Map();
  globalThis.localStorage = { getItem: key => values.get(key) ?? null, setItem: (key, value) => values.set(key, value) };
  saveSelectedChild(1, 20);
  saveSelectedChild(2, 10);
  assert.equal(readSelectedChild(1), '20');
  assert.equal(readSelectedChild(2), '10');
  assert.equal(readSelectedChild(3), null);
  globalThis.localStorage = { getItem() { throw new Error('disabled'); }, setItem() { throw new Error('disabled'); } };
  assert.equal(readSelectedChild(1), null);
  assert.doesNotThrow(() => saveSelectedChild(1, 10));
});
