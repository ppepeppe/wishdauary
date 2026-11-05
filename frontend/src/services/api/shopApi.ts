import type {
  CreateShopProductPayload,
  CreateWishProductPayload,
  GiftRequestPayload,
  GiftRequestResult,
  ReceivedGiftRequest,
  ShopFamilyRecipient,
  ShopProduct,
} from '../../types/shop';
import { getCurrentChildUserIdOrThrow, getCurrentFamilyUserIdOrThrow } from '../session/authSession';
import { request } from './apiClient';

interface ShopProductApiItem {
  productId?: number;
  id?: number;
  title?: string;
  description?: string;
  imageUrl?: string | null;
  productUrl?: string | null;
  cloverPrice?: number;
  purchasePriceWon?: number | null;
  source?: string;
  childUserId?: number | null;
  receiverFamilyUserId?: number | null;
  createdAt?: string;
}

interface ShopFamilyRecipientApiItem {
  userId?: number;
  id?: number;
  nickname?: string | null;
  relationLabel?: string | null;
}

interface ReceivedGiftRequestApiItem {
  notificationId?: number;
  id?: number;
  productId?: number;
  productTitle?: string;
  productImageUrl?: string | null;
  productUrl?: string | null;
  cloverPrice?: number;
  purchasePriceWon?: number | null;
  requesterChildUserId?: number;
  requesterChildNickname?: string | null;
  receiverFamilyUserId?: number;
  receiverRelationLabel?: string | null;
  message?: string | null;
  status?: string;
  processedByFamilyUserId?: number | null;
  processedAt?: string | null;
  createdAt?: string;
}

function normalizeProduct(item: ShopProductApiItem): ShopProduct {
  return {
    productId: item.productId ?? item.id ?? 0,
    title: item.title?.trim() || '이름 없는 선물',
    description: item.description?.trim() || '설명이 아직 없어요.',
    imageUrl: item.imageUrl ?? null,
    productUrl: item.productUrl ?? null,
    cloverPrice: Math.max(0, item.cloverPrice ?? 0),
    purchasePriceWon: item.purchasePriceWon ?? null,
    source: item.source ?? 'custom',
    childUserId: item.childUserId ?? null,
    receiverFamilyUserId: item.receiverFamilyUserId ?? null,
    createdAt: item.createdAt,
  };
}

function normalizeRecipient(item: ShopFamilyRecipientApiItem): ShopFamilyRecipient {
  return {
    userId: item.userId ?? item.id ?? 0,
    nickname: item.nickname?.trim() || '가족',
    relationLabel: item.relationLabel?.trim() || '가족',
  };
}

function normalizeReceivedGiftRequest(item: ReceivedGiftRequestApiItem): ReceivedGiftRequest {
  return {
    notificationId: item.notificationId ?? item.id ?? 0,
    productId: item.productId ?? 0,
    productTitle: item.productTitle?.trim() || '이름 없는 선물',
    productImageUrl: item.productImageUrl ?? null,
    productUrl: item.productUrl ?? null,
    requesterChildUserId: item.requesterChildUserId ?? 0,
    requesterChildNickname: item.requesterChildNickname?.trim() || '아이',
    receiverFamilyUserId: item.receiverFamilyUserId ?? 0,
    receiverRelationLabel: item.receiverRelationLabel?.trim() || '가족',
    message: item.message?.trim() || '',
    cloverPrice: Math.max(0, item.cloverPrice ?? 0),
    purchasePriceWon: item.purchasePriceWon ?? null,
    status: item.status === 'approved' || item.status === 'rejected' ? item.status : 'pending',
    processedByFamilyUserId: item.processedByFamilyUserId ?? null,
    processedAt: item.processedAt ?? null,
    createdAt: item.createdAt ?? new Date(0).toISOString(),
  };
}

export async function fetchShopProducts() {
  getCurrentChildUserIdOrThrow();
  const products = await request<ShopProductApiItem[]>('/api/v1/shop/products', {
    method: 'GET',
  });

  return products.map(normalizeProduct).filter((product) => product.productId > 0);
}

