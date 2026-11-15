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

export type CatalogSort = 'clovers-asc' | 'clovers-desc' | 'newest';

export function getCatalogListing(products: ShopProduct[], query: string, sort: CatalogSort) {
  const keyword = query.trim().toLocaleLowerCase('ko-KR');
  return getCommonShopProducts(products)
    .filter((product) => `${product.title} ${product.description}`.toLocaleLowerCase('ko-KR').includes(keyword))
    .sort((left, right) => {
      if (sort === 'newest') return (right.createdAt ?? '').localeCompare(left.createdAt ?? '') || right.productId - left.productId;
      const difference = sort === 'clovers-desc' ? right.cloverPrice - left.cloverPrice : left.cloverPrice - right.cloverPrice;
      return difference || left.productId - right.productId;
    });
}
