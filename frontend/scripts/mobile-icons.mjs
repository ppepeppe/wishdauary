import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import sharp from 'sharp';

process.chdir(fileURLToPath(new URL('..', import.meta.url)));
const icon = await readFile('assets/app-icon.svg');
const res = 'android/app/src/main/res';
for (const [density, size, foreground] of [['mdpi',48,108],['hdpi',72,162],['xhdpi',96,216],['xxhdpi',144,324],['xxxhdpi',192,432]]) {
  const directory = `${res}/mipmap-${density}`;
  await mkdir(directory, { recursive: true });
  for (const name of ['ic_launcher', 'ic_launcher_round']) {
    await sharp(icon).resize(size, size).png().toFile(`${directory}/${name}.png`);
  }
  await sharp(icon).resize(foreground, foreground).png().toFile(`${directory}/ic_launcher_foreground.png`);
}
await writeFile(`${res}/values/ic_launcher_background.xml`, '<?xml version="1.0" encoding="utf-8"?><resources><color name="ic_launcher_background">#087f95</color></resources>\n');
await sharp(icon).resize(1024,1024).png().toFile('ios/App/App/Assets.xcassets/AppIcon.appiconset/AppIcon-512@2x.png');
const splash = await sharp(icon).resize(180,180).toBuffer();
const splashDirectory = 'ios/App/App/Assets.xcassets/Splash.imageset';
const splashAssets = JSON.parse(await readFile(`${splashDirectory}/Contents.json`, 'utf8'));
for (const [scale, size] of [[1,2732],[2,2732],[3,2732]]) {
  const filename = splashAssets.images.find((image) => image.scale === `${scale}x`).filename;
  const target = `${splashDirectory}/${filename}`;
  await sharp({create:{width:size,height:size,channels:3,background:'#ffffff'}})
    .composite([{input:splash,gravity:'centre'}]).png().toFile(target);
}
// Android launch screens use a white background with the same brand mark.
const directories = ['drawable','drawable-land-hdpi','drawable-land-mdpi','drawable-land-xhdpi','drawable-land-xxhdpi','drawable-land-xxxhdpi','drawable-port-hdpi','drawable-port-mdpi','drawable-port-xhdpi','drawable-port-xxhdpi','drawable-port-xxxhdpi'];
for (const directory of directories) {
  await mkdir(`${res}/${directory}`, { recursive: true });
  await sharp({create:{width:480,height:480,channels:3,background:'#ffffff'}})
    .composite([{input:splash,gravity:'centre'}]).png().toFile(`${res}/${directory}/splash.png`);
}
console.log('안드로이드·아이폰 앱 아이콘과 시작 화면을 만들었어요.');
