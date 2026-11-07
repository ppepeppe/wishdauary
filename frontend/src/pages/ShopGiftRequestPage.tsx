import { useMemo, useState } from 'react';
import { Link, Navigate, useParams, useSearchParams } from 'react-router-dom';
import HoverHint from '../components/common/HoverHint';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { useShop } from '../hooks/useShop';
import { formatWon } from '../utils/currency';
import { getGiftRecipient, getGiftRequestBlockReason } from '../utils/giftRequest';
import { getProductListPath } from '../utils/shopNavigation';
import styles from './ShopGiftRequestPage.module.css';

const DEFAULT_MESSAGE = '이 선물이 갖고 싶어요. 모은 클로버로 부탁할게요!';

function ShopGiftRequestPage() {
  const { productId } = useParams();
  const [searchParams] = useSearchParams();
  const parsedProductId = Number(productId);
  const { data, isLoading, error, actionError, lastRequest, pendingProductId, requestProductGift } = useShop();
  const initialReceiverId = Number(searchParams.get('receiverId'));
  const [selectedRecipientId, setSelectedRecipientId] = useState<number | null>(
    Number.isFinite(initialReceiverId) && initialReceiverId > 0 ? initialReceiverId : null,
  );
  const [message, setMessage] = useState(DEFAULT_MESSAGE);

  const product = data?.products.find((item) => item.productId === parsedProductId) ?? null;
  const selectedRecipient = useMemo(
    () => getGiftRecipient(data?.recipients ?? [], product, selectedRecipientId),
    [data?.recipients, product, selectedRecipientId],
  );
  const hasPrice = Boolean(product && product.cloverPrice > 0);
  const hasEnoughClovers = Boolean(product && data && data.cloverBalance >= product.cloverPrice);
  const canSubmit = Boolean(product && selectedRecipient && hasPrice && hasEnoughClovers && pendingProductId === null);
  const submitDisabledReason = product && data
    ? getGiftRequestBlockReason(product, data.cloverBalance, Boolean(selectedRecipient))
    : null;

  if (!Number.isFinite(parsedProductId)) {
    return <Navigate to="/shop" replace />;
  }

  async function handleSubmit() {
    if (!canSubmit || lastRequest || !product || !selectedRecipient) {
      return;
    }

    await requestProductGift({
      productId: product.productId,
      receiverFamilyUserId: selectedRecipient.userId,
      message: message.trim() || DEFAULT_MESSAGE,
    });
  }

  return (
    <PageSection
      title="선물 요청하기"
      description="선물을 부탁할 가족을 고르고, 하고 싶은 말을 적어요."
    >
      {isLoading ? <StatusMessage kind="loading" message="부탁할 준비를 하고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message={error} /> : null}
      {actionError ? <StatusMessage kind="error" message={actionError} /> : null}
      {lastRequest ? (
        <StatusMessage
          kind="success"
          message={`${lastRequest.receiverRelationLabel}에게 ${lastRequest.productTitle} 부탁을 보냈어요. 지금 클로버는 ${lastRequest.remainingCloverBalance}개예요.`}
        />
      ) : null}
      {!isLoading && data && !product ? <StatusMessage kind="empty" message="찾을 수 없는 선물이에요." /> : null}

      {lastRequest ? (
        <div className={styles.successActions}>
          <Link to="/shop/requests" className={styles.submitButton}>요청 내역 확인하기</Link>
          <Link to={getProductListPath(product?.source)} className={styles.backLink}>
            {product?.source === 'preset' ? '상점으로 돌아가기 →' : '갖고 싶은 선물로 돌아가기 →'}
          </Link>
        </div>
      ) : null}

      {data && product && !lastRequest ? (
        <div className={styles.container}>
          <Link to={`/shop/products/${product.productId}`} className={styles.backLink}>
            선물 구경으로 돌아가기
          </Link>

          <section className={styles.summary}>
            <div>
              <p className={styles.summaryLabel}>부탁할 선물</p>
              <h2>{product.title}</h2>
            </div>
            <strong>
              {hasPrice ? `${product.cloverPrice} 클로버` : '클로버 정하는 중'}
              {formatWon(product.purchasePriceWon) ? <small>{formatWon(product.purchasePriceWon)}</small> : null}
            </strong>
          </section>

          <section className={styles.recipientPanel}>
            <h3>누구에게 말할까요?</h3>
            <p className={styles.recipientHint}>부탁을 보낼 가족을 선택해 주세요.</p>
            {data.recipients.length > 0 ? (
              <div className={styles.recipientGrid}>
                {data.recipients.map((recipient) => {
                  const isSelected = selectedRecipient?.userId === recipient.userId;

                  return (
                    <button
                      key={recipient.userId}
                      type="button"
                      className={isSelected ? styles.recipientButtonActive : styles.recipientButton}
                      aria-pressed={isSelected}
                      onClick={() => setSelectedRecipientId(recipient.userId)}
                    >
                      <span>{recipient.relationLabel}</span>
                      <small>{recipient.nickname}</small>
                    </button>
                  );
                })}
              </div>
            ) : (
              <StatusMessage kind="empty" message="그룹에 추가된 가족이 없어요. 그룹 관리에서 가족 계정을 추가해 주세요." />
            )}
          </section>

          <label className={styles.messageField}>
            <span>하고 싶은 말</span>
            <textarea
              value={message}
              maxLength={160}
              onChange={(event) => setMessage(event.target.value)}
            />
          </label>

          <section className={styles.finalPanel}>
            <div>
              <p>마지막 확인</p>
              <strong>{selectedRecipient ? `${selectedRecipient.relationLabel}에게 보내기` : '가족을 먼저 골라주세요'}</strong>
            </div>
            <HoverHint hint={submitDisabledReason}>
              <button type="button" className={styles.submitButton} disabled={!canSubmit} onClick={() => void handleSubmit()}>
                {pendingProductId === product.productId
                  ? '보내는 중...'
                  : '선물 요청 보내기'}
              </button>
            </HoverHint>
            {submitDisabledReason ? <p className={styles.disabledReason}>{submitDisabledReason}</p> : null}
            {data.recipients.length === 0 ? <Link to="/sharing-groups" className={styles.backLink}>그룹에서 가족 추가하기 →</Link> : null}
          </section>
        </div>
      ) : null}
    </PageSection>
  );
}

export default ShopGiftRequestPage;
