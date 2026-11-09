import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import CloverIcon from '../components/common/CloverIcon';
import ShopProductGrid from '../components/shop/ShopProductGrid';
import ShopNavigation from '../components/shop/ShopNavigation';
import { useShop } from '../hooks/useShop';
import { getCommonShopProducts } from '../utils/shopNavigation';
import styles from './ShopPage.module.css';

function ShopCatalogPage() {
  const { data, isLoading, error, refetch } = useShop();
  const products = getCommonShopProducts(data?.products ?? []);

  return (
    <PageSection title="선물" description="함께 구경하는 공통 상점이에요. 마음에 드는 선물을 골라봐요.">
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
          <div className={styles.catalogProducts}>
            {products.length > 0 ? (
              <ShopProductGrid products={products} cloverBalance={data.cloverBalance} hasRecipients={data.recipients.length > 0} layout="grid" />
            ) : (
              <div className={styles.emptyState}>
                <h3>아직 등록된 선물이 없어요</h3>
                <p>갖고 싶은 선물을 직접 등록할 수도 있어요.</p>
                <Link to="/shop/wishlist">갖고 싶은 선물 모아보기 →</Link>
              </div>
            )}
          </div>
        </div>
      ) : null}
    </PageSection>
  );
}

export default ShopCatalogPage;
