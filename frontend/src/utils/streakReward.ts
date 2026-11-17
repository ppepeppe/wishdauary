const STREAK_REWARDS = [
  { days: 3, clovers: 1 },
  { days: 5, clovers: 2 },
  { days: 7, clovers: 3 },
] as const;

export function getNextStreakReward(currentStreak: number) {
  return STREAK_REWARDS.find(reward => reward.days > currentStreak) ?? null;
}
