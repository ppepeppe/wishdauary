import { FormEvent, useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ShopPhotoPicker from '../components/shop/ShopPhotoPicker';
import HoverHint from '../components/common/HoverHint';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { createShopProduct, fetchShopRecipients } from '../services/api/shopApi';
import { useAsyncData } from '../hooks/useAsyncData';
import { getProductUrlHost, isSupportedProductUrl, normalizeProductUrl } from '../utils/productUrl';
import styles from './ShopNewProductPage.module.css';

function ShopNewProductPage() {
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [imageData, setImageData] = useState<string | null>(null);
  const [photoBusy, setPhotoBusy] = useState(false);
  const [productUrl, setProductUrl] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { data: recipients, isLoading: recipientsLoading, error: recipientsError, refetch } = useAsyncData(fetchShopRecipients, true);
  const [receiverId, setReceiverId] = useState('');
  const busyRef = useRef(false);
  const selectedReceiver = recipients?.find((person) => person.userId === Number(receiverId));
  const normalizedProductUrl = normalizeProductUrl(productUrl);
  const productUrlHost = useMemo(() => getProductUrlHost(normalizedProductUrl), [normalizedProductUrl]);
  const productUrlInvalid = Boolean(normalizedProductUrl && !isSupportedProductUrl(normalizedProductUrl));
  const submitDisabledReason = photoBusy ? '사진을 준비하고 있어요' : isSubmitting
    ? null
    : !selectedReceiver
      ? '부탁할 가족을 먼저 골라 주세요'
      : !title.trim()
        ? '선물 이름을 적으면 보낼 수 있어요'
        : !description.trim()
          ? '어떤 선물인지 한 줄이라도 알려주면 보낼 수 있어요'
          : productUrlInvalid
            ? '쇼핑몰 링크가 올바른 주소인지 확인해 주세요'
            : null;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busyRef.current || photoBusy || !selectedReceiver) return;

    if (productUrlInvalid) {
      setError('쇼핑몰 링크는 http:// 또는 https://로 시작하는 올바른 주소여야 해요.');
      return;
    }

    try {
      busyRef.current = true;
      setIsSubmitting(true);
      setError(null);
      await createShopProduct({
        title: title.trim(),
        description: description.trim(),
        imageData,
        productUrl: normalizedProductUrl || null,
        receiverFamilyUserId: selectedReceiver.userId,
      });
      navigate('/shop/requests', { state: { wishRequestSent: true } });
    } catch (caughtError) {
      const message = caughtError instanceof Error ? caughtError.message : '선물 요청을 보내지 못했어요.';
      setError(message);
    } finally {
      setIsSubmitting(false);
      busyRef.current = false;
    }
  }

  return (
    <PageSection
      title="갖고 싶은 선물 요청"
      description="부탁할 가족을 골라 보내세요. 가족이 클로버를 정하면 ‘갖고 싶은 선물’에 보여요."
    >
      {error ? <StatusMessage kind="error" message={error} /> : null}
      {recipientsLoading ? <StatusMessage kind="loading" message="부탁할 가족을 불러오고 있어요." /> : null}
      {recipientsError ? <div><StatusMessage kind="error" message={recipientsError} /><button type="button" onClick={() => void refetch()}>가족 다시 불러오기</button></div> : null}
      {recipients && recipients.length === 0 ? <StatusMessage kind="empty" message="아직 연결된 가족이 없어요. 마이페이지에서 가족 연결을 확인해 주세요." /> : null}
      <form className={styles.form} onSubmit={(event) => void handleSubmit(event)}>
        <Link to="/shop/wishlist" className={styles.backLink}>
          갖고 싶은 선물로 돌아가기
        </Link>

        <div className={styles.field}>
          <label htmlFor="wish-recipient">누구에게 부탁할까요?</label>
          <select id="wish-recipient" required value={receiverId} disabled={isSubmitting || recipientsLoading || !recipients?.length} onChange={(event) => setReceiverId(event.target.value)}>
            <option value="">가족을 선택해 주세요</option>
            {recipients?.map((person) => <option key={person.userId} value={person.userId}>{person.relationLabel} · {person.nickname}</option>)}
          </select>
        </div>

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

        <ShopPhotoPicker value={imageData} onChange={setImageData} onBusyChange={setPhotoBusy} disabled={isSubmitting} />

        <div className={styles.submitPanel}>
          <HoverHint hint={submitDisabledReason}>
            <button type="submit" disabled={isSubmitting || photoBusy || !selectedReceiver || !title.trim() || !description.trim() || productUrlInvalid}>
              {isSubmitting ? '보내는 중...' : selectedReceiver ? `${selectedReceiver.relationLabel}에게 요청 보내기` : '부탁할 가족을 골라주세요'}
            </button>
          </HoverHint>
        </div>
      </form>
    </PageSection>
  );
}

export default ShopNewProductPage;
