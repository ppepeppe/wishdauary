import assert from 'node:assert/strict';
import { test } from 'node:test';
import { getNativeBackAction } from '../src/utils/nativeNavigation.ts';

test('Android back returns from a detail page using app history', () => {
  assert.equal(getNativeBackAction('/shop/products/1', 2, true), 'back');
});

test('Android back on a directly opened subpage returns home', () => {
  assert.equal(getNativeBackAction('/shop/catalog', 0, true), 'home');
  assert.equal(getNativeBackAction('/shop/catalog', 2, false), 'home');
});

test('Android back minimizes the app at its entry screens', () => {
  for (const path of ['/', '/home', '/family-home', '/login']) {
    assert.equal(getNativeBackAction(path, 3, true), 'minimize');
  }
});

test('the adult diary list returns to the new home when opened directly', () => {
  assert.equal(getNativeBackAction('/family-feed', 0, true), 'home');
  assert.equal(getNativeBackAction('/family-feed', 1, true), 'back');
});
