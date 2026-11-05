import type { ShopProduct } from '../types/shop';

export function getCommonShopProducts(products: ShopProduct[]) {
  return products.filter((product) => product.source === 'preset');
}

export function getChildWishProducts(products: ShopProduct[], childUserId?: number) {
  if (!childUserId) return [];
  return products.filter((product) => product.childUserId === childUserId
    && product.cloverPrice > 0
    && (product.source === 'custom' || product.source === 'family'));
}

export function getProductListPath(source?: string) {
  return source === 'custom' || source === 'family' ? '/shop/wishlist' : '/shop';
}
