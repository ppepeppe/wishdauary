import { Link, useLocation } from 'react-router-dom';
import { useEffect, useRef, useState } from 'react';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import ShopNavigation from '../components/shop/ShopNavigation';
import ProductArtwork from '../components/shop/ProductArtwork';
import RequestCarousel from '../components/shop/RequestCarousel';
import { useAsyncData } from '../hooks/useAsyncData';
import { confirmGiftReceipt, fetchSentGiftRequests, fetchWishProductRequests, fetchShopRecipients } from '../services/api/shopApi';
import { GIFT_REQUESTS_CHANGED_EVENT } from '../constants/events';
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
  approved: { label: '선물 요청 승인됨', className: 'statusApproved' },
  received: { label: '수령 완료', className: 'statusApproved' },
  rejected: { label: '클로버가 돌아왔어요', className: 'statusRejected' },
  pending: { label: '기다리는 중', className: 'statusPending' },
};

function getStatusMeta(status: string) {
  return STATUS_META[status] ?? STATUS_META.pending;
}

async function loadRequests() {
  const [gifts, wishes, recipients] = await Promise.all([fetchSentGiftRequests(), fetchWishProductRequests(), fetchShopRecipients()]);
  return { gifts, wishes, recipients };
}

function ShopSentRequestsPage() {
  const { data, isLoading, error, refetch } = useAsyncData(loadRequests);
  const [receivingId, setReceivingId] = useState<number | null>(null);
  const [receiptError, setReceiptError] = useState<string | null>(null);
  const [receiptMessage, setReceiptMessage] = useState<string | null>(null);
  const receiving = useRef(false);

  async function handleReceipt(notificationId: number) {
    if (receiving.current) return;
    receiving.current = true;
    setReceivingId(notificationId); setReceiptError(null); setReceiptMessage(null);
    try {
      await confirmGiftReceipt(notificationId);
      await refetch();
      window.dispatchEvent(new Event(GIFT_REQUESTS_CHANGED_EVENT));
      setReceiptMessage('선물을 받았다고 기록했어요!');
    } catch (caught) {
      setReceiptError(caught instanceof Error ? caught.message : '수령 완료를 저장하지 못했어요. 다시 눌러 주세요.');
    } finally {
      receiving.current = false;
      setReceivingId(null);
    }
  }
  useEffect(() => {
    const refresh = () => void refetch();
    window.addEventListener('focus', refresh);
    return () => window.removeEventListener('focus', refresh);
  }, [refetch]);
  const requests = data?.gifts;
  const location = useLocation();
  const sent = (location.state as { wishRequestSent?: boolean } | null)?.wishRequestSent;


  return (
    <PageSection title="선물">
      <ShopNavigation />
      {sent ? <StatusMessage kind="success" message="요청을 보냈어요. 가족이 클로버를 정하면 ‘갖고 싶은 선물’에서 볼 수 있어요." /> : null}
      {isLoading ? <StatusMessage kind="loading" message="보낸 부탁을 찾고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="보낸 부탁을 불러오지 못했어요. 잠시 후 다시 봐주세요." /> : null}
      {receiptError ? <StatusMessage kind="error" message={receiptError} /> : null}
      {receiptMessage ? <StatusMessage kind="success" message={receiptMessage} /> : null}

      <div className={styles.container}>
        <Link to="/shop/products/new" className={styles.backLink}>+ 갖고 싶은 선물 요청하기</Link>
        {data && data.wishes.length > 0 ? <RequestCarousel title="클로버 설정 요청">
          {data.wishes.map((wish) => {
            const receiver = data.recipients.find((person) => person.userId === wish.receiverFamilyUserId);
            const ready = wish.cloverPrice > 0;
            return <article key={wish.productId} className={styles.requestCard}>
              <div className={styles.requestHeader}><div className={styles.productSummary}>
                <ProductArtwork title={wish.title} imageUrl={wish.imageUrl} className={styles.photo} />
                <div><h3>{wish.title}</h3><p>{receiver?.relationLabel ?? '가족'}에게 보낸 요청</p></div></div>
                <span className={ready ? styles.statusApproved : styles.statusPending}>{ready ? '클로버 정해졌어요' : '클로버 확인 대기'}</span></div>
              <details className={styles.requestDetails}>
                <summary>선물 설명 보기</summary>
                <p className={styles.message}>{wish.description}</p>
              </details>
              {ready ? <><strong className={styles.priceRow}>{wish.cloverPrice} 클로버</strong><Link to="/shop/wishlist" className={styles.backLink}>갖고 싶은 선물 보기 →</Link></> : <p className={styles.date}>가족이 확인하고 필요한 클로버를 정하고 있어요.</p>}
              {wish.createdAt ? <p className={styles.date}>{formatDate(wish.createdAt)}</p> : null}
            </article>;
          })}
        </RequestCarousel> : null}
        {requests && requests.length > 0 ? (
          <RequestCarousel title="모은 클로버로 보낸 부탁" label="모은 클로버로 보낸 선물 요청">
            {requests.map((request) => {
              const productUrlHost = getProductUrlHost(request.productUrl);
              const purchasePrice = formatWon(request.purchasePriceWon);
              const statusMeta = getStatusMeta(request.status);

              return (
                <article key={request.notificationId} className={styles.requestCard}>
                  <div className={styles.requestHeader}>
                    <div className={styles.productSummary}>
                      <ProductArtwork title={request.productTitle} imageUrl={request.productImageUrl} className={styles.photo} />
                      <div><h2>{request.productTitle}</h2>
                      <p>{request.receiverRelationLabel}에게 보낸 부탁</p></div>
                    </div>
                    <span className={styles[statusMeta.className]}>{statusMeta.label}</span>
                  </div>

                  <div className={styles.priceRow}>
                    <span>{request.cloverPrice} 클로버</span>
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

                  <details className={styles.requestDetails}>
                    <summary>보낸 메시지 보기</summary>
                    <p className={styles.message}>{request.message}</p>
                  </details>
                  <p className={styles.date}>{formatDate(request.createdAt)}</p>
                  {request.status === 'approved' ? <>
                    <p className={styles.date}>선물을 실제로 받았다면 수령 완료를 눌러 주세요.</p>
                    <button type="button" className={styles.receiptButton} disabled={receivingId !== null}
                      onClick={() => void handleReceipt(request.notificationId)}>
                      {receivingId === request.notificationId ? '저장하는 중…' : '수령 완료'}
                    </button>
                  </> : null}
                  {request.status === 'received' && request.receivedAt ? <p className={styles.date}>수령 완료 · {formatDate(request.receivedAt)}</p> : null}
                </article>
              );
            })}
          </RequestCarousel>
        ) : null}

        {data && requests?.length === 0 && data.wishes.length === 0 ? (
          <div className={styles.emptyState}>
            <StatusMessage kind="empty" message="아직 보낸 선물 요청이 없어요. 상점에서 원하는 선물을 골라보세요." />
            <Link to="/shop" className={styles.backLink}>상점에서 선물 고르기 →</Link>
          </div>
        ) : null}
      </div>
    </PageSection>
  );
}

export default ShopSentRequestsPage;
