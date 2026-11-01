import { fetchUserStreak } from '../services/api/userApi';
import { useAsyncData } from './useAsyncData';

export function useUserStreak() {
  return useAsyncData(fetchUserStreak);
}
