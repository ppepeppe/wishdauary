import assert from 'node:assert/strict';
import { test } from 'node:test';
import { getCommonShopProducts, getChildWishProducts, getProductListPath } from '../src/utils/shopNavigation.ts';

const products = [
  { productId: 1, source: 'preset', childUserId: null },
  { productId: 2, source: 'custom', childUserId: 10, cloverPrice: 5 },
  { productId: 3, source: 'custom', childUserId: 20, cloverPrice: 5 },
  { productId: 4, source: 'family', childUserId: 10, cloverPrice: 5 },
  { productId: 5, source: 'family', childUserId: 20, cloverPrice: 5 },
  { productId: 6, source: 'custom', childUserId: 10, cloverPrice: 0 },
];

test('the common shop contains shared catalog products without personal gifts', () => {
  assert.deepEqual(getCommonShopProducts(products).map(item => item.productId), [1]);
});

test('each child wishlist includes their own wishes and gifts chosen for them', () => {
  assert.deepEqual(getChildWishProducts(products, 10).map(item => item.productId), [2, 4]);
  assert.deepEqual(getChildWishProducts(products, 20).map(item => item.productId), [3, 5]);
  assert.deepEqual(getChildWishProducts(products, undefined), []);
});

test('detail and request links return to the appropriate product list', () => {
  assert.equal(getProductListPath('preset'), '/shop');
  assert.equal(getProductListPath('custom'), '/shop/wishlist');
  assert.equal(getProductListPath('family'), '/shop/wishlist');
  assert.equal(getProductListPath(), '/shop');
});
