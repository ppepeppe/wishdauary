import assert from 'node:assert/strict';
import { beforeEach, test } from 'node:test';
const storage = new Map();
globalThis.localStorage={getItem:key=>storage.get(key)??null,setItem:(key,value)=>storage.set(key,String(value)),removeItem:key=>storage.delete(key)};
globalThis.window={location:{origin:'http://localhost:5187'}};
const api=await import('../src/services/api/familyConnectionApi.ts');
const shop=await import('../src/services/api/shopApi.ts');
const key='wishdiary.auth.session';
function session(role){storage.set(key,JSON.stringify({userId:1,groupId:1,appRole:role,accessToken:'fixture'}))}
beforeEach(()=>storage.clear());

test('family requests target one child and cannot self-approve',async()=>{
 session('family');let calls=0;
 globalThis.fetch=async(url,init)=>{calls++;assert.equal(new URL(url).pathname,'/api/v1/family-connections/requests');assert.equal(init.method,'POST');assert.deepEqual(JSON.parse(init.body),{childUserId:42});return Response.json({requestId:5,status:'pending'})};
 assert.equal((await api.requestFamilyConnection(42)).status,'pending');
 assert.throws(()=>api.processFamilyConnection(5,'APPROVE'),/아이 계정/);assert.equal(calls,1);
});
test('only child accounts submit an approval decision',async()=>{
 session('child');
 globalThis.fetch=async(url,init)=>{assert.equal(new URL(url).pathname,'/api/v1/family-connections/requests/5');assert.equal(init.method,'PATCH');assert.deepEqual(JSON.parse(init.body),{action:'REJECT'});return Response.json({requestId:5,status:'rejected'})};
 assert.equal((await api.processFamilyConnection(5,'REJECT')).status,'rejected');
 assert.throws(()=>api.searchChildren('하루'),/가족 계정/);
});
test('family product uploads carry the selected child and price',async()=>{
 session('family');const payload={childUserId:42,title:'책',description:'함께 읽어요',cloverPrice:5,purchasePriceWon:10000};
 globalThis.fetch=async(url,init)=>{assert.equal(new URL(url).pathname,'/api/v1/shop/family-products');assert.deepEqual(JSON.parse(init.body),payload);return Response.json({...payload,productId:8,source:'family'})};
 assert.equal((await shop.createFamilyShopProduct(payload)).source,'family');
 session('child');await assert.rejects(shop.createFamilyShopProduct(payload),/가족 계정/);
});

test('family management loads child products even after pricing and edits their clovers',async()=>{
 session('family');let savedPrice=0;
 globalThis.fetch=async(url,init)=>{
  const path=new URL(url).pathname;
  if(path==='/api/v1/shop/managed-products')return Response.json([{productId:42,title:'책',cloverPrice:savedPrice,source:'custom',childUserId:7}]);
  assert.equal(path,'/api/v1/shop/products/42/price');assert.equal(init.method,'PATCH');
  assert.deepEqual(JSON.parse(init.body),{cloverPrice:8,purchasePriceWon:12000});savedPrice=8;
  return Response.json({productId:42,cloverPrice:savedPrice,source:'custom'});
 };
 assert.equal((await shop.fetchManagedShopProducts())[0].cloverPrice,0);
 await shop.setProductPrice(42,8,12000);
 assert.equal((await shop.fetchManagedShopProducts())[0].cloverPrice,8);
 session('child');await assert.rejects(shop.fetchManagedShopProducts(),/가족 계정/);await assert.rejects(shop.setProductPrice(42,9),/가족 계정/);
});
