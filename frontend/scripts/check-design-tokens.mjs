import { readdirSync, readFileSync } from 'node:fs';
import { resolve, join, basename, relative } from 'node:path';

const root = resolve(import.meta.dirname, '../src');
const artwork = new Set(['ProductArtwork.module.css', 'EmotionIcon.module.css', 'ProfileAvatar.module.css']);
function files(directory) {
  return readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const path = join(directory, entry.name);
    return entry.isDirectory() ? files(path) : [path];
  });
}
const errors = [];
for (const file of files(root).filter(path => path.endsWith('.module.css') && !artwork.has(basename(path)))) {
  const css = readFileSync(file, 'utf8').replace(/\/\*[\s\S]*?\*\//g, '');
  for (const match of css.matchAll(/(?<![\w-])(border-radius|(?:row-|column-)?gap|(?:padding|margin)[\w-]*)\s*:\s*([^;{}]+)/g)) {
    const pixels = [...match[2].matchAll(/(?<![\w.])(-?\d+(?:\.\d+)?)px/g)].map(value => Number(value[1]));
    if (match[1] === 'border-radius' && pixels.length) {
      errors.push(`${relative(root, file)}: border-radius must use a radius token`);
    } else if (pixels.some(value => value % 4 !== 0)) {
      errors.push(`${relative(root, file)}: ${match[1]} must use the 4px spacing scale`);
    }
  }
}
if (errors.length) {
  console.error(errors.join('\n'));
  process.exitCode = 1;
} else {
  console.log('Design tokens verified: shared radii and 4px layout spacing.');
}
