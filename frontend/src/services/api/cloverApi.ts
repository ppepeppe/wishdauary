import type { CloverBalance } from '../../types/clover';
import { getCurrentUserIdOrThrow } from '../session/authSession';
import { request } from './apiClient';

interface CloverBalanceApiResponse {
  userId?: number;
  balance?: number;
}

function normalizeCloverBalance(data: CloverBalanceApiResponse, userId: number): CloverBalance {
  return {
    userId: data.userId ?? userId,
    balance: Math.max(0, data.balance ?? 0),
  };
}

export async function fetchCloverBalance() {
  const userId = getCurrentUserIdOrThrow();

  const data = await request<CloverBalanceApiResponse>('/api/v1/clovers/balance', {
    method: 'GET',
  });

  return normalizeCloverBalance(data, userId);
}

export interface CloverHistoryEntry {
  ledgerId: number;
  amount: number;
  reason: string;
  createdAt: string;
}

interface CloverHistoryApiResponse {
  entries?: Array<{
    ledgerId?: number;
    amount?: number;
    reason?: string;
    createdAt?: string;
  }>;
}

export async function fetchCloverHistory(): Promise<CloverHistoryEntry[]> {
  getCurrentUserIdOrThrow();

  const data = await request<CloverHistoryApiResponse>('/api/v1/clovers/history', {
    method: 'GET',
  });

  return (data.entries ?? [])
    .filter((entry) => typeof entry.ledgerId === 'number')
    .map((entry) => ({
      ledgerId: entry.ledgerId ?? 0,
      amount: entry.amount ?? 0,
      reason: entry.reason ?? '',
      createdAt: entry.createdAt ?? new Date(0).toISOString(),
    }));
}
