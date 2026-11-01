import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import ShopProductGrid from '../components/shop/ShopProductGrid';
import { useShop } from '../hooks/useShop';
import styles from './ShopPage.module.css';

function ShopCatalogPage() {
  const { data, isLoading, error, refetch } = useShop();
  const products = data?.products.filter((product) => product.source !== 'custom') ?? [];

  return (
    <PageSection title="상점" description="마음에 드는 선물을 골라 가족에게 요청해 보세요.">
      <Link to="/shop" className={styles.catalogBack}>← 내 선물</Link>
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
              <span className={styles.balanceIcon} aria-hidden="true">☘︎</span>
              <div>
                <p className={styles.balanceLabel}>모은 클로버</p>
                <strong className={styles.balanceValue}>{data.cloverBalance}<small>개</small></strong>
              </div>
            </div>
          </section>
          {data.recipients.length === 0 ? (
            <p className={styles.familyNotice}>
              그룹에 가족을 추가하면 선물을 요청할 수 있어요.
              <Link to="/sharing-groups" className={styles.familyLink}>그룹에서 가족 추가하기 →</Link>
            </p>
          ) : null}
          <div className={styles.catalogProducts}>
            {products.length > 0 ? (
              <ShopProductGrid products={products} cloverBalance={data.cloverBalance} hasRecipients={data.recipients.length > 0} />
            ) : (
              <div className={styles.emptyState}>
                <h3>아직 등록된 선물이 없어요</h3>
                <p>갖고 싶은 선물을 직접 등록할 수도 있어요.</p>
                <Link to="/shop/products/new">원하는 선물 등록하기 →</Link>
              </div>
            )}
          </div>
        </div>
      ) : null}
    </PageSection>
  );
}

export default ShopCatalogPage;
