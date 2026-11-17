import assert from 'node:assert/strict';
import { beforeEach, test } from 'node:test';
const storage = new Map();
globalThis.localStorage = { getItem:k=>storage.get(k)??null,setItem:(k,v)=>storage.set(k,v),removeItem:k=>storage.delete(k) };
globalThis.window = { location:{origin:'http://localhost:5187'} };
const api = await import('../src/services/api/diaryApi.ts');
const photo = 'data:image/jpeg;base64,cGhvdG8=';
const content = { content:'오늘 이야기',visibilityScope:'private',emotion:null,writtenDate:'2026-11-17' };
beforeEach(()=>storage.set('wishdiary.auth.session',JSON.stringify({userId:1,groupId:1,appRole:'child',accessToken:'fixture'})));
test('diary creation sends the selected photo with the text and audience',async()=>{
 globalThis.fetch=async(url,init)=>{assert.equal(init.method,'POST');assert.equal(JSON.parse(init.body).imageData,photo);return Response.json({diaryId:3})};
 await api.createDiary({...content,imageData:photo});
});
test('diary details expose photos while older entries have no attachment',async()=>{
 globalThis.fetch=async()=>Response.json({diaryId:3,content:'내용',imageData:photo});
 assert.equal((await api.fetchDiaryDetail(3)).imageData,photo);
 globalThis.fetch=async()=>Response.json({diaryId:3,content:'내용'});
 assert.equal((await api.fetchDiaryDetail(3)).imageData,null);
});
test('photo edits distinguish replacement, explicit removal and unchanged attachments',async()=>{
 for(const change of [{},{imageData:photo},{removePhoto:true}]){
  globalThis.fetch=async(url,init)=>{const body=JSON.parse(init.body);assert.equal(body.imageData,change.imageData);assert.equal(body.removePhoto,change.removePhoto);return Response.json({diaryId:3,content:'내용',imageData:change.imageData??null})};
  await api.updateDiary(3,{...content,...change});
 }
});
