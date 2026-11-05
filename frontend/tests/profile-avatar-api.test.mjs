import assert from 'node:assert/strict';
import { beforeEach, test } from 'node:test';

const storage = new Map();
globalThis.localStorage = { getItem: key => storage.get(key) ?? null, setItem: (key, value) => storage.set(key, value), removeItem: key => storage.delete(key) };
globalThis.window = { location: { origin: 'http://localhost:5187' } };
const { fetchMyProfile, updateMyProfile } = await import('../src/services/api/userApi.ts');
beforeEach(() => storage.set('wishdiary.auth.session', JSON.stringify({ userId: 1, groupId: 1, appRole: 'child', accessToken: 'fixture' })));

test('older profiles retain their background and custom image with the default character', async () => {
  globalThis.fetch = async () => Response.json({ userId: 1, avatarPreset: 'mint', avatarImage: 'data:image/png;base64,aGVsbG8=' });
  const profile = await fetchMyProfile();
  assert.equal(profile.avatarCharacter, 'my-melody');
  assert.equal(profile.avatarPreset, 'mint');
  assert.equal(profile.avatarImage, 'data:image/png;base64,aGVsbG8=');
});

test('saving a character also preserves its independent background on reload', async () => {
  for (const avatarCharacter of ['my-melody', 'kuromi', 'cinnamoroll']) {
    const payload = { nickname: '하루', avatarPreset: 'sky', avatarImage: null, avatarCharacter };
    let saved;
    globalThis.fetch = async (url, init) => {
      assert.equal(new URL(url).pathname, '/api/v1/users/me');
      if (init.method === 'PATCH') { saved = JSON.parse(init.body); assert.deepEqual(saved, payload); }
      return Response.json({ userId: 1, ...saved });
    };
    assert.equal((await updateMyProfile(payload)).avatarCharacter, avatarCharacter);
    const reloaded = await fetchMyProfile();
    assert.equal(reloaded.avatarCharacter, avatarCharacter);
    assert.equal(reloaded.avatarPreset, 'sky');
  }
});
