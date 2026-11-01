import assert from 'node:assert/strict';
import { test } from 'node:test';
import { getDefaultRouteForRole } from '../src/services/session/authSession.ts';

test('adult sign-in and role guards lead to the adult home', () => {
  assert.equal(getDefaultRouteForRole('family'), '/family-home');
});

test('children keep their existing home', () => {
  assert.equal(getDefaultRouteForRole('child'), '/home');
  assert.equal(getDefaultRouteForRole(), '/home');
});
