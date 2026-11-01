import assert from 'node:assert/strict';
import { test } from 'node:test';
import { createLatestAsyncRequest } from '../src/hooks/latestAsyncRequest.ts';

function deferred() {
  let resolve;
  let reject;
  const promise = new Promise((done, fail) => {
    resolve = done;
    reject = fail;
  });
  return { promise, resolve, reject };
}

test('a slower earlier success cannot replace the latest result or loading state', async () => {
  const request = createLatestAsyncRequest();
  const earlier = deferred();
  const latest = deferred();
  const events = [];
  const callbacks = {
    onSuccess: (value) => events.push(['success', value]),
    onError: (error) => events.push(['error', error.message]),
    onSettled: () => events.push(['settled']),
  };

  const firstRun = request.run(() => earlier.promise, callbacks);
  const secondRun = request.run(() => latest.promise, callbacks);
  earlier.resolve('old');
  await firstRun;
  assert.deepEqual(events, []);

  latest.resolve('new');
  await secondRun;
  assert.deepEqual(events, [['success', 'new'], ['settled']]);
});

test('a slower earlier failure cannot show an error after a newer success', async () => {
  const request = createLatestAsyncRequest();
  const earlier = deferred();
  const events = [];
  const callbacks = {
    onSuccess: (value) => events.push(['success', value]),
    onError: (error) => events.push(['error', error.message]),
    onSettled: () => events.push(['settled']),
  };

  const firstRun = request.run(() => earlier.promise, callbacks);
  await request.run(async () => 'new', callbacks);
  earlier.reject(new Error('old failure'));
  await firstRun;
  assert.deepEqual(events, [['success', 'new'], ['settled']]);
});

test('invalidating a request prevents updates after its owner is removed', async () => {
  const request = createLatestAsyncRequest();
  const pending = deferred();
  const events = [];
  const run = request.run(() => pending.promise, {
    onSuccess: (value) => events.push(value),
    onError: (error) => events.push(error),
    onSettled: () => events.push('settled'),
  });

  request.invalidate();
  pending.resolve('late');
  await run;
  assert.deepEqual(events, []);
});
