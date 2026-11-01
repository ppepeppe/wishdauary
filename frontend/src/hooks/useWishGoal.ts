import { useCallback, useMemo, useState } from 'react';
import { clearWishGoal, fetchWishGoal, setWishGoal } from '../services/api/wishGoalApi';
import type { WishGoal } from '../types/wishGoal';
import { useAsyncData } from './useAsyncData';

export function useWishGoal() {
  const { data, isLoading, error, refetch } = useAsyncData<WishGoal | null>(fetchWishGoal);
  const [isSaving, setIsSaving] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const saveGoal = useCallback(
    async (productId: number) => {
      try {
        setIsSaving(true);
        setActionError(null);
        await setWishGoal(productId);
        await refetch();
        return true;
      } catch (caughtError) {
        setActionError(caughtError instanceof Error ? caughtError.message : '목표를 저장하지 못했어요.');
        return false;
      } finally {
        setIsSaving(false);
      }
    },
    [refetch],
  );

  const removeGoal = useCallback(async () => {
    try {
      setIsSaving(true);
      setActionError(null);
      await clearWishGoal();
      await refetch();
      return true;
    } catch (caughtError) {
      setActionError(caughtError instanceof Error ? caughtError.message : '목표를 지우지 못했어요.');
      return false;
    } finally {
      setIsSaving(false);
    }
  }, [refetch]);

  return useMemo(
    () => ({
      goal: data,
      isLoading,
      error,
      isSaving,
      actionError,
      refetch,
      saveGoal,
      removeGoal,
    }),
    [actionError, data, error, isLoading, isSaving, refetch, removeGoal, saveGoal],
  );
}
