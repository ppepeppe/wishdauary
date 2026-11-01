import { fetchDiaryList } from '../services/api/diaryApi';
import { useAsyncData } from './useAsyncData';

export function useDiaryList() {
  return useAsyncData(fetchDiaryList);
}
