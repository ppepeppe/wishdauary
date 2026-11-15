import type { ShopProduct } from '../../types/shop';
import { getCurrentUserIdOrThrow } from '../session/authSession';
import { request } from './apiClient';

export interface CreateCatalogProductPayload {
  title: string;
  description: string;
  imageData?: string | null;
  productUrl?: string | null;
  cloverPrice: number;
  purchasePriceWon?: number | null;
}

export function fetchCatalogProducts() {
  getCurrentUserIdOrThrow();
  return request<ShopProduct[]>('/api/v1/admin/shop/products');
}

export function createCatalogProduct(payload: CreateCatalogProductPayload) {
  getCurrentUserIdOrThrow();
  return request<ShopProduct>('/api/v1/admin/shop/products', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
