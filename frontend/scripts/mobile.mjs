import { execFileSync } from 'node:child_process';
import { copyFileSync, existsSync, mkdirSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { build, loadEnv } from 'vite';
import { resolveMobileOptions } from './mobile-options.mjs';

process.chdir(fileURLToPath(new URL('..', import.meta.url)));
const { values, positionals } = parseArgs({
  allowPositionals: true,
  options: {
    development: { type: 'boolean', default: false },
    'api-url': { type: 'string' },
    open: { type: 'boolean', default: false },
    run: { type: 'boolean', default: false },
    apk: { type: 'boolean', default: false },
  },
});
const capCli = fileURLToPath(new URL('../node_modules/@capacitor/cli/bin/capacitor', import.meta.url));
function cap(...args) {
  execFileSync(process.execPath, [capCli, ...args], { stdio: 'inherit', env: process.env });
}

function androidBuildEnvironment() {
  const env = { ...process.env };
  if (!env.JAVA_HOME && process.platform === 'darwin') {
    try {
      env.JAVA_HOME = execFileSync('/usr/libexec/java_home', ['-v', '21'], { encoding: 'utf8' }).trim();
    } catch { /* Gradle will explain if the required JDK is missing. */ }
  }
  return env;
}

function buildAndroidApk() {
  execFileSync('./gradlew', [':app:assembleDebug'], { cwd: 'android', stdio: 'inherit', env: androidBuildEnvironment() });
  mkdirSync('artifacts', { recursive: true });
  copyFileSync('android/app/build/outputs/apk/debug/app-debug.apk', 'artifacts/wishdauary-android-debug.apk');
  copyFileSync('dist-native/mobile-build.json', 'artifacts/mobile-build.json');
  console.log('설치 파일: artifacts/wishdauary-android-debug.apk');
}

async function prepareMobile() {
  const env = loadEnv('mobile', process.cwd(), 'MOBILE_');
  const options = resolveMobileOptions(positionals[0], {
    development: values.development,
    apiUrl: values['api-url'] ?? env.MOBILE_API_BASE_URL,
  });
  if (values.apk && options.platform !== 'android') throw new Error('--apk는 android에서만 사용할 수 있어요.');
  process.env.WISHDIARY_MOBILE_DEVELOPMENT = String(options.development);
  process.env.VITE_API_BASE_URL = options.apiBaseUrl;
  execFileSync('npm', ['run', 'check:css'], { stdio: 'inherit' });
  execFileSync(process.execPath, ['node_modules/typescript/bin/tsc', '-b'], { stdio: 'inherit' });
  await build({ mode: 'native' });
  writeFileSync('dist-native/mobile-build.json', JSON.stringify(options, null, 2));
  if (!existsSync(options.platform)) cap('add', options.platform);
  cap('sync', options.platform);
  console.log(`\n${options.platform} 앱 준비 완료: ${options.apiBaseUrl} (${options.development ? '로컬 테스트' : '배포용'})`);
  if (values.apk) buildAndroidApk();
  if (values.open) cap('open', options.platform);
  if (values.run) cap('run', options.platform, '--no-sync');
}

prepareMobile().catch((error) => {
  console.error(`\n앱 준비 실패: ${error.message}`);
  process.exitCode = 1;
});
