export const AVATAR_PRESETS = [
  { key: 'peach', label: '복숭아' },
  { key: 'mint', label: '민트' },
  { key: 'sky', label: '하늘' },
  { key: 'lavender', label: '라벤더' },
  { key: 'honey', label: '꿀' },
  { key: 'rose', label: '장미' },
] as const;

export type AvatarPreset = (typeof AVATAR_PRESETS)[number]['key'];

export function isAvatarPreset(value: string | null | undefined): value is AvatarPreset {
  return AVATAR_PRESETS.some((preset) => preset.key === value);
}
