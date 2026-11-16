import assert from 'node:assert/strict';
import { beforeEach, test } from 'node:test';

const storage = new Map();
globalThis.localStorage = { getItem: key => storage.get(key) ?? null, setItem: (key, value) => storage.set(key, value), removeItem: key => storage.delete(key) };
globalThis.window = { location: { origin: 'http://localhost:5187' } };
const api = await import('../src/services/api/shopApi.ts');
const receivedAt = '2026-11-16T09:30:00Z';
const receipt = { notificationId: 5, productId: 9, status: 'received', receivedAt, cloverPrice: 2 };
function session(appRole) { storage.set('wishdiary.auth.session', JSON.stringify({ userId: 1, groupId: 1, appRole, accessToken: 'fixture' })); }
beforeEach(() => session('child'));

test('a child confirms receipt without sending a price, balance or identity override', async () => {
  globalThis.fetch = async (url, init) => {
    assert.equal(new URL(url).pathname, '/api/v1/shop/gift-requests/5/receipt');
    assert.equal(init.method, 'PATCH');
    assert.equal(init.body, undefined);
    return Response.json(receipt);
  };
  const result = await api.confirmGiftReceipt(5);
  assert.equal(result.status, 'received');
  assert.equal(result.receivedAt, receivedAt);
});

test('child and family histories preserve the received state and original date', async () => {
  globalThis.fetch = async () => Response.json([receipt]);
  assert.equal((await api.fetchSentGiftRequests())[0].receivedAt, receivedAt);
  session('family');
  const [item] = await api.fetchReceivedGiftRequests();
  assert.equal(item.status, 'received');
  assert.equal(item.receivedAt, receivedAt);
});

test('family accounts cannot submit receipt confirmation and older records stay compatible', async () => {
  session('family');
  await assert.rejects(api.confirmGiftReceipt(5), /아이 계정/);
  session('child');
  globalThis.fetch = async () => Response.json([{ notificationId: 5, status: 'approved' }]);
  const [item] = await api.fetchSentGiftRequests();
  assert.equal(item.status, 'approved');
  assert.equal(item.receivedAt, null);
});
