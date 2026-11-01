/**
 * CSS Modules 안전망: TSX가 참조하는 styles.X 가 실제 .module.css 에 존재하는지 검사한다.
 * 존재하지 않으면 className 이 "undefined" 로 렌더되어 조용히 스타일이 사라지므로,
 * 타입 검사만으로는 잡히지 않는 회귀를 빌드 전에 잡아준다.
 */
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';

const SRC = resolve(import.meta.dirname, '..', 'src');

function walk(dir) {
  return readdirSync(dir).flatMap((entry) => {
    const full = join(dir, entry);
    return statSync(full).isDirectory() ? walk(full) : [full];
  });
}

const files = walk(SRC);
const problems = [];
const dynamic = [];

for (const file of files.filter((f) => f.endsWith('.tsx'))) {
  const source = readFileSync(file, 'utf8');

  const importMatch = source.match(/import\s+(\w+)\s+from\s+'(\.[^']*\.module\.css)'/);
  if (!importMatch) {
    continue;
  }

  const [, alias, relativePath] = importMatch;
  const cssPath = resolve(dirname(file), relativePath);
  const css = readFileSync(cssPath, 'utf8');
  const defined = new Set([...css.matchAll(/\.(-?[_a-zA-Z][\w-]*)/g)].map((m) => m[1]));

  for (const match of source.matchAll(new RegExp(`\\b${alias}\\.([A-Za-z_][\\w]*)`, 'g'))) {
    if (!defined.has(match[1])) {
      problems.push(`${file.replace(SRC, 'src')}: ${alias}.${match[1]} — not found in ${relativePath}`);
    }
  }

  // styles[expr] 형태는 정적으로 확인할 수 없으므로 사람이 볼 목록으로 남긴다.
  for (const match of source.matchAll(new RegExp(`\\b${alias}\\[([^\\]]+)\\]`, 'g'))) {
    dynamic.push(`${file.replace(SRC, 'src')}: ${alias}[${match[1]}]`);
  }
}

if (dynamic.length > 0) {
  console.log('Dynamic class lookups (verify by hand):');
  dynamic.forEach((line) => console.log('  ' + line));
  console.log('');
}

if (problems.length > 0) {
  console.error(`Missing CSS classes (${problems.length}):`);
  problems.forEach((line) => console.error('  ' + line));
  process.exit(1);
}

console.log('All static CSS module class references resolve.');
