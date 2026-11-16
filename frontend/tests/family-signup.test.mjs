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
  await signupFamily({ loginId: 'family_123', password: 'password', nickname: '엄마', familyRelation: 'mother', relationLabel: '엄마' });
  assert.deepEqual(sent, { loginId: 'family_123', password: 'password', nickname: '엄마', familyRelation: 'MOTHER', relationLabel: '엄마' });
  assert.equal('inviteCode' in sent, false);
});

test('a typed family relationship is preserved without needing a separate selection', async () => {
  for (const [relationLabel, familyRelation] of [['고모', 'AUNT'], ['엄마', 'MOTHER'], [' 큰고모 ', 'OTHER_FAMILY'], ['constructor', 'OTHER_FAMILY']]) {
    let sent;
    globalThis.fetch = async (_url, options) => {
      sent = JSON.parse(options.body);
      return Response.json({ userId: 2, appRole: 'family', accessToken: 'token' });
    };
    await signupFamily({ loginId: 'family_123', password: 'password', nickname: '가족', relationLabel });
    assert.equal(sent.relationLabel, relationLabel.trim());
    assert.equal(sent.familyRelation, familyRelation);
  }
});
