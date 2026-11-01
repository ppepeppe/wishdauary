import { useCallback, useEffect, useMemo, useState } from 'react';
import { fetchCloverBalance } from '../services/api/cloverApi';
import { fetchShopProducts, fetchShopRecipients, requestGift } from '../services/api/shopApi';
import type { GiftRequestPayload, GiftRequestResult, ShopFamilyRecipient, ShopProduct } from '../types/shop';

interface ShopData {
  products: ShopProduct[];
  recipients: ShopFamilyRecipient[];
  cloverBalance: number;
}

export function useShop() {
  const [data, setData] = useState<ShopData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [lastRequest, setLastRequest] = useState<GiftRequestResult | null>(null);
  const [pendingProductId, setPendingProductId] = useState<number | null>(null);

  const refetch = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      const [products, recipients, cloverBalance] = await Promise.all([
        fetchShopProducts(),
        fetchShopRecipients(),
        fetchCloverBalance(),
      ]);
      setData({
        products,
        recipients,
        cloverBalance: cloverBalance.balance,
      });
    } catch (caughtError) {
      const message = caughtError instanceof Error ? caughtError.message : '샵 정보를 불러오지 못했어요.';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  const requestProductGift = useCallback(
    async (payload: GiftRequestPayload) => {
      try {
        setPendingProductId(payload.productId);
        setActionError(null);
        setLastRequest(null);
        const result = await requestGift(payload);
        setLastRequest(result);
        setData((current) =>
          current
            ? {
                ...current,
                cloverBalance: result.remainingCloverBalance,
              }
            : current,
        );
      } catch (caughtError) {
        const message = caughtError instanceof Error ? caughtError.message : '선물 요청을 보내지 못했어요.';
        setActionError(message);
      } finally {
        setPendingProductId(null);
      }
    },
    [],
  );

  return useMemo(
    () => ({
      data,
      isLoading,
      error,
      actionError,
      lastRequest,
      pendingProductId,
      refetch,
      requestProductGift,
    }),
    [actionError, data, error, isLoading, lastRequest, pendingProductId, refetch, requestProductGift],
  );
}
