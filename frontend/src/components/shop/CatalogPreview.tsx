import Button from '../common/Button';
import StatusMessage from '../common/StatusMessage';
import ProductArtwork from './ProductArtwork';
import { useAsyncData } from '../../hooks/useAsyncData';
import { fetchCatalogProducts } from '../../services/api/adminShopApi';
import { formatWon } from '../../utils/currency';
import type { ShopProduct } from '../../types/shop';
import styles from './ShopProductGrid.module.css';
import previewStyles from './CatalogPreview.module.css';

export function CatalogPreviewProducts({ products }: { products: ShopProduct[] }) {
  if (products.length === 0) return <StatusMessage kind="empty" message="등록된 공통 상품이 없어요." />;
  return <div className={styles.productGrid} role="region" aria-label="공통 상점 상품 미리보기">
    {products.map(product => <article className={styles.product} key={product.productId}>
      <div className={styles.productMainLink}>
        <ProductArtwork title={product.title} imageUrl={product.imageUrl} className={styles.productImage} />
        <div className={styles.productBody}>
          <div className={styles.productHeader}><h3>{product.title}</h3><strong>{product.cloverPrice} 클로버</strong></div>
          <p>{product.description}</p>
          {product.postedByName ? <p className={styles.attribution}>등록 · {product.postedByName}</p> : null}
        </div>
      </div>
      <details className={previewStyles.details}>
        <summary>상품 자세히</summary>
        <h4>{product.title}</h4><p>{product.description}</p>
        {formatWon(product.purchasePriceWon) ? <p>실제 구매가 {formatWon(product.purchasePriceWon)}</p> : null}
        {product.productUrl ? <a href={product.productUrl} target="_blank" rel="noreferrer">쇼핑몰 상품 확인 ↗</a> : null}
      </details>
    </article>)}
  </div>;
}

export default function CatalogPreview() {
  const { data, isLoading, error, refetch } = useAsyncData(fetchCatalogProducts, true);
  return <>
    {isLoading ? <StatusMessage kind="loading" message="공통 상점을 불러오고 있어요." /> : null}
    {error ? <><StatusMessage kind="error" message={error} /><Button onClick={() => void refetch()}>다시 불러오기</Button></> : null}
    {data ? <CatalogPreviewProducts products={data} /> : null}
  </>;
}
