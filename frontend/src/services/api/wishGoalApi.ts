import type { ChildWishGoal, WishGoal } from '../../types/wishGoal';
import { getCurrentChildUserIdOrThrow, getCurrentFamilyUserIdOrThrow } from '../session/authSession';
import { request } from './apiClient';

interface WishGoalApiResponse {
  goalId?: number;
  productId?: number;
  productTitle?: string;
  productImageUrl?: string | null;
  targetCloverPrice?: number;
  currentCloverBalance?: number;
  remainingClovers?: number;
  createdAt?: string;
}

function normalizeWishGoal(data: WishGoalApiResponse | null): WishGoal | null {
  if (!data || !data.goalId || !data.productId) {
    return null;
  }

  return {
    goalId: data.goalId,
    productId: data.productId,
    productTitle: data.productTitle?.trim() || '이름 없는 선물',
    productImageUrl: data.productImageUrl ?? null,
    targetCloverPrice: Math.max(0, data.targetCloverPrice ?? 0),
    currentCloverBalance: Math.max(0, data.currentCloverBalance ?? 0),
    remainingClovers: Math.max(0, data.remainingClovers ?? 0),
    createdAt: data.createdAt,
  };
}

export async function fetchWishGoal() {
  getCurrentChildUserIdOrThrow();
  const data = await request<WishGoalApiResponse | null>('/api/v1/wish-goals/me', {
    method: 'GET',
  });

  return normalizeWishGoal(data);
}

export async function setWishGoal(productId: number) {
  getCurrentChildUserIdOrThrow();
  const data = await request<WishGoalApiResponse>('/api/v1/wish-goals/me', {
    method: 'PUT',
    body: JSON.stringify({ productId }),
  });

  return normalizeWishGoal(data);
}

export async function clearWishGoal() {
  getCurrentChildUserIdOrThrow();
  await request<void>('/api/v1/wish-goals/me', {
    method: 'DELETE',
  });
}

interface ChildWishGoalApiItem {
  childUserId?: number;
  childNickname?: string;
  goal?: WishGoalApiResponse;
}

export async function fetchChildrenWishGoals(): Promise<ChildWishGoal[]> {
  getCurrentFamilyUserIdOrThrow();
  const items = await request<ChildWishGoalApiItem[]>('/api/v1/wish-goals/children', {
    method: 'GET',
  });

  return items
    .map((item) => {
      const goal = normalizeWishGoal(item.goal ?? null);
      if (!item.childUserId || !goal) {
        return null;
      }

      return {
        childUserId: item.childUserId,
        childNickname: item.childNickname?.trim() || '아이',
        goal,
      };
    })
    .filter((item): item is ChildWishGoal => item !== null);
}
