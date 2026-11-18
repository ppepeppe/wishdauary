import test from 'node:test';
import assert from 'node:assert/strict';
import { getChildGiftRequests, getChildPricingRequests, getChildEnglishRequests } from '../src/utils/childRequests.ts';

test('switching children filters pending gifts, pricing and English independently', () => {
  const gifts = [{ id: 1, requesterChildUserId: 10, status: 'pending' }, { id: 2, requesterChildUserId: 20, status: 'pending' }, { id: 3, requesterChildUserId: 10, status: 'approved' }];
  const pricing = [{ id: 4, childUserId: 10, cloverPrice: 0 }, { id: 5, childUserId: 20, cloverPrice: 0 }, { id: 6, childUserId: 10, cloverPrice: 5 }, { id: 7, childUserId: null, cloverPrice: 0 }];
  const english = [{ id: 8, childUserId: 10, status: 'pending' }, { id: 9, childUserId: 20, status: 'pending' }, { id: 10, childUserId: 10, status: 'rejected' }];
  for (const [child, ids] of [[10, [1, 4, 8]], [20, [2, 5, 9]], [30, []]]) {
    const shown = [...getChildGiftRequests(gifts, child), ...getChildPricingRequests(pricing, child), ...getChildEnglishRequests(english, child)];
    assert.deepEqual(shown.map(item => item.id), ids);
  }
  assert.deepEqual(getChildGiftRequests(gifts), []);
  assert.deepEqual(getChildPricingRequests(pricing), []);
  assert.equal(gifts.length, 3);
  assert.equal(pricing.length, 4);
});
