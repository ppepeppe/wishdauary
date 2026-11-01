import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import ShopProductGrid from '../components/shop/ShopProductGrid';
import ShopNavigation from '../components/shop/ShopNavigation';
import { useShop } from '../hooks/useShop';
import { fetchCloverHistory, type CloverHistoryEntry } from '../services/api/cloverApi';
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

function ShopPage() {
  const { data, isLoading, error, refetch } = useShop();
  const [history, setHistory] = useState<CloverHistoryEntry[]>([]);
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);

  const myProducts = data?.products.filter((product) => product.source === 'custom') ?? [];

  useEffect(() => {
    let ignore = false;

    async function loadHistory() {
      try {
        const entries = await fetchCloverHistory();
        if (!ignore) {
          setHistory(entries);
        }
      } catch {
        if (!ignore) {
          setHistory([]);
        }
      }
    }

    void loadHistory();

    return () => {
      ignore = true;
    };
  }, []);

  return (
    <PageSection
      title="선물"
      description="모은 클로버로 가족에게 선물을 요청해요."
    >
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
          <section className={styles.balancePanel}>
            <div className={styles.balanceInfo}>
              <span className={styles.balanceIcon} aria-hidden="true">☘︎</span>
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

          {data.recipients.length === 0 ? (
            <p className={styles.familyNotice}>
              그룹에 가족을 추가하면 선물을 요청할 수 있어요.
              <Link to="/sharing-groups" className={styles.familyLink}>그룹에서 가족 추가하기 →</Link>
            </p>
          ) : null}

          {history.length > 0 && isHistoryOpen ? (
            <section id="clover-history" className={styles.historyPanel} aria-label="클로버 적립 내역">
              <ul className={styles.historyList}>
                {history.slice(0, 10).map((entry) => (
                  <li key={entry.ledgerId} className={styles.historyItem}>
                    <span className={styles.historyReason}>
                      {CLOVER_REASON_LABELS[entry.reason] ?? entry.reason}
                    </span>
                    <span className={styles.historyDate}>{formatHistoryDate(entry.createdAt)}</span>
                    <strong className={entry.amount >= 0 ? styles.historyGain : styles.historySpend}>
                      {entry.amount >= 0 ? `+${entry.amount}` : entry.amount} ☘︎
                    </strong>
                  </li>
                ))}
              </ul>
            </section>
          ) : null}

          <Link to="/shop/catalog" className={styles.catalogLink}>
            상점 <span aria-hidden="true">→</span>
          </Link>

          <section className={styles.productSection}>
            <div className={styles.sectionTitleRow}>
              <h2 className={styles.sectionTitle}>직접 등록한 선물 <span className={styles.sectionCount}>{myProducts.length}</span></h2>
              <Link to="/shop/products/new" className={styles.newProductCta}>
                <span aria-hidden="true">+</span> 선물 등록
              </Link>
            </div>
            {myProducts.length > 0 ? (
              <ShopProductGrid products={myProducts} cloverBalance={data.cloverBalance} hasRecipients={data.recipients.length > 0} />
            ) : (
              <div className={styles.emptyState}>
                <h3>상점에 없는 선물도 괜찮아요</h3>
                <p>갖고 싶은 선물을 등록하면 가족이 필요한 클로버를 정해줘요.</p>
                <Link to="/shop/products/new">원하는 선물 등록하기 <span aria-hidden="true">→</span></Link>
              </div>
            )}
          </section>
        </div>
      ) : null}
    </PageSection>
  );
}

export default ShopPage;
