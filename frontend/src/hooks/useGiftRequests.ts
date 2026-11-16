import { useAsyncData } from './useAsyncData';
import { fetchFamilyConnections } from '../services/api/familyConnectionApi';
import { GIFT_REQUESTS_CHANGED_EVENT } from '../constants/events';
import {
  fetchProductPricingRequests,
  fetchReceivedGiftRequests,
  processGiftRequest,
  setProductPrice,
} from '../services/api/shopApi';

async function fetchFamilyShopData() {
  const [giftRequests, productPricingRequests, children] = await Promise.all([
    fetchReceivedGiftRequests(),
    fetchProductPricingRequests(),
    fetchFamilyConnections(),
  ]);

  return { giftRequests, productPricingRequests, children };
}

export function useGiftRequests() {
  const state = useAsyncData(fetchFamilyShopData, true);

  async function processRequest(notificationId: number, action: 'APPROVE' | 'REJECT') {
    await processGiftRequest(notificationId, action);
    await state.refetch();
    window.dispatchEvent(new Event(GIFT_REQUESTS_CHANGED_EVENT));
  }

  async function priceProduct(productId: number, cloverPrice: number, purchasePriceWon?: number | null) {
    await setProductPrice(productId, cloverPrice, purchasePriceWon);
    await state.refetch();
    window.dispatchEvent(new Event(GIFT_REQUESTS_CHANGED_EVENT));
  }

  return { ...state, processRequest, priceProduct };
}
