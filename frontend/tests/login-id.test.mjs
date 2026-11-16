import assert from 'node:assert/strict';
import { test } from 'node:test';

globalThis.window = { location: { origin: 'http://localhost' } };
globalThis.localStorage = { getItem: () => JSON.stringify({ userId: 1, groupId: 1, appRole: 'child', accessToken: 'fixture' }) };
const { signupChild, signupFamily, login } = await import('../src/services/api/authApi.ts');
const { fetchMyProfile } = await import('../src/services/api/userApi.ts');
const { request } = await import('../src/services/api/apiClient.ts');

test('child signup, family signup and login send a plain login ID without requiring email', async () => {
  const cases = [
    [signupChild, '/signup/child', { loginId: '우리조카', password: 'password', nickname: '아이' }],
    [signupFamily, '/signup/family', { loginId: 'family_123', password: 'password', nickname: '가족', familyRelation: 'mother', relationLabel: '엄마' }],
    [login, '/login', { loginId: 'kid_123', password: 'password' }],
  ];
  for (const [send, suffix, payload] of cases) {
    globalThis.fetch = async (url, options) => {
      assert.equal(new URL(url).pathname, '/api/v1/auth' + suffix);
      const body = JSON.parse(options.body);
      assert.equal(body.loginId, payload.loginId);
      assert.equal(body.password, payload.password);
      assert.equal('email' in body, false);
      return Response.json({ userId: 1, appRole: 'child', accessToken: 'fixture' });
    };
    await send(payload);
  }
});

test('profiles use loginId and retain compatibility with an older email-only response', async () => {
  for (const [data, expected] of [[{ loginId: 'kid_123', email: 'old@test.com' }, 'kid_123'], [{ email: 'legacy@test.com' }, 'legacy@test.com']]) {
    globalThis.fetch = async () => Response.json({ userId: 1, ...data });
    assert.equal((await fetchMyProfile()).loginId, expected);
  }
});

test('both new and legacy duplicate ID errors use the ID wording', async () => {
  for (const code of ['DUPLICATE_LOGIN_ID', 'DUPLICATE_USER_EMAIL']) {
    globalThis.fetch = async () => Response.json({ code, message: 'duplicate' }, { status: 409 });
    await assert.rejects(request('/api/test'), /이미 사용 중인 아이디예요/);
  }
});
