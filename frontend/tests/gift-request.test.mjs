import assert from 'node:assert/strict';
import { test } from 'node:test';
import { getGiftRecipient, getGiftRequestBlockReason, getPendingGoalRequest, getPendingGiftReviewCount } from '../src/utils/giftRequest.ts';

test('gift review count includes unpriced wishes and pending redemptions, excluding completed requests', () => {
  const requests = [{ status: 'pending' }, { status: 'approved' }, { status: 'rejected' }, { status: 'received' }];
  const products = [{ cloverPrice: 0 }, { cloverPrice: 10 }];
  assert.equal(getPendingGiftReviewCount(requests, products), 2);
  assert.equal(getPendingGiftReviewCount([{ status: 'approved' }], [{ cloverPrice: 10 }]), 0);
  assert.equal(getPendingGiftReviewCount([], []), 0);
});

const recipients = [{ userId: 1, nickname: '엄마' }, { userId: 2, nickname: '아빠' }];

test('a goal waits only for this childs pending redemption of the same product', () => {
  const pending = { productId: 4, requesterChildUserId: 7, status: 'pending', cloverPrice: 2 };
  assert.equal(getPendingGoalRequest([pending], 4, 7), pending);
  assert.equal(getPendingGoalRequest([pending], 5, 7), undefined);
  assert.equal(getPendingGoalRequest([pending], 4, 8), undefined);
  for (const status of ['approved', 'rejected', 'received']) {
    assert.equal(getPendingGoalRequest([{ ...pending, status }], 4, 7), undefined);
  }
  assert.equal(getPendingGoalRequest([pending], undefined, 7), undefined);
});

test('a personal wish keeps the family member who received and priced it as the default', () => {
  assert.equal(getGiftRecipient(recipients, { receiverFamilyUserId: 2 }).userId, 2);
});

test('an explicit connected recipient takes priority over the original wish recipient', () => {
  assert.equal(getGiftRecipient(recipients, { receiverFamilyUserId: 2 }, 1).userId, 1);
});

test('common gifts and disconnected selections fall back to an available family member', () => {
  assert.equal(getGiftRecipient(recipients, {}, 99).userId, 1);
  assert.equal(getGiftRecipient(recipients, { receiverFamilyUserId: 99 }).userId, 1);
  assert.equal(getGiftRecipient([], { receiverFamilyUserId: 2 }), null);
});

test('a priced gift can be requested with exactly enough clovers and a connected family', () => {
  assert.equal(getGiftRequestBlockReason({ cloverPrice: 5 }, 5, true), null);
});

test('unpriced gifts explain that the family needs to set a price', () => {
  assert.equal(getGiftRequestBlockReason({ cloverPrice: 0 }, 10, true), '가족이 필요한 클로버를 정하고 있어요.');
});

test('insufficient clovers show the exact amount still needed', () => {
  assert.equal(getGiftRequestBlockReason({ cloverPrice: 10 }, 3, true), '클로버가 7개 더 필요해요.');
});

test('a funded request still requires a connected family', () => {
  assert.equal(getGiftRequestBlockReason({ cloverPrice: 5 }, 10, false), '그룹 관리에서 선물을 요청할 가족을 추가해 주세요.');
});
