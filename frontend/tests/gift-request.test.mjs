import assert from 'node:assert/strict';
import { test } from 'node:test';
import { getGiftRequestBlockReason } from '../src/utils/giftRequest.ts';

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
