import CardCarousel from '../common/CardCarousel';
import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import ProductArtwork from './ProductArtwork';
import { useAsyncData } from '../../hooks/useAsyncData';
import { fetchSentGiftRequests } from '../../services/api/shopApi';
import type { ReceivedGiftRequest, ShopProduct } from '../../types/shop';
import { formatWon } from '../../utils/currency';
import { getProductUrlHost } from '../../utils/productUrl';
import { getGiftRequestBlockReason } from '../../utils/giftRequest';
import styles from './ShopProductGrid.module.css';

interface ShopProductGridProps {
  products: ShopProduct[];
  cloverBalance: number;
  hasRecipients: boolean;
  showPurchasePrice?: boolean;
  layout?: 'grid' | 'carousel';
}

const REQUEST_STATUS_META: Record<string, { label: string; className: 'requestPending' | 'requestApproved' | 'requestRejected' }> = {
  pending: { label: '답변 기다리는 중', className: 'requestPending' },
  approved: { label: '승인됨', className: 'requestApproved' },
  rejected: { label: '거절됨', className: 'requestRejected' },
};

function ShopProductGrid({ products, cloverBalance, hasRecipients, showPurchasePrice = true, layout = 'carousel' }: ShopProductGridProps) {
  const { data: sentRequests } = useAsyncData(fetchSentGiftRequests, true);
  const requestStatusByProductId = useMemo(() => {
    const statusMap = new Map<number, ReceivedGiftRequest['status']>();
    // 목록이 최신순이므로, 상품별로 가장 최근 요청 상태만 남긴다.
    (sentRequests ?? []).forEach((request) => {
      if (!statusMap.has(request.productId)) {
        statusMap.set(request.productId, request.status);
      }
    });
    return statusMap;
  }, [sentRequests]);

  function renderProduct(product: ShopProduct) {
    const hasPrice = product.cloverPrice > 0;
    const productUrlHost = getProductUrlHost(product.productUrl);
    const requestStatus = requestStatusByProductId.get(product.productId);
    const requestMeta = requestStatus ? REQUEST_STATUS_META[requestStatus] : null;
    const blockedReason = getGiftRequestBlockReason(product, cloverBalance, hasRecipients);

    return (
      <article key={product.productId} className={styles.product}>
        <Link to={`/shop/products/${product.productId}`} className={styles.productMainLink}>
          <ProductArtwork
            title={product.title}
            imageUrl={product.imageUrl}
            className={styles.productImage}
          />
          <div className={styles.productBody}>
            <div className={styles.productHeader}>
              <h3>{product.title}</h3>
              <strong>
                {hasPrice ? `${product.cloverPrice} 클로버` : '클로버 정하는 중'}
                {showPurchasePrice && formatWon(product.purchasePriceWon) ? <small>{formatWon(product.purchasePriceWon)}</small> : null}
              </strong>
            </div>
            <p>{product.description}</p>
            <div className={styles.badgeRow}>
              {product.source === 'family' ? <span className={styles.sourceBadge}>가족이 골라준 선물</span> : null}
              {requestMeta ? <span className={styles[requestMeta.className]}>{requestMeta.label}</span> : null}
              {productUrlHost ? <span className={styles.sourceBadge}>{productUrlHost}</span> : null}
            </div>
          </div>
        </Link>
        <div className={styles.productAction}>
          {blockedReason ? (
            <button type="button" className={styles.requestButton} disabled aria-describedby={`request-reason-${product.productId}`}>
              선물 요청하기
            </button>
          ) : (
            <Link to={`/shop/products/${product.productId}/request`} className={styles.requestButton} aria-label={`${product.title} 선물 요청하기`}>
              선물 요청하기
            </Link>
          )}
          {blockedReason ? <p id={`request-reason-${product.productId}`} className={styles.requestHint}>{blockedReason}</p> : null}
        </div>
      </article>
    );
  }

  if (layout === 'grid') {
    return <div className={styles.productGrid} role="region" aria-label="선물 둘러보기">{products.map(renderProduct)}</div>;
  }
  return <CardCarousel title="선물 둘러보기" itemLabel="선물">{products.map(renderProduct)}</CardCarousel>;
}

export default ShopProductGrid;
