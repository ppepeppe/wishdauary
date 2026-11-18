import CardCarousel from '../components/common/CardCarousel';
import FamilyEnglishReviews from '../components/rewards/FamilyEnglishReviews';
import ProductArtwork from '../components/shop/ProductArtwork';
import { useState } from 'react';
import { useAsyncData } from '../hooks/useAsyncData';
import HoverHint from '../components/common/HoverHint';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { useGiftRequests } from '../hooks/useGiftRequests';
import { fetchChildrenWishGoals } from '../services/api/wishGoalApi';
import { formatWon } from '../utils/currency';
import { getProductUrlHost } from '../utils/productUrl';
import { getPendingGoalRequest } from '../utils/giftRequest';
import styles from './FamilySettingsPage.module.css';

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function FamilySettingsPage() {
  const { data, isLoading, error, processRequest, priceProduct } = useGiftRequests();
  const [processingId, setProcessingId] = useState<number | null>(null);
  const [pricingId, setPricingId] = useState<number | null>(null);
  const [priceInputs, setPriceInputs] = useState<Record<number, string>>({});
  const [purchasePriceInputs, setPurchasePriceInputs] = useState<Record<number, string>>({});
  const [actionError, setActionError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const { data: goals, refetch: refetchGoals } = useAsyncData(fetchChildrenWishGoals, true);
  const childrenGoals = goals ?? [];

  async function handleProcess(notificationId: number, action: 'APPROVE' | 'REJECT') {
    try {
      setProcessingId(notificationId);
      setActionError(null);
      setSuccessMessage(null);
      await processRequest(notificationId, action);
      await refetchGoals();
      setSuccessMessage(action === 'APPROVE' ? '선물 요청을 승인했어요. 아이에게 선물을 전달해 주세요.' : '요청을 거절하고 아이에게 클로버를 돌려줬어요.');
    } catch (caughtError) {
      setActionError(caughtError instanceof Error ? caughtError.message : '선물 요청을 처리하지 못했어요.');
    } finally {
      setProcessingId(null);
    }
  }

  async function handleSetPrice(productId: number) {
    const cloverPrice = Number(priceInputs[productId]);
    const purchasePriceInput = purchasePriceInputs[productId]?.trim() ?? '';
    const purchasePriceWon = purchasePriceInput ? Number(purchasePriceInput) : null;
    if (!Number.isInteger(cloverPrice) || cloverPrice <= 0) {
      setActionError('클로버 가격을 1 이상 숫자로 입력해 주세요.');
      return;
    }
    if (purchasePriceWon !== null && (!Number.isInteger(purchasePriceWon) || purchasePriceWon < 0)) {
      setActionError('실제 구매가는 0 이상 숫자로 입력해 주세요.');
      return;
    }

    try {
      setPricingId(productId);
      setActionError(null);
      setSuccessMessage(null);
      await priceProduct(productId, cloverPrice, purchasePriceWon);
      setSuccessMessage('필요한 클로버를 저장했어요. 아이의 ‘갖고 싶은 선물’에 표시돼요.');
    } catch (caughtError) {
      setActionError(caughtError instanceof Error ? caughtError.message : '가격을 저장하지 못했어요.');
    } finally {
      setPricingId(null);
    }
  }

  const giftRequests = data?.giftRequests ?? [];
  const productPricingRequests = data?.productPricingRequests ?? [];
  const pendingGiftRequestCount = giftRequests.filter((request) => request.status === 'pending').length;

  return (
    <PageSection title="받은 요청">
      {isLoading ? <StatusMessage kind="loading" message="받은 요청을 불러오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="선물 요청을 불러오지 못했어요. 잠시 후 다시 확인해 주세요." /> : null}
      {actionError ? <StatusMessage kind="error" message={actionError} /> : null}
      {successMessage ? <StatusMessage kind="success" message={successMessage} /> : null}

      <FamilyEnglishReviews />

      {childrenGoals.length > 0 ? (
        <div className={styles.goalSection}>
          <CardCarousel title="아이가 모으고 있는 목표" itemLabel="목표">
            {childrenGoals.map((childGoal) => {
              const pending = getPendingGoalRequest(giftRequests, childGoal.goal.productId, childGoal.childUserId);
              const ratio = pending ? 100 :
                childGoal.goal.targetCloverPrice > 0
                  ? Math.min(100, (childGoal.goal.currentCloverBalance / childGoal.goal.targetCloverPrice) * 100)
                  : 0;

              return (
                <article key={childGoal.childUserId} className={styles.goalCard}>
                  <div className={styles.goalHeader}>
                    <div>
                      <h3>{childGoal.goal.productTitle}</h3>
                      <p>{childGoal.childNickname}의 목표</p>
                    </div>
                    <span className={styles.goalBadge}>
                      {pending ? '선물 요청 중' : childGoal.goal.remainingClovers === 0
                        ? '달성!'
                        : `${childGoal.goal.remainingClovers} 클로버 남음`}
                    </span>
                  </div>
                  <div className={styles.goalNumbers}>
                    <span>{pending ? '클로버로 요청했어요' : `${childGoal.goal.currentCloverBalance} 클로버`}</span>
                    <span>{pending?.cloverPrice ?? childGoal.goal.targetCloverPrice} 클로버</span>
                  </div>
                  <div className={styles.goalTrack}>
                    <div className={styles.goalFill} style={{ width: `${ratio}%` }} />
                  </div>
                </article>
              );
            })}
          </CardCarousel>
        </div>
      ) : null}
      {productPricingRequests.length > 0 ? (
        <div className={styles.pricingSection}>
          <CardCarousel title="선물 신청 · 클로버 정하기" itemLabel="상품">
            {productPricingRequests.map((product) => (
              <article key={product.productId} className={styles.requestCard}>
                <div className={styles.requestHeader}>
                  <div className={styles.productSummary}>
                    {product.imageUrl ? <ProductArtwork title={product.title} imageUrl={product.imageUrl} className={styles.photo} /> : null}
                    <div>
                    <h3>{product.title}</h3>
                    <p>{data?.children.find((child) => child.userId === product.childUserId)?.nickname ?? `아이 #${product.childUserId}`}의 부탁</p>
                    <p>{product.description}</p>
                    </div>
                  </div>
                  <span className={styles.price}>대기 중</span>
                </div>
                {product.productUrl ? (
                  <div className={styles.productLinkPanel}>
                    <span>{getProductUrlHost(product.productUrl) ?? '쇼핑몰'}</span>
                    <a className={styles.productLink} href={product.productUrl} target="_blank" rel="noreferrer">
                      확인
                    </a>
                  </div>
                ) : null}
                <div className={styles.priceForm}>
                  <label>
                    <span>클로버</span>
                    <input
                      inputMode="numeric"
                      type="number" min="1" max="2147483647" step="1"
                      value={priceInputs[product.productId] ?? ''}
                      onChange={(event) =>
                        setPriceInputs((current) => ({ ...current, [product.productId]: event.target.value }))
                      }
                    />
                  </label>
                  <label>
                    <span>실제 구매가</span>
                    <input
                      inputMode="numeric"
                      type="number" min="0" max="2147483647" step="1"
                      value={purchasePriceInputs[product.productId] ?? ''}
                      onChange={(event) =>
                        setPurchasePriceInputs((current) => ({ ...current, [product.productId]: event.target.value }))
                      }
                    />
                  </label>
                  <button
                    type="button"
                    disabled={pricingId === product.productId}
                    onClick={() => void handleSetPrice(product.productId)}
                  >
                    저장
                  </button>
                </div>
              </article>
            ))}
          </CardCarousel>
        </div>
      ) : null}

      {giftRequests.length > 0 ? (
        <CardCarousel title={`선물 요청 · 대기 ${pendingGiftRequestCount}개`} itemLabel="요청">
          {giftRequests.map((request) => (
            <article key={request.notificationId} className={styles.requestCard}>
              <div className={styles.requestHeader}>
                <div className={styles.productSummary}>
                  {request.productImageUrl ? <ProductArtwork title={request.productTitle} imageUrl={request.productImageUrl} className={styles.photo} /> : null}
                  <div>
                  <h3>{request.productTitle}</h3>
                  <p>{request.requesterChildNickname}의 요청</p>
                  </div>
                </div>
                <span className={styles.price}>
                  {request.cloverPrice} 클로버
                  {formatWon(request.purchasePriceWon) ? <small>{formatWon(request.purchasePriceWon)}</small> : null}
                </span>
              </div>
              {request.productUrl ? (
                <div className={styles.productLinkPanel}>
                  <span>{getProductUrlHost(request.productUrl) ?? '쇼핑몰'}</span>
                  <a className={styles.productLink} href={request.productUrl} target="_blank" rel="noreferrer">
                    확인
                  </a>
                </div>
              ) : null}
              {request.message ? <p className={styles.requestMessage}>{request.message}</p> : null}
              <div className={styles.requestFooter}>
                <span className={request.status === 'pending' ? styles.statusPending : styles.statusDone}>
                  {request.status === 'pending' ? '대기 중' : request.status === 'approved' ? '승인됨' : request.status === 'received' ? '수령 완료' : '거절됨'}
                </span>
                {request.status === 'pending' ? (
                  <div className={styles.actionGroup}>
                    <HoverHint hint={`승인하면 아이가 낸 클로버 ${request.cloverPrice}개는 사용한 것으로 남아요`}>
                      <button
                        type="button"
                        disabled={processingId === request.notificationId}
                        onClick={() => void handleProcess(request.notificationId, 'APPROVE')}
                      >
                        승인
                      </button>
                    </HoverHint>
                    <HoverHint hint={`거절하면 아이가 낸 클로버 ${request.cloverPrice}개를 그대로 돌려줘요`}>
                      <button
                        type="button"
                        disabled={processingId === request.notificationId}
                        className={styles.rejectButton}
                        onClick={() => void handleProcess(request.notificationId, 'REJECT')}
                      >
                        거절
                      </button>
                    </HoverHint>
                  </div>
                ) : null}
              </div>
              <p className={styles.requestDate}>{formatDate(request.createdAt)}</p>
              {request.status === 'received' && request.receivedAt ? <p className={styles.requestDate}>아이가 선물을 받았어요 · {formatDate(request.receivedAt)}</p> : null}
            </article>
          ))}
        </CardCarousel>
      ) : null}
      {data && giftRequests.length === 0 && productPricingRequests.length === 0 ? (
        <section className={styles.emptyRequests} aria-labelledby="empty-gift-requests-title">
          <h2 id="empty-gift-requests-title">선물 요청</h2>
          <p>확인을 기다리는 선물 요청이 없어요.</p>
        </section>
      ) : null}
    </PageSection>
  );
}

export default FamilySettingsPage;
