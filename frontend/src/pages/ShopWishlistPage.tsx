import CardCarousel from '../components/common/CardCarousel';
import { useState } from 'react';
import { useAsyncData } from '../hooks/useAsyncData';
import PageSection from '../components/common/PageSection';
import EmptyState from '../components/common/EmptyState';
import LinkButton from '../components/common/LinkButton';
import StatusMessage from '../components/common/StatusMessage';
import CloverIcon from '../components/common/CloverIcon';
import ShopProductGrid from '../components/shop/ShopProductGrid';
import ShopNavigation from '../components/shop/ShopNavigation';
import { useShop } from '../hooks/useShop';
import { fetchCloverHistory } from '../services/api/cloverApi';
import { getAuthSession } from '../services/session/authSession';
import { getChildWishProducts } from '../utils/shopNavigation';
import styles from './ShopPage.module.css';

const CLOVER_REASON_LABELS: Record<string, string> = {
  DIARY_WRITE: '일기 쓰기',
  STREAK_3: '3일 연속 보너스',
  STREAK_7: '7일 연속 보너스',
  GIFT_REQUEST: '선물 부탁하기',
  GIFT_REQUEST_REFUND: '클로버 돌려받기',
};

function formatHistoryDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', { month: 'long', day: 'numeric' }).format(new Date(value));
}

function ShopWishlistPage() {
  const { data, isLoading, error, refetch } = useShop();
  const { data: historyData } = useAsyncData(fetchCloverHistory, true);
  const history = historyData ?? [];
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);

  const myProducts = getChildWishProducts(data?.products ?? [], getAuthSession()?.userId);

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
            {history.length > 0 ? (
              <button
                type="button"
                className={styles.historyToggle}
                aria-expanded={isHistoryOpen}
                aria-controls="clover-history"
                onClick={() => setIsHistoryOpen((current) => !current)}
              >
                적립 내역 <span aria-hidden="true">{isHistoryOpen ? '−' : '+'}</span>
              </button>
            ) : null}
          </section>

          {history.length > 0 && isHistoryOpen ? (
            <section id="clover-history" className={styles.historyPanel} aria-label="클로버 적립 내역">
              <CardCarousel title="적립 내역" itemLabel="기록">
                {history.slice(0, 10).map((entry) => (
                  <article key={entry.ledgerId} className={styles.historyItem}>
                    <span className={styles.historyReason}>
                      {CLOVER_REASON_LABELS[entry.reason] ?? entry.reason}
                    </span>
                    <span className={styles.historyDate}>{formatHistoryDate(entry.createdAt)}</span>
                    <strong className={entry.amount >= 0 ? styles.historyGain : styles.historySpend}>
                      {entry.amount >= 0 ? `+${entry.amount}` : entry.amount} 클로버
                    </strong>
                  </article>
                ))}
              </CardCarousel>
            </section>
          ) : null}

          <section className={styles.productSection}>
            <div className={styles.sectionTitleRow}>
              <h2 className={styles.sectionTitle}>내가 갖고 싶은 선물 <span className={styles.sectionCount}>{myProducts.length}</span></h2>
              <LinkButton to="/shop/products/new" size="sm">
                <span aria-hidden="true">+</span> 선물 요청
              </LinkButton>
            </div>
            {myProducts.length > 0 ? (
              <ShopProductGrid products={myProducts} cloverBalance={data.cloverBalance} hasRecipients={data.recipients.length > 0} showPurchasePrice={false} />
            ) : (
              <EmptyState title="아직 등록된 선물이 없어요" description="가족이 클로버를 정한 선물이 여기에 표시됩니다.">
                <LinkButton to="/shop/requests" variant="neutral">요청 내역 보기</LinkButton>
              </EmptyState>
            )}
          </section>
        </div>
      ) : null}
    </PageSection>
  );
}

export default ShopWishlistPage;
