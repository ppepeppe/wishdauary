export const AVATAR_PRESETS = [
  { key: 'peach', label: '복숭아' },
  { key: 'mint', label: '민트' },
  { key: 'sky', label: '하늘' },
  { key: 'lavender', label: '라벤더' },
  { key: 'honey', label: '꿀' },
  { key: 'rose', label: '장미' },
] as const;

export const AVATAR_CHARACTERS = [
  { key: 'my-melody', label: '마이멜로디', imageUrl: '/assets/avatars/my-melody.png' },
  { key: 'kuromi', label: '쿠로미', imageUrl: '/assets/avatars/kuromi.png' },
  { key: 'cinnamoroll', label: '시나모롤', imageUrl: '/assets/avatars/cinnamoroll.png' },
] as const;

export function getAvatarCharacter(value?: string | null) {
  return AVATAR_CHARACTERS.find((character) => character.key === value) ?? AVATAR_CHARACTERS[0];
}

export type AvatarPreset = (typeof AVATAR_PRESETS)[number]['key'];

export function isAvatarPreset(value: string | null | undefined): value is AvatarPreset {
  return AVATAR_PRESETS.some((preset) => preset.key === value);
}
