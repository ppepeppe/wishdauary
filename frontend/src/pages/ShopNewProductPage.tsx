import { FormEvent, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import HoverHint from '../components/common/HoverHint';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { createShopProduct } from '../services/api/shopApi';
import { getProductUrlHost, isSupportedProductUrl, normalizeProductUrl } from '../utils/productUrl';
import styles from './ShopNewProductPage.module.css';

function ShopNewProductPage() {
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [productUrl, setProductUrl] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const normalizedProductUrl = normalizeProductUrl(productUrl);
  const productUrlHost = useMemo(() => getProductUrlHost(normalizedProductUrl), [normalizedProductUrl]);
  const productUrlInvalid = Boolean(normalizedProductUrl && !isSupportedProductUrl(normalizedProductUrl));
  const submitDisabledReason = isSubmitting
    ? null
    : !title.trim()
      ? '선물 이름을 적으면 저장할 수 있어요'
      : !description.trim()
        ? '어떤 선물인지 한 줄이라도 알려주면 저장할 수 있어요'
        : productUrlInvalid
          ? '쇼핑몰 링크가 올바른 주소인지 확인해 주세요'
          : null;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (productUrlInvalid) {
      setError('쇼핑몰 링크는 http:// 또는 https://로 시작하는 올바른 주소여야 해요.');
      return;
    }

    try {
      setIsSubmitting(true);
      setError(null);
      const product = await createShopProduct({
        title: title.trim(),
        description: description.trim(),
        imageUrl: imageUrl.trim() || null,
        productUrl: normalizedProductUrl || null,
      });
      navigate(`/shop/products/${product.productId}`);
    } catch (caughtError) {
      const message = caughtError instanceof Error ? caughtError.message : '갖고 싶은 선물을 적어두지 못했어요.';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <PageSection
      title="갖고 싶은 선물 적기"
      description="갖고 싶은 선물과 그 이유를 적어보세요."
    >
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <form className={styles.form} onSubmit={(event) => void handleSubmit(event)}>
        <Link to="/shop" className={styles.backLink}>
          내 선물로 돌아가기
        </Link>

        <label className={styles.field}>
          <span>선물 이름</span>
          <input
            value={title}
            maxLength={60}
            required
            placeholder="예: 포켓몬 카드"
            onChange={(event) => setTitle(event.target.value)}
          />
        </label>

        <label className={styles.field}>
          <span>어떤 선물인지 알려주세요</span>
          <textarea
            value={description}
            maxLength={240}
            required
            placeholder="색깔, 모양, 갖고 싶은 이유를 짧게 적어 주세요."
            onChange={(event) => setDescription(event.target.value)}
          />
        </label>

        <label className={styles.field}>
          <span>쇼핑몰 링크</span>
          <input
            type="url"
            value={productUrl}
            placeholder="쿠팡이나 쇼핑몰 주소"
            onChange={(event) => setProductUrl(event.target.value)}
          />
          {productUrlInvalid ? <small className={styles.fieldError}>http:// 또는 https://로 시작하는 주소를 넣어 주세요.</small> : null}
          {productUrlHost ? (
            <div className={styles.linkPreview}>
              <span>연결될 쇼핑몰</span>
              <strong>{productUrlHost}</strong>
            </div>
          ) : null}
        </label>

        <label className={styles.field}>
          <span>이미지 주소</span>
          <input
            value={imageUrl}
            placeholder="선택 사항"
            onChange={(event) => setImageUrl(event.target.value)}
          />
        </label>

        <div className={styles.submitPanel}>
          <HoverHint hint={submitDisabledReason}>
            <button type="submit" disabled={isSubmitting || !title.trim() || !description.trim() || productUrlInvalid}>
              {isSubmitting ? '적어두는 중...' : '선물 적어두기'}
            </button>
          </HoverHint>
        </div>
      </form>
    </PageSection>
  );
}

export default ShopNewProductPage;
