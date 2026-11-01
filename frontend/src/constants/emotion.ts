import type { DiaryEmotion } from '../types/diary';

export const EMOTIONS: Array<{
  key: DiaryEmotion;
  label: string;
}> = [
  { key: 'happy', label: '행복해' },
  { key: 'sad', label: '슬퍼' },
  { key: 'angry', label: '화나' },
  { key: 'tired', label: '졸려' },
  { key: 'excited', label: '신나' },
];

export const EMOTION_MAP = Object.fromEntries(
  EMOTIONS.map((emotion) => [emotion.key, emotion]),
) as Record<DiaryEmotion, (typeof EMOTIONS)[number]>;

export function getEmotionMeta(emotion?: DiaryEmotion | null) {
  if (!emotion) {
    return null;
  }

  return EMOTION_MAP[emotion];
}
