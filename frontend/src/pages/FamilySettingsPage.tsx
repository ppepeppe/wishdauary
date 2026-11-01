import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import HoverHint from '../components/common/HoverHint';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { useGiftRequests } from '../hooks/useGiftRequests';
import { fetchChildrenWishGoals } from '../services/api/wishGoalApi';
import type { ChildWishGoal } from '../types/wishGoal';
import { formatWon } from '../utils/currency';
import { getProductUrlHost } from '../utils/productUrl';
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
  const [childrenGoals, setChildrenGoals] = useState<ChildWishGoal[]>([]);

  useEffect(() => {
    let ignore = false;

    async function loadChildrenGoals() {
      try {
        const goals = await fetchChildrenWishGoals();
        if (!ignore) {
          setChildrenGoals(goals);
        }
      } catch {
        if (!ignore) {
          setChildrenGoals([]);
        }
      }
    }

    void loadChildrenGoals();

    return () => {
      ignore = true;
    };
  }, []);

  async function handleProcess(notificationId: number, action: 'APPROVE' | 'REJECT') {
    try {
      setProcessingId(notificationId);
      setActionError(null);
      await processRequest(notificationId, action);
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
      await priceProduct(productId, cloverPrice, purchasePriceWon);
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
    <PageSection
      title="받은 선물 요청"
      description="아이가 모은 클로버로 보낸 선물 요청을 확인할 수 있어요."
    >
      <p><Link to="/family-shop">상품 등록·클로버 수정하기 →</Link> · <Link to="/family-connections">아이와 연결하기 →</Link></p>
      {isLoading ? <StatusMessage kind="loading" message="받은 요청을 불러오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="선물 요청을 불러오지 못했어요. 잠시 후 다시 확인해 주세요." /> : null}
      {actionError ? <StatusMessage kind="error" message={actionError} /> : null}
      {data && pendingGiftRequestCount > 0 ? (
        <StatusMessage kind="success" message={`확인할 선물 요청이 ${pendingGiftRequestCount}개 있어요.`} />
      ) : null}

      {childrenGoals.length > 0 ? (
        <div className={styles.goalSection}>
          <h2>아이가 모으고 있는 목표</h2>
          <div className={styles.goalList}>
            {childrenGoals.map((childGoal) => {
              const ratio =
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
                      {childGoal.goal.remainingClovers === 0
                        ? '달성!'
                        : `${childGoal.goal.remainingClovers} ☘︎ 남음`}
                    </span>
                  </div>
                  <div className={styles.goalNumbers}>
                    <span>{childGoal.goal.currentCloverBalance} ☘︎</span>
                    <span>{childGoal.goal.targetCloverPrice} ☘︎</span>
                  </div>
                  <div className={styles.goalTrack}>
                    <div className={styles.goalFill} style={{ width: `${ratio}%` }} />
                  </div>
                </article>
              );
            })}
          </div>
        </div>
      ) : null}
      {productPricingRequests.length > 0 ? (
        <div className={styles.pricingSection}>
          <h2>가격 정하기</h2>
          <div className={styles.requestList}>
            {productPricingRequests.map((product) => (
              <article key={product.productId} className={styles.requestCard}>
                <div className={styles.requestHeader}>
                  <div>
                    <h3>{product.title}</h3>
                    <p>{product.description}</p>
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
                      placeholder="예: 12"
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
                      placeholder="예: 12900"
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
          </div>
        </div>
      ) : null}

      {giftRequests.length > 0 ? (
        <div className={styles.requestList}>
          {giftRequests.map((request) => (
            <article key={request.notificationId} className={styles.requestCard}>
              <div className={styles.requestHeader}>
                <div>
                  <h3>{request.productTitle}</h3>
                  <p>{request.requesterChildNickname}의 요청</p>
                </div>
                <span className={styles.price}>
                  {request.cloverPrice} ☘︎
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
              <p className={styles.requestMessage}>{request.message}</p>
              <div className={styles.requestFooter}>
                <span className={request.status === 'pending' ? styles.statusPending : styles.statusDone}>
                  {request.status === 'pending' ? '대기 중' : request.status === 'approved' ? '승인됨' : '거절됨'}
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
            </article>
          ))}
        </div>
      ) : null}
      {data && giftRequests.length === 0 && productPricingRequests.length === 0 ? (
        <StatusMessage kind="empty" message="아직 도착한 선물 요청이 없어요. 아이에게 연결을 요청하고 승인을 받으면 선물을 부탁받을 수 있어요." />
      ) : null}
    </PageSection>
  );
}

export default FamilySettingsPage;
