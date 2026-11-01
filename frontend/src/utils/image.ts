export async function compressImageFile(file: File, maxEdge = 256, quality = 0.72): Promise<string> {
  const bitmap = await createImageBitmap(file);
  const scale = Math.min(1, maxEdge / Math.max(bitmap.width, bitmap.height));
  const canvas = document.createElement('canvas');
  canvas.width = Math.max(1, Math.round(bitmap.width * scale));
  canvas.height = Math.max(1, Math.round(bitmap.height * scale));

  const context = canvas.getContext('2d');
  if (!context) {
    throw new Error('사진을 읽지 못했어요. 다른 사진으로 다시 시도해 주세요.');
  }

  context.drawImage(bitmap, 0, 0, canvas.width, canvas.height);
  const dataUrl = canvas.toDataURL('image/jpeg', quality);

  if (dataUrl.length > 280_000) {
    return compressImageFile(file, Math.floor(maxEdge * 0.8), Math.max(0.5, quality - 0.12));
  }

  return dataUrl;
}
