import assert from 'node:assert/strict';
import { beforeEach, test } from 'node:test';

const storage = new Map();
globalThis.localStorage = {
  getItem: (key) => storage.get(key) ?? null,
  setItem: (key, value) => storage.set(key, String(value)),
  removeItem: (key) => storage.delete(key),
};
globalThis.window = { location: { origin: 'http://localhost' } };

const { getAuthSession, saveLoginSession, getCurrentUserIdOrThrow } =
  await import('../src/services/session/authSession.ts');
const { request } = await import('../src/services/api/apiClient.ts');
const key = 'wishdiary.auth.session';
const session = { userId: 1, groupId: 2, appRole: 'child', accessToken: 'old-token' };
beforeEach(() => storage.clear());

test('valid child and family sessions retain profile fields', () => {
  for (const appRole of ['child', 'family']) {
    saveLoginSession({ ...session, appRole, nickname: '별이' });
    assert.equal(getAuthSession().appRole, appRole);
    assert.equal(getAuthSession().nickname, '별이');
    assert.equal(getCurrentUserIdOrThrow(), 1);
  }
});

test('invalid stored sessions are removed and require login', () => {
  const invalidValues = [
    '{', 'null', '[]', 'true', '"token"', '{}',
    ...[
      { userId: 0 }, { userId: -1 }, { userId: 1.5 }, { userId: '1' },
      { groupId: null }, { groupId: 0 }, { appRole: 'admin' },
      { accessToken: '' }, { accessToken: '   ' }, { accessToken: null },
      { nickname: {} }, { relationLabel: 1 }, { inviteCode: [] },
    ].map((patch) => JSON.stringify({ ...session, ...patch })),
  ];
  for (const raw of invalidValues) {
    storage.set(key, raw);
    assert.equal(getAuthSession(), null, raw);
    assert.equal(storage.has(key), false, raw);
    assert.throws(getCurrentUserIdOrThrow, /로그인/);
  }
});

test('401 clears the session that sent the request', async () => {
  saveLoginSession(session);
  globalThis.fetch = async () => Response.json({ message: '만료' }, { status: 401 });
  await assert.rejects(request('/api/test'), /만료/);
  assert.equal(getAuthSession(), null);
});

test('late 401 from an old token preserves a new login', async () => {
  saveLoginSession(session);
  let finish;
  globalThis.fetch = () => new Promise((resolve) => { finish = resolve; });
  const pending = request('/api/test');
  saveLoginSession({ ...session, accessToken: 'new-token' });
  finish(Response.json({ message: '만료' }, { status: 401 }));
  await assert.rejects(pending, /만료/);
  assert.equal(getAuthSession().accessToken, 'new-token');
});

test('an unauthenticated request cannot clear a later login', async () => {
  let finish;
  globalThis.fetch = () => new Promise((resolve) => { finish = resolve; });
  const pending = request('/api/test');
  saveLoginSession(session);
  finish(new Response(null, { status: 401 }));
  await assert.rejects(pending);
  assert.equal(getAuthSession().accessToken, 'old-token');
});

test('all HeadersInit forms override default headers case-insensitively', async () => {
  saveLoginSession(session);
  for (const headers of [
    { 'content-type': 'text/plain', authorization: 'Bearer custom' },
    new Headers({ 'content-type': 'text/plain', authorization: 'Bearer custom' }),
    [['content-type', 'text/plain'], ['authorization', 'Bearer custom']],
  ]) {
    globalThis.fetch = async (_url, init) => {
      const actual = new Headers(init.headers);
      assert.equal(actual.get('content-type'), 'text/plain');
      assert.equal(actual.get('authorization'), 'Bearer custom');
      return new Response(null, { status: 204 });
    };
    assert.equal(await request('/api/test', { headers }), undefined);
  }
});

test('401 for an explicit different token preserves the stored session', async () => {
  saveLoginSession(session);
  globalThis.fetch = async () => new Response(null, { status: 401 });
  await assert.rejects(request('/api/test', { headers: { Authorization: 'Bearer other' } }));
  assert.equal(getAuthSession().accessToken, 'old-token');
});

test('query values and common response data are preserved', async () => {
  saveLoginSession(session);
  globalThis.fetch = async (url, init) => {
    const params = new URL(url).searchParams;
    assert.equal(params.get('page'), '0');
    assert.equal(params.get('enabled'), 'false');
    assert.equal(params.has('missing'), false);
    assert.equal(new Headers(init.headers).get('authorization'), 'Bearer old-token');
    return Response.json({ success: true, code: 'OK', message: '', data: [1, 2] });
  };
  assert.deepEqual(await request('/api/test', { query: { page: 0, enabled: false, missing: undefined } }), [1, 2]);
});

test('network failure and non-JSON server failure provide readable errors', async () => {
  globalThis.fetch = async () => { throw new TypeError('Failed to fetch'); };
  await assert.rejects(request('/api/test'), /네트워크/);
  globalThis.fetch = async () => new Response('<html>Error</html>', { status: 502 });
  await assert.rejects(request('/api/test'), /서버 오류/);
});

test('duplicate email and upstream English errors are shown in Korean', async () => {
  globalThis.fetch = async () => Response.json({
    code: 'DUPLICATE_USER_EMAIL',
    message: 'User email already exists. email=test@example.com',
  }, { status: 409 });
  await assert.rejects(request('/api/test'), /이미 가입된 이메일이에요/);

  globalThis.fetch = async () => Response.json({ error: 'Bad Request' }, { status: 400 });
  await assert.rejects(request('/api/test'), /요청 형식을 다시 확인해 주세요/);

  globalThis.fetch = async () => Response.json({ message: 'User email already exists. email=홍길동@example.com' }, { status: 409 });
  await assert.rejects(request('/api/test'), /요청을 처리하지 못했어요/);
});

test('malformed successful response is shown in Korean', async () => {
  globalThis.fetch = async () => new Response('not json', { status: 200 });
  await assert.rejects(request('/api/test'), /응답을 읽지 못했어요/);
});
