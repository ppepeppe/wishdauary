import { Link } from 'react-router-dom';
import { useMemo, useState } from 'react';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import CloverIcon from '../components/common/CloverIcon';
import NavIcon from '../components/common/NavIcon';
import ShopProductGrid from '../components/shop/ShopProductGrid';
import ShopNavigation from '../components/shop/ShopNavigation';
import { useShop } from '../hooks/useShop';
import { getCatalogListing, type CatalogSort } from '../utils/shopNavigation';
import styles from './ShopPage.module.css';

function ShopCatalogPage() {
  const { data, isLoading, error, refetch } = useShop();
  const [query, setQuery] = useState('');
  const [sort, setSort] = useState<CatalogSort>('clovers-asc');
  const products = useMemo(() => getCatalogListing(data?.products ?? [], query, sort), [data?.products, query, sort]);

  return (
    <PageSection title="선물" description="모은 클로버로 갖고 싶은 선물을 골라봐요.">
      <ShopNavigation />
      {isLoading ? <StatusMessage kind="loading" message="상점을 불러오는 중이에요." /> : null}
      {error ? (
        <div className={styles.loadError}>
          <StatusMessage kind="error" message={error} />
          <button type="button" onClick={() => void refetch()}>다시 불러오기</button>
        </div>
      ) : null}
      {data ? (
        <div className={styles.container}>
          <section className={styles.balancePanel} aria-label="내 클로버">
            <div className={styles.balanceInfo}>
              <span className={styles.balanceIcon} aria-hidden="true"><CloverIcon /></span>
              <div>
                <p className={styles.balanceLabel}>모은 클로버</p>
                <strong className={styles.balanceValue}>{data.cloverBalance}<small>개</small></strong>
              </div>
            </div>
          </section>
          <div className={styles.catalogTools}>
            <label className={styles.searchField}><NavIcon name="search" />
              <input type="search" aria-label="상품 검색" placeholder="어떤 선물을 찾고 있나요?" value={query} onChange={(event) => setQuery(event.target.value)} />
            </label>
            <div className={styles.catalogHeading}><p aria-live="polite">상품 <strong>{products.length}</strong>개</p>
              <select aria-label="상품 정렬" value={sort} onChange={(event) => setSort(event.target.value as CatalogSort)}>
                <option value="clovers-asc">클로버 낮은 순</option><option value="clovers-desc">클로버 높은 순</option><option value="newest">최신 등록 순</option>
              </select>
            </div>
          </div>
          <div className={styles.catalogProducts}>
            {products.length > 0 ? (
              <ShopProductGrid products={products} cloverBalance={data.cloverBalance} hasRecipients={data.recipients.length > 0} layout="grid" />
            ) : (
              <div className={styles.emptyState}>
                <h3>{query.trim() ? '찾는 선물이 없어요' : '아직 등록된 선물이 없어요'}</h3>
                <p>{query.trim() ? '다른 이름으로 검색하거나 가족에게 직접 부탁해 보세요.' : '갖고 싶은 선물을 가족에게 직접 부탁해 보세요.'}</p>
                {query.trim() ? <button type="button" className={styles.clearSearch} onClick={() => setQuery('')}>전체 상품 보기</button> : null}
                <Link to="/shop/products/new">갖고 싶은 선물 요청하기 →</Link>
              </div>
            )}
          </div>
        </div>
      ) : null}
    </PageSection>
  );
}

export default ShopCatalogPage;
