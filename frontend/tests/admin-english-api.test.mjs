import assert from 'node:assert/strict';
import {test} from 'node:test';
const storage = new Map();
globalThis.localStorage = {getItem:key=>storage.get(key)??null,setItem:(key,value)=>storage.set(key,value),removeItem:key=>storage.delete(key)};
globalThis.window = {location:{origin:'http://localhost:5187'}};
const {fetchEnglishContents,createEnglishContent,updateEnglishContent} = await import('../src/services/api/adminEnglishApi.ts');
function login() {storage.set('wishdiary.auth.session',JSON.stringify({userId:1,groupId:1,appRole:'family',accessToken:'fixture'}));}

test('English administration preserves the daily selection and stored content metadata',async()=>{
  login();
  const overview={today:'2026-11-17',todayContentId:15,contents:[{contentId:15,sentence:'I can sing.',meaning:'나는 노래할 수 있어요.',version:2}]};
  globalThis.fetch=async(url,init)=>{
    assert.equal(new URL(url).pathname,'/api/v1/admin/english/contents');
    assert.equal(new Headers(init.headers).get('Authorization'),'Bearer fixture');
    return Response.json(overview);
  };
  assert.deepEqual(await fetchEnglishContents(),overview);
});
test('English create and update send the sentence, meaning and optimistic update version',async()=>{
  login();const calls=[];const body={sentence:'I can sing.',meaning:'나는 노래할 수 있어요.'};
  globalThis.fetch=async(url,init)=>{calls.push({path:new URL(url).pathname,method:init.method,body:JSON.parse(init.body)});return Response.json({contentId:31,...body,version:1});};
  await createEnglishContent(body);await updateEnglishContent(31,body,0);
  assert.deepEqual(calls,[{path:'/api/v1/admin/english/contents',method:'POST',body},
    {path:'/api/v1/admin/english/contents/31',method:'PATCH',body:{...body,version:0}}]);
});
test('English content conflicts are reported to the editor and anonymous calls require login',async()=>{
  login();globalThis.fetch=async()=>Response.json({code:'REWARD_CONFLICT',message:'다른 곳에서 수정한 문장이에요.'},{status:409});
  await assert.rejects(updateEnglishContent(31,{sentence:'Hello',meaning:'안녕'},0),/다른 곳에서/);
  storage.clear();assert.throws(()=>fetchEnglishContents(),/로그인/);
});
