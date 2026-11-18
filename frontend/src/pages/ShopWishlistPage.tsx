import PageSection from '../components/common/PageSection';
import { useSearchParams } from 'react-router-dom';
import Tabs from '../components/common/Tabs';
import EmptyState from '../components/common/EmptyState';
import LinkButton from '../components/common/LinkButton';
import StatusMessage from '../components/common/StatusMessage';
import CloverIcon from '../components/common/CloverIcon';
import ShopProductGrid from '../components/shop/ShopProductGrid';
import ShopNavigation from '../components/shop/ShopNavigation';
import { useShop } from '../hooks/useShop';
import { getAuthSession } from '../services/session/authSession';
import { getChildWishProducts } from '../utils/shopNavigation';
import styles from './ShopPage.module.css';

function ShopWishlistPage() {
  const { data, isLoading, error, refetch } = useShop();
  const [params, setParams] = useSearchParams();
  const category = params.get('category') === 'family' ? 'family' : 'custom';

  const userId = getAuthSession()?.userId;
  const appliedProducts = getChildWishProducts(data?.products ?? [], userId, 'custom');
  const suggestedProducts = getChildWishProducts(data?.products ?? [], userId, 'family');
  const visibleProducts = category === 'custom' ? appliedProducts : suggestedProducts;

  return (
    <PageSection title="선물">
      <ShopNavigation />
      {isLoading ? <StatusMessage kind="loading" message="선물을 불러오는 중이에요." /> : null}
      {error ? (
        <div className={styles.loadError}>
          <StatusMessage kind="error" message={error} />
          <button type="button" onClick={() => void refetch()}>다시 불러오기</button>
        </div>
      ) : null}

      {data ? (
        <div className={styles.container}>
          <section className={styles.balancePanel} aria-label="모은 클로버">
            <div className={styles.balanceInfo}>
              <span className={styles.balanceIcon} aria-hidden="true"><CloverIcon /></span>
              <div>
                <p className={styles.balanceLabel}>모은 클로버</p>
                <strong className={styles.balanceValue}>{data.cloverBalance}<small>개</small></strong>
              </div>
            </div>

          </section>


          <section className={styles.productSection}>
            <div className={styles.sectionTitleRow}>
              <h2 className={styles.sectionTitle}>갖고 싶은 선물</h2>
              <LinkButton to="/shop/products/new" size="sm">
                <span aria-hidden="true">+</span> 선물 신청
              </LinkButton>
            </div>
            <Tabs label="갖고 싶은 선물 구분" value={category} onChange={value => {
              const next = new URLSearchParams(params); next.set('category', value); setParams(next, { replace: true });
            }} options={[
              { value: 'custom', label: `내가 신청한 선물 ${appliedProducts.length}` },
              { value: 'family', label: `가족이 제안한 선물 ${suggestedProducts.length}` },
            ]}>
            {visibleProducts.length > 0 ? (
              <ShopProductGrid key={category} products={visibleProducts} cloverBalance={data.cloverBalance} hasRecipients={data.recipients.length > 0} showPurchasePrice={false} layout="grid" />
            ) : (
              <EmptyState title={category === 'custom' ? '아직 내가 신청한 선물이 없어요' : '아직 가족이 제안한 선물이 없어요'}
                description={category === 'custom' ? '신청한 선물은 가족이 클로버를 정하면 여기에 표시돼요.' : undefined}>
                {category === 'custom' ? <LinkButton to="/shop/requests" variant="neutral">신청 상태 확인하기</LinkButton> : null}
              </EmptyState>
            )}
            </Tabs>
          </section>
        </div>
      ) : null}
    </PageSection>
  );
}

export default ShopWishlistPage;
