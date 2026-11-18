import assert from 'node:assert/strict';
import { test } from 'node:test';
globalThis.window = { location: { origin: 'http://localhost' } };
globalThis.localStorage = { getItem: () => null };
const { checkSignupAvailability } = await import('../src/services/api/authApi.ts');

test('public availability checks preserve the field, typed value, server result and cancellation signal', async () => {
  for (const field of ['LOGIN_ID', 'NICKNAME']) {
    const controller = new AbortController();
    globalThis.fetch = async (url, options) => {
      const parsed = new URL(url);
      assert.equal(parsed.pathname, '/api/v1/auth/signup/availability');
      assert.equal(parsed.searchParams.get('field'), field);
      assert.equal(parsed.searchParams.get('value'), '다은');
      assert.equal(options.signal, controller.signal);
      assert.equal(new Headers(options.headers).has('Authorization'), false);
      return Response.json({ available: false, message: '이미 사용 중이에요.' });
    };
    assert.deepEqual(await checkSignupAvailability(field, '다은', controller.signal), { available: false, message: '이미 사용 중이에요.' });
  }
});
