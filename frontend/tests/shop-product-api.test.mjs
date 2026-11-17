import assert from 'node:assert/strict';
import { test } from 'node:test';

const storage = new Map();
globalThis.localStorage = { getItem: key => storage.get(key) ?? null, setItem: (key, value) => storage.set(key, value), removeItem: key => storage.delete(key) };
globalThis.window = { location: { origin: 'http://localhost:5187' } };
const { fetchShopProducts } = await import('../src/services/api/shopApi.ts');

test('personal gift lists retain the poster and designated family from the API', async () => {
  storage.set('wishdiary.auth.session', JSON.stringify({ userId: 10, groupId: 1, appRole: 'child', accessToken: 'fixture' }));
  globalThis.fetch = async () => Response.json([
    { productId: 1, source: 'custom', postedByName: ' 서아 ', pricedByName: '삼촌', cloverPrice: 10 },
    { productId: 2, source: 'family', postedByName: '이모', pricedByName: null, cloverPrice: 15 },
    { productId: 3, source: 'family', cloverPrice: 20 },
  ]);
  const products = await fetchShopProducts();
  assert.deepEqual(products.map(p => [p.postedByName, p.pricedByName]), [['서아', '삼촌'], ['이모', null], [null, null]]);
});
