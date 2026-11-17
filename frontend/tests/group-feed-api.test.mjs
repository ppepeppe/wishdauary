import assert from 'node:assert/strict';
import {test} from 'node:test';
const storage = new Map();
globalThis.localStorage={getItem:key=>storage.get(key)??null,setItem:(key,value)=>storage.set(key,value),removeItem:key=>storage.delete(key)};
globalThis.window={location:{origin:'http://localhost:5187'}};
const {fetchSharedDiaryFeed,fetchSharedFeedGroups}=await import('../src/services/api/diaryApi.ts');
function login(){storage.set('wishdiary.auth.session',JSON.stringify({userId:1,groupId:1,appRole:'child',accessToken:'fixture'}));}
test('group selection is sent to the server together with pagination',async()=>{
 login();globalThis.fetch=async(url,init)=>{
  const parsed=new URL(url);assert.equal(parsed.pathname,'/api/v1/diaries/shared-feed');
  assert.equal(parsed.searchParams.get('groupId'),'14');assert.equal(parsed.searchParams.get('page'),'2');
  assert.equal(new Headers(init.headers).get('Authorization'),'Bearer fixture');return Response.json([]);
 };assert.deepEqual(await fetchSharedDiaryFeed(2,14),[]);
});
test('visible group tabs preserve their identity and owner name',async()=>{
 login();const groups=[{id:14,name:'친구',ownerName:'서아'}];
 globalThis.fetch=async(url)=>{assert.equal(new URL(url).pathname,'/api/v1/diaries/shared-feed/groups');return Response.json(groups);};
 assert.deepEqual(await fetchSharedFeedGroups(),groups);
 storage.clear();assert.throws(()=>fetchSharedFeedGroups(),/로그인/);
});
