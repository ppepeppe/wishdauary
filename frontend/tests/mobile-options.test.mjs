import assert from 'node:assert/strict';
import { test } from 'node:test';
import { resolveMobileOptions } from '../scripts/mobile-options.mjs';

test('local development reaches the host from each platform simulator', () => {
  assert.equal(resolveMobileOptions('android', { development: true }).apiBaseUrl, 'http://10.0.2.2:8080');
  assert.equal(resolveMobileOptions('ios', { development: true }).apiBaseUrl, 'http://localhost:8080');
});

test('physical devices can use an explicitly configured LAN backend', () => {
  const options = resolveMobileOptions('ios', { development: true, apiUrl: ' http://192.168.1.12:8080/ ' });
  assert.equal(options.apiBaseUrl, 'http://192.168.1.12:8080');
});

test('release builds require an explicit remote HTTPS backend', () => {
  for (const apiUrl of [undefined, '', 'http://api.example.com', 'https://localhost', 'https://127.0.0.1', 'https://[::1]', 'https://10.0.2.2']) {
    assert.throws(() => resolveMobileOptions('android', { apiUrl }));
  }
  assert.deepEqual(resolveMobileOptions('android', { apiUrl: 'https://api.example.com/' }), {
    platform: 'android', development: false, apiBaseUrl: 'https://api.example.com',
  });
});

test('invalid addresses and credentials cannot enter an app build', () => {
  for (const apiUrl of ['invalid', 'file:///tmp/api', 'https://user:password@api.example.com', 'https://api.example.com/api', 'https://api.example.com?token=secret', 'https://api.example.com/#secret']) {
    assert.throws(() => resolveMobileOptions('ios', { apiUrl }));
  }
  assert.throws(() => resolveMobileOptions('web', { development: true }));
});
