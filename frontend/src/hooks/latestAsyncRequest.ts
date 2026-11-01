interface RequestCallbacks<T> {
  onSuccess: (value: T) => void;
  onError: (error: unknown) => void;
  onSettled: () => void;
}

export function createLatestAsyncRequest() {
  let currentRequest = 0;

  return {
    async run<T>(fetcher: () => Promise<T>, callbacks: RequestCallbacks<T>): Promise<void> {
      const requestId = ++currentRequest;

      try {
        const result = await fetcher();
        if (requestId === currentRequest) callbacks.onSuccess(result);
      } catch (error) {
        if (requestId === currentRequest) callbacks.onError(error);
      } finally {
        if (requestId === currentRequest) callbacks.onSettled();
      }
    },
    invalidate() {
      currentRequest += 1;
    },
  };
}