export async function createShopProduct(payload: CreateWishProductPayload) {
  getCurrentChildUserIdOrThrow();
  const product = await request<ShopProductApiItem>('/api/v1/shop/wish-requests', {
    method: 'POST',
    body: JSON.stringify({
      title: payload.title,
      description: payload.description,
      imageUrl: payload.imageUrl || null,
      imageData: payload.imageData || null,
      productUrl: payload.productUrl || null,
      receiverFamilyUserId: payload.receiverFamilyUserId,
    }),
  });

  return normalizeProduct(product);
}

export async function fetchWishProductRequests() {
  getCurrentChildUserIdOrThrow();
  const products = await request<ShopProductApiItem[]>('/api/v1/shop/wish-requests');
  return products.map(normalizeProduct).filter((product) => product.productId > 0);
}

export async function fetchShopRecipients() {
  getCurrentChildUserIdOrThrow();
  const recipients = await request<ShopFamilyRecipientApiItem[]>('/api/v1/shop/recipients', {
    method: 'GET',
  });

  return recipients.map(normalizeRecipient).filter((recipient) => recipient.userId > 0);
}

export async function requestGift(payload: GiftRequestPayload) {
  getCurrentChildUserIdOrThrow();

  return request<GiftRequestResult>('/api/v1/shop/gift-requests', {
    method: 'POST',
    body: JSON.stringify({
      productId: payload.productId,
      receiverFamilyUserId: payload.receiverFamilyUserId,
      message: payload.message,
    }),
  });
}

export async function fetchReceivedGiftRequests() {
  getCurrentFamilyUserIdOrThrow();
  const items = await request<ReceivedGiftRequestApiItem[]>('/api/v1/shop/gift-requests', {
    method: 'GET',
  });

  return items.map(normalizeReceivedGiftRequest).filter((item) => item.notificationId > 0);
}

export async function fetchSentGiftRequests() {
  getCurrentChildUserIdOrThrow();
  const items = await request<ReceivedGiftRequestApiItem[]>('/api/v1/shop/gift-requests/sent', {
    method: 'GET',
  });

  return items.map(normalizeReceivedGiftRequest).filter((item) => item.notificationId > 0);
}

export async function fetchProductPricingRequests() {
  const items = await request<ShopProductApiItem[]>('/api/v1/shop/product-pricing-requests', {
    method: 'GET',
  });

  return items.map(normalizeProduct).filter((product) => product.productId > 0);
}

export async function setProductPrice(productId: number, cloverPrice: number, purchasePriceWon?: number | null) {
  getCurrentFamilyUserIdOrThrow();
  const product = await request<ShopProductApiItem>(`/api/v1/shop/products/${productId}/price`, {
    method: 'PATCH',
    body: JSON.stringify({ cloverPrice, purchasePriceWon: purchasePriceWon ?? null }),
  });

  return normalizeProduct(product);
}

export async function processGiftRequest(
  notificationId: number,
  action: 'APPROVE' | 'REJECT',
) {
  const item = await request<ReceivedGiftRequestApiItem>(`/api/v1/shop/gift-requests/${notificationId}`, {
    method: 'PATCH',
    body: JSON.stringify({ action }),
  });

  return normalizeReceivedGiftRequest(item);
}

export async function fetchFamilyShopProducts() {
  getCurrentFamilyUserIdOrThrow();
  const products = await request<ShopProductApiItem[]>('/api/v1/shop/family-products');
  return products.map(normalizeProduct);
}

export async function fetchManagedShopProducts() {
  getCurrentFamilyUserIdOrThrow();
  const products = await request<ShopProductApiItem[]>('/api/v1/shop/managed-products');
  return products.map(normalizeProduct).filter((product) => product.productId > 0);
}

export async function createFamilyShopProduct(payload: CreateShopProductPayload & {
  childUserId: number; cloverPrice: number; purchasePriceWon?: number | null;
}) {
  getCurrentFamilyUserIdOrThrow();
  const product = await request<ShopProductApiItem>('/api/v1/shop/family-products', {
    method: 'POST', body: JSON.stringify(payload),
  });
  return normalizeProduct(product);
}
