export interface ShopProduct {
  productId: number;
  title: string;
  description: string;
  imageUrl?: string | null;
  productUrl?: string | null;
  cloverPrice: number;
  purchasePriceWon?: number | null;
  source: string;
  childUserId?: number | null;
  receiverFamilyUserId?: number | null;
  createdAt?: string;
}

export interface ShopFamilyRecipient {
  userId: number;
  nickname: string;
  relationLabel: string;
}

export interface GiftRequestPayload {
  productId: number;
  receiverFamilyUserId: number;
  message: string;
}

export interface CreateShopProductPayload {
  imageData?: string | null;
  title: string;
  description: string;
  imageUrl?: string | null;
  productUrl?: string | null;
}

export interface CreateWishProductPayload extends CreateShopProductPayload {
  receiverFamilyUserId: number;
}

export interface GiftRequestResult {
  notificationId: number;
  productId: number;
  productTitle: string;
  productUrl?: string | null;
  cloverPrice: number;
  purchasePriceWon?: number | null;
  childUserId: number;
  childNickname: string;
  receiverFamilyUserId: number;
  receiverRelationLabel: string;
  message: string;
  remainingCloverBalance: number;
  status: 'pending' | 'approved' | 'rejected';
  createdAt: string;
}

export interface ReceivedGiftRequest {
  notificationId: number;
  productId: number;
  productTitle: string;
  productImageUrl?: string | null;
  productUrl?: string | null;
  requesterChildUserId: number;
  requesterChildNickname: string;
  receiverFamilyUserId: number;
  receiverRelationLabel: string;
  message: string;
  cloverPrice: number;
  purchasePriceWon?: number | null;
  status: 'pending' | 'approved' | 'rejected';
  processedByFamilyUserId?: number | null;
  processedAt?: string | null;
  createdAt: string;
}
