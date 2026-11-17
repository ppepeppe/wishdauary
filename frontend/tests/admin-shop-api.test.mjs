import assert from 'node:assert/strict';
import { beforeEach, test } from 'node:test';

const storage = new Map();
globalThis.localStorage = { getItem: key => storage.get(key) ?? null, setItem: (key, value) => storage.set(key, value), removeItem: key => storage.delete(key) };
globalThis.window = { location: { origin: 'http://localhost:5187' } };
const { fetchCatalogProducts, createCatalogProduct, updateCatalogPrice, deleteCatalogProduct } = await import('../src/services/api/adminShopApi.ts');
const { fetchMyProfile } = await import('../src/services/api/userApi.ts');
beforeEach(() => storage.set('wishdiary.auth.session', JSON.stringify({ userId: 1, groupId: 1, appRole: 'family', accessToken: 'fixture' })));

test('catalog registration preserves photos, clovers and optional purchase details without a child target', async () => {
  const payload = { title: '보드게임', description: '함께 골라요.', cloverPrice: 10, purchasePriceWon: 12000,
    imageData: 'data:image/jpeg;base64,aGVsbG8=', productUrl: 'https://example.com/gift' };
  globalThis.fetch = async (url, init) => {
    assert.equal(new URL(url).pathname, '/api/v1/admin/shop/products');
    assert.equal(init.method, 'POST');
    assert.equal(new Headers(init.headers).get('Authorization'), 'Bearer fixture');
    assert.deepEqual(JSON.parse(init.body), payload);
    return Response.json({ productId: 7, ...payload, source: 'preset', childUserId: null });
  };
  assert.equal((await createCatalogProduct(payload)).source, 'preset');
});

test('catalog management uses existing login without child, family or administrator authorization checks', async () => {
  for (const appRole of ['child', 'family']) {
    storage.set('wishdiary.auth.session', JSON.stringify({ userId: 1, groupId: 1, appRole, accessToken: 'fixture' }));
    globalThis.fetch = async url => {
      assert.equal(new URL(url).pathname, '/api/v1/admin/shop/products');
      return Response.json([{ productId: 7, source: 'preset' }]);
    };
    assert.equal((await fetchCatalogProducts())[0].productId, 7);
  }
  storage.clear();
  assert.throws(() => fetchCatalogProducts(), /로그인/);
});

test('profile exposes the database role and older or unknown roles default to regular users', async () => {
  for (const [userRole, expected] of [['admin', 'admin'], ['user', 'user'], [undefined, 'user'], ['unknown', 'user']]) {
    globalThis.fetch = async () => Response.json({ userId: 1, appRole: 'family', userRole });
    const profile = await fetchMyProfile();
    assert.equal(profile.appRole, 'family');
    assert.equal(profile.userRole, expected);
  }
});

test('price-only updates preserve product metadata and deletion addresses the selected product', async () => {
  const calls = [];
  globalThis.fetch = async (url, init) => {
    calls.push({ path: new URL(url).pathname, method: init.method, body: init.body });
    assert.equal(new Headers(init.headers).get('Authorization'), 'Bearer fixture');
    return Response.json({ success: true, code: 'SUCCESS', message: 'ok', data: init.method === 'PATCH' ? { productId: 12, cloverPrice: 15 } : null });
  };
  assert.equal((await updateCatalogPrice(12, 15)).cloverPrice, 15);
  await deleteCatalogProduct(12);
  assert.deepEqual(calls, [
    { path: '/api/v1/admin/shop/products/12', method: 'PATCH', body: JSON.stringify({ cloverPrice: 15 }) },
    { path: '/api/v1/admin/shop/products/12', method: 'DELETE', body: undefined },
  ]);
});
