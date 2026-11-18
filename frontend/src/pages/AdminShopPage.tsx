import { useRef, useState } from 'react';
import Button from '../components/common/Button';
import PageSection from '../components/common/PageSection';
import FamilyShopNavigation from '../components/shop/FamilyShopNavigation';
import StatusMessage from '../components/common/StatusMessage';
import CatalogProductManager from '../components/shop/CatalogProductManager';
import ShopPhotoPicker from '../components/shop/ShopPhotoPicker';
import { useAsyncData } from '../hooks/useAsyncData';
import { createCatalogProduct, fetchCatalogProducts } from '../services/api/adminShopApi';
import { isSupportedProductUrl, normalizeProductUrl } from '../utils/productUrl';
import styles from './AdminShopPage.module.css';

export default function AdminShopPage() {
  const { data: products, isLoading, error, refetch } = useAsyncData(fetchCatalogProducts, true);
  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [imageData, setImageData] = useState<string | null>(null);
  const [productUrl, setProductUrl] = useState('');
  const [clovers, setClovers] = useState('');
  const [purchasePrice, setPurchasePrice] = useState('');
  const [photoBusy, setPhotoBusy] = useState(false);
  const [busy, setBusy] = useState(false);
  const pending = useRef(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  async function save(event: React.FormEvent) {
    event.preventDefault();
    if (pending.current || photoBusy || !title.trim() || !description.trim()) return;
    const cloverPrice = Number(clovers);
    const purchasePriceWon = purchasePrice.trim() ? Number(purchasePrice) : null;
    const url = normalizeProductUrl(productUrl);
    if (!Number.isSafeInteger(cloverPrice) || cloverPrice <= 0 || cloverPrice > 2147483647
      || (purchasePriceWon !== null && (!Number.isSafeInteger(purchasePriceWon) || purchasePriceWon < 0 || purchasePriceWon > 2147483647))) {
      setActionError('클로버는 1 이상, 구매가는 0 이상의 정수로 입력해 주세요.'); return;
    }
    if (url && !isSupportedProductUrl(url)) { setActionError('올바른 쇼핑몰 주소를 입력해 주세요.'); return; }
    pending.current = true; setBusy(true); setActionError(null); setMessage(null);
    try {
      await createCatalogProduct({ title: title.trim(), description: description.trim(), imageData,
        productUrl: url || null, cloverPrice, purchasePriceWon });
      setTitle(''); setDescription(''); setImageData(null); setProductUrl(''); setClovers(''); setPurchasePrice('');
      setShowForm(false); setMessage('상품을 등록했어요. 모든 아이의 공통 상점에 표시돼요.');
      await refetch();
    } catch (caught) { setActionError(caught instanceof Error ? caught.message : '상품을 등록하지 못했어요.'); }
    finally { pending.current = false; setBusy(false); }
  }

  return <PageSection title="상점 관리">
    <FamilyShopNavigation />
    <div className={styles.container}>
      {isLoading ? <StatusMessage kind="loading" message="공통 상점 상품을 불러오고 있어요." /> : null}
      {error ? <div><StatusMessage kind="error" message={error} /><Button onClick={() => void refetch()}>다시 불러오기</Button></div> : null}
      {actionError ? <StatusMessage kind="error" message={actionError} /> : null}
      {message ? <StatusMessage kind="success" message={message} /> : null}
      <div className={styles.heading}><h2>공통 상품 <small>{products?.length ?? 0}</small></h2>
        <Button disabled={busy} onClick={() => { setShowForm(true); setMessage(null); setActionError(null); }}>+ 상품 등록</Button>
      </div>
      {showForm ? <form className={styles.form} onSubmit={(event) => void save(event)}>
        <h2>공통 상품 등록하기</h2>
        <fieldset disabled={busy}>
          <label>상품 이름<input required maxLength={120} value={title} onChange={(event) => setTitle(event.target.value)} placeholder="예: 함께 고르는 보드게임" /></label>
          <label>상품 설명<textarea required maxLength={500} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="어떤 선물인지 알려주세요." /></label>
          <div className={styles.prices}>
            <label>필요한 클로버<input type="number" required min="1" max="2147483647" step="1" value={clovers} onChange={(event) => setClovers(event.target.value)} /></label>
            <label>실제 구매가 (선택)<input type="number" min="0" max="2147483647" step="1" value={purchasePrice} onChange={(event) => setPurchasePrice(event.target.value)} /></label>
          </div>
          <label>쇼핑몰 링크 (선택)<input inputMode="url" maxLength={1000} value={productUrl} onChange={(event) => setProductUrl(event.target.value)} placeholder="https://" /></label>
          <ShopPhotoPicker value={imageData} onChange={setImageData} onBusyChange={setPhotoBusy} disabled={busy} />
          <div className={styles.actions}>
            <Button type="submit" disabled={busy || photoBusy || !title.trim() || !description.trim() || !clovers.trim()}>{busy ? '등록 중…' : '공통 상점에 등록하기'}</Button>
            <Button type="button" variant="secondary" disabled={busy || photoBusy} onClick={() => { setShowForm(false); setActionError(null); }}>취소</Button>
          </div>
        </fieldset>
      </form> : null}
      {products?.length === 0 ? <StatusMessage kind="empty" message="첫 공통 상품을 등록해 보세요." /> : null}
          <div className={styles.products} aria-label="공통 상품 목록" role="region">
            {products?.map((product) => <CatalogProductManager key={product.productId} product={product} onUpdated={async () => { await refetch(); }} />)}
          </div>
    </div>
  </PageSection>;
}
