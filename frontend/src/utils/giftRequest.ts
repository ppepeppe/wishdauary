import type { ShopFamilyRecipient, ShopProduct } from '../types/shop';

export function getGiftRecipient(
  recipients: ShopFamilyRecipient[],
  product?: Pick<ShopProduct, 'receiverFamilyUserId'> | null,
  selectedId?: number | null,
) {
  return recipients.find((person) => person.userId === selectedId)
    ?? recipients.find((person) => person.userId === product?.receiverFamilyUserId)
    ?? recipients[0] ?? null;
}

export function getGiftRequestBlockReason(
  product: Pick<ShopProduct, 'cloverPrice'>,
  cloverBalance: number,
  hasRecipients: boolean,
): string | null {
  if (product.cloverPrice <= 0) {
    return '가족이 필요한 클로버를 정하고 있어요.';
  }
  if (cloverBalance < product.cloverPrice) {
    return `클로버가 ${product.cloverPrice - cloverBalance}개 더 필요해요.`;
  }
  if (!hasRecipients) {
    return '그룹 관리에서 선물을 요청할 가족을 추가해 주세요.';
  }
  return null;
}
