import assert from 'node:assert/strict';
import { test } from 'node:test';
import { getCommonShopProducts, getChildWishProducts, getProductListPath, getCatalogListing } from '../src/utils/shopNavigation.ts';

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

test('wishlist tabs separate the childs own applications from family suggestions without exposing another childs gifts', () => {
  assert.deepEqual(getChildWishProducts(products, 10, 'custom').map(item => item.productId), [2]);
  assert.deepEqual(getChildWishProducts(products, 10, 'family').map(item => item.productId), [4]);
  assert.deepEqual(getChildWishProducts(products, 20, 'custom').map(item => item.productId), [3]);
  assert.deepEqual(getChildWishProducts(products, 20, 'family').map(item => item.productId), [5]);
  assert.deepEqual(getChildWishProducts(products, undefined, 'family'), []);
});

test('unpriced applications remain outside both wishlist tabs until the family sets clovers', () => {
  const waiting = [{ productId: 7, source: 'custom', childUserId: 10, cloverPrice: 0 },
    { productId: 8, source: 'family', childUserId: 10, cloverPrice: 0 }];
  assert.deepEqual(getChildWishProducts(waiting, 10, 'custom'), []);
  assert.deepEqual(getChildWishProducts(waiting, 10, 'family'), []);
});

test('detail and request links return to the appropriate product list', () => {
  assert.equal(getProductListPath('preset'), '/shop');
  assert.equal(getProductListPath('custom'), '/shop/wishlist');
  assert.equal(getProductListPath('family'), '/shop/wishlist?category=family');
  assert.equal(getProductListPath(), '/shop');
});

const catalog = [
  { productId: 3, title: '그림책', description: '바다 이야기', source: 'preset', cloverPrice: 8, createdAt: '2026-11-01' },
  { productId: 1, title: '색연필', description: '그림을 그려요', source: 'preset', cloverPrice: 3, createdAt: '2026-11-02' },
  { productId: 2, title: '그림책', description: '가족 이야기', source: 'custom', cloverPrice: 1 },
];

test('catalog search matches title or description while keeping personal gifts out', () => {
  assert.deepEqual(getCatalogListing(catalog, ' 그림 ', 'clovers-asc').map(p => p.productId), [1, 3]);
  assert.deepEqual(getCatalogListing(catalog, '바다', 'clovers-asc').map(p => p.productId), [3]);
  assert.deepEqual(getCatalogListing(catalog, '없는 상품', 'clovers-asc'), []);
});

test('catalog sorting uses clovers or registration time without mutating the loaded products', () => {
  assert.deepEqual(getCatalogListing(catalog, '', 'clovers-desc').map(p => p.productId), [3, 1]);
  assert.deepEqual(getCatalogListing(catalog, '', 'newest').map(p => p.productId), [1, 3]);
  assert.deepEqual(catalog.map(p => p.productId), [3, 1, 2]);
});
