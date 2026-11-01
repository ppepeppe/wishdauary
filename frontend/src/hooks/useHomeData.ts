import { fetchHomeSummary } from '../services/api/userApi';
import { useAsyncData } from './useAsyncData';

export function useHomeData() {
  return useAsyncData(fetchHomeSummary);
}
