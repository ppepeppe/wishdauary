import { useAsyncData } from './useAsyncData';
import {
  fetchProductPricingRequests,
  fetchReceivedGiftRequests,
  processGiftRequest,
  setProductPrice,
} from '../services/api/shopApi';

async function fetchFamilyShopData() {
  const [giftRequests, productPricingRequests] = await Promise.all([
    fetchReceivedGiftRequests(),
    fetchProductPricingRequests(),
  ]);

  return { giftRequests, productPricingRequests };
}

export function useGiftRequests() {
  const state = useAsyncData(fetchFamilyShopData);

  async function processRequest(notificationId: number, action: 'APPROVE' | 'REJECT') {
    await processGiftRequest(notificationId, action);
    await state.refetch();
  }

  async function priceProduct(productId: number, cloverPrice: number, purchasePriceWon?: number | null) {
    await setProductPrice(productId, cloverPrice, purchasePriceWon);
    await state.refetch();
  }

  return { ...state, processRequest, priceProduct };
}
