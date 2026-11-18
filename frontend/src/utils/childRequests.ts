import type { ReceivedGiftRequest, ShopProduct } from '../types/shop';
import type { EnglishStudy } from '../types/rewards';

export function getChildGiftRequests<T extends Pick<ReceivedGiftRequest, 'requesterChildUserId' | 'status'>>(items: T[], childId?: number) {
  return childId === undefined ? [] : items.filter(item => item.requesterChildUserId === childId && item.status === 'pending');
}

export function getChildPricingRequests<T extends Pick<ShopProduct, 'childUserId' | 'cloverPrice'>>(items: T[], childId?: number) {
  return childId === undefined ? [] : items.filter(item => item.childUserId === childId && item.cloverPrice <= 0);
}

export function getChildEnglishRequests<T extends Pick<EnglishStudy, 'childUserId' | 'status'>>(items: T[], childId: number) {
  return items.filter(item => item.childUserId === childId && item.status === 'pending');
}
