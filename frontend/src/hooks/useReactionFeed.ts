import { fetchReactionFeed } from '../services/api/diaryApi';
import { useAsyncData } from './useAsyncData';

export function useReactionFeed() {
  return useAsyncData(fetchReactionFeed);
}
