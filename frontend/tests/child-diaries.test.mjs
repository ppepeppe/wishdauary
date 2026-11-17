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
