import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import ShopNavigation from '../components/shop/ShopNavigation';
import { useAsyncData } from '../hooks/useAsyncData';
import { fetchSentGiftRequests } from '../services/api/shopApi';
import { formatWon } from '../utils/currency';
import { getProductUrlHost } from '../utils/productUrl';
import styles from './ShopSentRequestsPage.module.css';

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

const STATUS_META: Record<string, { label: string; className: 'statusPending' | 'statusApproved' | 'statusRejected' }> = {
  approved: { label: '가족이 좋아했어요', className: 'statusApproved' },
  rejected: { label: '클로버가 돌아왔어요', className: 'statusRejected' },
  pending: { label: '기다리는 중', className: 'statusPending' },
};

function getStatusMeta(status: string) {
  return STATUS_META[status] ?? STATUS_META.pending;
}

function ShopSentRequestsPage() {
  const { data: requests, isLoading, error } = useAsyncData(fetchSentGiftRequests);

  return (
    <PageSection
      title="선물"
      description="가족에게 보낸 선물 요청과 답변을 확인해요."
    >
      <ShopNavigation />
      {isLoading ? <StatusMessage kind="loading" message="보낸 부탁을 찾고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="보낸 부탁을 불러오지 못했어요. 잠시 후 다시 봐주세요." /> : null}

      <div className={styles.container}>
        {requests && requests.length > 0 ? (
          <section className={styles.requestList}>
            {requests.map((request) => {
              const productUrlHost = getProductUrlHost(request.productUrl);
              const purchasePrice = formatWon(request.purchasePriceWon);
              const statusMeta = getStatusMeta(request.status);

              return (
                <article key={request.notificationId} className={styles.requestCard}>
                  <div className={styles.requestHeader}>
                    <div>
                      <h2>{request.productTitle}</h2>
                      <p>{request.receiverRelationLabel}에게 보낸 부탁</p>
                    </div>
                    <span className={styles[statusMeta.className]}>{statusMeta.label}</span>
                  </div>

                  <div className={styles.priceRow}>
                    <span>{request.cloverPrice} ☘︎</span>
                    {purchasePrice ? <strong>{purchasePrice}</strong> : null}
                  </div>

                  {request.productUrl ? (
                    <div className={styles.linkPanel}>
                      <span>{productUrlHost ?? '쇼핑몰'}</span>
                      <a href={request.productUrl} target="_blank" rel="noreferrer">
                        열기
                      </a>
                    </div>
                  ) : null}

                  <p className={styles.message}>{request.message}</p>
                  <p className={styles.date}>{formatDate(request.createdAt)}</p>
                </article>
              );
            })}
          </section>
        ) : null}

        {requests && requests.length === 0 ? (
          <div className={styles.emptyState}>
            <StatusMessage kind="empty" message="아직 보낸 선물 요청이 없어요. 상점에서 원하는 선물을 골라보세요." />
            <Link to="/shop/catalog" className={styles.backLink}>상점에서 선물 고르기 →</Link>
          </div>
        ) : null}
      </div>
    </PageSection>
  );
}

export default ShopSentRequestsPage;
