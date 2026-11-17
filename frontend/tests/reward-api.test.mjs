import assert from 'node:assert/strict';
import { test } from 'node:test';
const storage = new Map();
globalThis.localStorage = { getItem: key => storage.get(key) ?? null, setItem: (key,value) => storage.set(key,value), removeItem: key => storage.delete(key) };
globalThis.window = { location: { origin: 'http://localhost:5187' } };
const api = await import('../src/services/api/rewardApi.ts');
const {fetchCloverHistory} = await import('../src/services/api/cloverApi.ts');
function login(appRole) { storage.set('wishdiary.auth.session',JSON.stringify({userId:1,groupId:1,appRole,accessToken:'fixture'})); }

test('manual awards send an idempotency key, reason and selected child', async () => {
  login('family');
  const body = {childUserId:10,amount:3,reason:'정리를 잘했어요',requestKey:'542c7025-6533-4a13-a740-46ee5ee1c012'};
  globalThis.fetch = async (url,init) => {
    assert.equal(new URL(url).pathname,'/api/v1/clovers/awards'); assert.equal(init.method,'POST');
    assert.deepEqual(JSON.parse(init.body),body);
    assert.equal(new Headers(init.headers).get('Authorization'),'Bearer fixture');
    return Response.json({ledgerId:1,childUserId:10,amount:3,balance:5});
  };
  assert.equal((await api.awardClovers(body)).balance,5);
});
test('English requests specify the selected family and studied day without a client reward amount', async () => {
  login('child');
  globalThis.fetch = async (url,init) => {
    assert.equal(new URL(url).pathname,'/api/v1/english/requests');
    assert.deepEqual(JSON.parse(init.body),{receiverFamilyUserId:20,studyDate:'2026-11-17'});
    return Response.json({requestId:4,status:'pending'});
  };
  assert.equal((await api.requestEnglishReview(20,'2026-11-17')).status,'pending');
});
test('adult confirmation targets one request and keeps the approved server state', async () => {
  login('family');
  globalThis.fetch = async (url,init) => {
    assert.equal(new URL(url).pathname,'/api/v1/english/requests/4'); assert.equal(init.method,'PATCH');
    assert.deepEqual(JSON.parse(init.body),{action:'APPROVE'});
    return Response.json({requestId:4,status:'approved'});
  };
  assert.equal((await api.reviewEnglish(4,'APPROVE')).status,'approved');
});
test('credit history preserves the family reason and supports old diary rewards', async () => {
  login('child');
  globalThis.fetch = async () => Response.json({entries:[
    {ledgerId:1,amount:3,reason:'FAMILY_AWARD',note:'정리를 잘했어요',givenByName:'이모'},
    {ledgerId:2,amount:1,reason:'DIARY_WRITE'}]});
  const entries = await fetchCloverHistory();
  assert.equal(entries[0].note,'정리를 잘했어요'); assert.equal(entries[0].givenByName,'이모');
  assert.equal(entries[1].note,null); assert.equal(entries[1].givenByName,null);
});
test('role-specific reward calls reject the wrong client role before sending', () => {
  login('child'); assert.throws(()=>api.awardClovers({}),/가족/); assert.throws(()=>api.fetchEnglishReviews(),/가족/);
  login('family'); assert.throws(()=>api.fetchTodayEnglish(),/아이/); assert.throws(()=>api.requestEnglishReview(1,'2026-11-17'),/아이/);
  storage.clear(); assert.throws(()=>api.fetchTodayEnglish(),/로그인/);
});
