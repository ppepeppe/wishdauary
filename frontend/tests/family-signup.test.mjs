import assert from 'node:assert/strict';
import { test } from 'node:test';

globalThis.window = { location: { origin: 'http://localhost' } };
globalThis.localStorage = { getItem: () => null };
const { signupFamily } = await import('../src/services/api/authApi.ts');

test('family signup sends identity and relationship without an invitation code', async () => {
  let sent;
  globalThis.fetch = async (_url, options) => {
    sent = JSON.parse(options.body);
    return Response.json({ userId: 2, appRole: 'family', accessToken: 'token' });
  };
  await signupFamily({ email: 'family@test.com', password: 'password', nickname: '엄마', familyRelation: 'mother', relationLabel: '엄마' });
  assert.deepEqual(sent, { email: 'family@test.com', password: 'password', nickname: '엄마', familyRelation: 'MOTHER', relationLabel: '엄마' });
  assert.equal('inviteCode' in sent, false);
});
