import { compressImageFile } from './image';

export async function prepareShopPhoto(file: File): Promise<string> {
  if (!file.type.startsWith('image/')) {
    throw new Error('사진 파일을 선택해 주세요.');
  }
  if (file.size > 20 * 1024 * 1024) {
    throw new Error('20MB 이하의 사진을 선택해 주세요.');
  }
  try {
    return await compressImageFile(file, 1000, 0.8);
  } catch {
    throw new Error('사진을 읽지 못했어요. JPG 또는 PNG 사진으로 다시 시도해 주세요.');
  }
}
