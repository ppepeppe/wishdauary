import { useCallback, useEffect, useRef, useState } from 'react';
import { createLatestAsyncRequest } from './latestAsyncRequest';

interface AsyncState<T> {
  data: T | null;
  isLoading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export function useAsyncData<T>(fetcher: () => Promise<T>, refreshOnFocus = false): AsyncState<T> {
  const [data, setData] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const request = useRef(createLatestAsyncRequest());

  const refetch = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    await request.current.run(fetcher, {
      onSuccess: setData,
      onError: (caughtError) => {
        const message = caughtError instanceof Error ? caughtError.message : '알 수 없는 오류가 발생했어요.';
        setError(message);
      },
      onSettled: () => setIsLoading(false),
    });
  }, [fetcher]);

  useEffect(() => {
    setData(null);
    void refetch();
    return () => request.current.invalidate();
  }, [refetch]);

  useEffect(() => {
    if (!refreshOnFocus) return;
    const refresh = () => void refetch();
    window.addEventListener('focus', refresh);
    return () => window.removeEventListener('focus', refresh);
  }, [refetch, refreshOnFocus]);

  return { data, isLoading, error, refetch };
}
