import { useMemo, useState } from 'react';
import { Link, Navigate, useParams } from 'react-router-dom';
import HoverHint from '../components/common/HoverHint';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import ProductArtwork from '../components/shop/ProductArtwork';
import { useShop } from '../hooks/useShop';
import { useWishGoal } from '../hooks/useWishGoal';
import { formatWon } from '../utils/currency';
import { getProductUrlHost } from '../utils/productUrl';
import { getGiftRequestBlockReason } from '../utils/giftRequest';
import styles from './ShopProductDetailPage.module.css';

function ShopProductDetailPage() {
  const { productId } = useParams();
  const parsedProductId = Number(productId);
  const { data, isLoading, error } = useShop();
  const { goal, isSaving, actionError, saveGoal, removeGoal } = useWishGoal();
  const [selectedRecipientId, setSelectedRecipientId] = useState<number | null>(null);
  const product = data?.products.find((item) => item.productId === parsedProductId) ?? null;
  const selectedRecipient = useMemo(() => {
    if (!data?.recipients.length) {
      return null;
    }

    return data.recipients.find((recipient) => recipient.userId === selectedRecipientId) ?? data.recipients[0];
  }, [data?.recipients, selectedRecipientId]);
  const hasPrice = Boolean(product && product.cloverPrice > 0);
  const productUrlHost = getProductUrlHost(product?.productUrl);
  const isCurrentGoal = Boolean(goal && product && goal.productId === product.productId);
  const requestDisabledReason = product && data
    ? getGiftRequestBlockReason(product, data.cloverBalance, Boolean(selectedRecipient))
    : null;
  const canRequest = Boolean(product && data && !requestDisabledReason);

  if (!Number.isFinite(parsedProductId)) {
    return <Navigate to="/shop" replace />;
  }

  async function handleGoalToggle() {
    if (!product) {
      return;
    }

    if (isCurrentGoal) {
      await removeGoal();
      return;
    }

    await saveGoal(product.productId);
  }

  return (
    <PageSection
      title="선물 상세"
    >
      {isLoading ? <StatusMessage kind="loading" message="선물을 살펴보고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message={error} /> : null}
      {actionError ? <StatusMessage kind="error" message={actionError} /> : null}
      {!isLoading && data && !product ? <StatusMessage kind="empty" message="찾을 수 없는 선물이에요." /> : null}

      {data && product ? (
        <div className={styles.detail}>
          <Link to={product.source !== 'custom' ? '/shop/catalog' : '/shop'} className={styles.backLink}>
            {product.source !== 'custom' ? '← 상점' : '← 내 선물'}
          </Link>
          <section className={styles.hero}>
            <div className={styles.productPanel}>
              <ProductArtwork
                title={product.title}
                imageUrl={product.imageUrl}
                className={styles.imageBox}
              />
              <h2>{product.title}</h2>
              <p className={styles.price}>
                {hasPrice ? `${product.cloverPrice} ☘︎` : '클로버 정하는 중'}
                {formatWon(product.purchasePriceWon) ? <small>{formatWon(product.purchasePriceWon)}</small> : null}
              </p>
              <p>{product.description}</p>
              {product.productUrl ? (
                <div className={styles.linkPanel}>
                  <div>
                    <span>구매 링크</span>
                    <strong>{productUrlHost ?? '쇼핑몰'}</strong>
                  </div>
                  <a className={styles.externalLink} href={product.productUrl} target="_blank" rel="noreferrer">
                    열기
                  </a>
                </div>
              ) : null}
              <div className={styles.balanceRow}>
                <span>내 클로버</span>
                <strong>{data.cloverBalance} ☘︎</strong>
              </div>
              <section className={styles.recipientPanel}>
                <h3>누구에게 부탁할까요?</h3>
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
              <div className={styles.primaryAction}>
                <HoverHint hint={requestDisabledReason}>
                  <Link
                    to={`/shop/products/${product.productId}/request${selectedRecipient ? `?receiverId=${selectedRecipient.userId}` : ''}`}
                    className={canRequest ? styles.requestLink : styles.requestLinkDisabled}
                    aria-disabled={!canRequest}
                    title={requestDisabledReason ?? undefined}
                    onClick={(event) => {
                      if (!canRequest) {
                        event.preventDefault();
                      }
                    }}
                  >
                    선물 요청하기
                  </Link>
                </HoverHint>
              </div>
              {requestDisabledReason ? <p className={styles.helper}>{requestDisabledReason}</p> : null}
              {data.recipients.length === 0 ? <Link to="/sharing-groups" className={styles.secondaryLink}>그룹에서 가족 추가하기 →</Link> : null}
              {hasPrice || isCurrentGoal ? (
                <button
                  type="button"
                  className={isCurrentGoal ? styles.goalButtonActive : styles.goalButton}
                  disabled={isSaving}
                  onClick={() => void handleGoalToggle()}
                >
                  {isSaving ? '저장하는 중...' : isCurrentGoal ? '내 목표예요 (누르면 해제)' : '이 선물을 목표로 정하기 ☘︎'}
                </button>
              ) : null}
              <Link to="/shop/catalog" className={styles.secondaryLink}>
                다른 선물 구경하기
              </Link>
            </div>
          </section>
        </div>
      ) : null}
    </PageSection>
  );
}

export default ShopProductDetailPage;
