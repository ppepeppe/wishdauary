import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import Button from '../components/common/Button';
import FamilyShopProductRow from '../components/shop/FamilyShopProductRow';
import { useAsyncData } from '../hooks/useAsyncData';
import { fetchFamilyConnections } from '../services/api/familyConnectionApi';
import { createFamilyShopProduct, fetchManagedShopProducts } from '../services/api/shopApi';
import { isSupportedProductUrl, normalizeProductUrl } from '../utils/productUrl';
import styles from './FamilyShopPage.module.css';

async function loadShop() {
  const [children, products] = await Promise.all([fetchFamilyConnections(), fetchManagedShopProducts()]);
  return { children, products };
}

function FamilyShopPage() {
  const { data, isLoading, error, refetch } = useAsyncData(loadShop);
  const [showForm, setShowForm] = useState(false);
  const [childId, setChildId] = useState('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [productUrl, setProductUrl] = useState('');
  const [price, setPrice] = useState('');
  const [purchasePrice, setPurchasePrice] = useState('');
  const [busy, setBusy] = useState(false);
  const busyRef = useRef(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const targetId = Number(childId || data?.children[0]?.userId);
  const childProducts = data?.products.filter((product) => product.source === 'custom') ?? [];
  const familyProducts = data?.products.filter((product) => product.source === 'family') ?? [];

  useEffect(() => {
    const refresh = () => void refetch();
    window.addEventListener('focus', refresh);
    return () => window.removeEventListener('focus', refresh);
  }, [refetch]);

  async function refreshPrice() {
    setMessage('클로버 수를 저장했어요. 아이의 상점에도 반영돼요.');
    await refetch();
  }

  async function save(event: React.FormEvent) {
    event.preventDefault();
    if (busyRef.current || !data?.children.some((child) => child.userId === targetId)) return;
    const cloverPrice = Number(price);
    const purchasePriceWon = purchasePrice.trim() ? Number(purchasePrice) : null;
    const url = normalizeProductUrl(productUrl);
    if (!Number.isSafeInteger(cloverPrice) || cloverPrice <= 0 || (purchasePriceWon !== null && (!Number.isSafeInteger(purchasePriceWon) || purchasePriceWon < 0))) {
      setActionError('클로버는 1 이상, 실제 구매가는 0 이상의 정수로 입력해 주세요.'); return;
    }
    if (url && !isSupportedProductUrl(url)) { setActionError('올바른 쇼핑몰 주소를 입력해 주세요.'); return; }
    busyRef.current = true; setBusy(true); setActionError(null); setMessage(null);
    try {
      await createFamilyShopProduct({ childUserId: targetId, title: title.trim(), description: description.trim(),
        imageUrl: imageUrl.trim() || null, productUrl: url || null, cloverPrice, purchasePriceWon });
      setShowForm(false); setTitle(''); setDescription(''); setPrice(''); setPurchasePrice(''); setImageUrl(''); setProductUrl('');
      setMessage('선물을 등록했어요. 선택한 아이의 상점에 표시돼요.');
      await refetch();
    } catch (caught) { setActionError(caught instanceof Error ? caught.message : '선물을 등록하지 못했어요.'); }
    finally { busyRef.current = false; setBusy(false); }
  }

  return (
    <PageSection title="상점 관리" description="아이가 올린 상품의 클로버를 정하고 수정해요. 가족이 직접 선물을 등록할 수도 있어요.">
      <div className={styles.container}>
        {isLoading ? <StatusMessage kind="loading" message="상점을 불러오고 있어요." /> : null}
        {error ? <div><StatusMessage kind="error" message={error} /><Button onClick={() => void refetch()}>다시 불러오기</Button></div> : null}
        {actionError ? <StatusMessage kind="error" message={actionError} /> : null}
        {message ? <StatusMessage kind="success" message={message} /> : null}
        {data && data.children.length === 0 ? <div className={styles.empty}>
          <p>아이의 연결 승인을 받은 뒤 선물을 등록할 수 있어요.</p><Link to="/family-connections">아이 찾고 연결 요청하기 →</Link>
        </div> : null}
        {data && data.children.length > 0 ? <>
          <div className={styles.heading}><h2>상품과 클로버 관리</h2><Button onClick={() => { setShowForm(true); setMessage(null); }}>+ 선물 등록</Button></div>
          {showForm ? <form className={styles.form} onSubmit={(event) => void save(event)}>
            <h2>선물 등록하기</h2>
            <fieldset disabled={busy}>
              <label>어떤 아이의 상점에 보여줄까요?<select value={String(targetId)} onChange={(event) => setChildId(event.target.value)}>{data.children.map((child) => <option key={child.userId} value={child.userId}>{child.nickname} · #{child.userId}</option>)}</select></label>
              <label>선물 이름<input required maxLength={120} value={title} onChange={(event) => setTitle(event.target.value)} placeholder="예: 함께 고르는 운동화" /></label>
              <label>선물 설명<textarea required maxLength={500} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="어떤 선물인지 알려주세요." /></label>
              <div className={styles.prices}>
                <label>필요한 클로버<input type="number" min="1" step="1" required value={price} onChange={(event) => setPrice(event.target.value)} /></label>
                <label>실제 구매가 (선택)<input type="number" min="0" step="1" value={purchasePrice} onChange={(event) => setPurchasePrice(event.target.value)} /></label>
              </div>
              <label>쇼핑몰 링크 (선택)<input type="url" maxLength={1000} value={productUrl} onChange={(event) => setProductUrl(event.target.value)} /></label>
              <label>이미지 주소 (선택)<input type="url" maxLength={500} value={imageUrl} onChange={(event) => setImageUrl(event.target.value)} /></label>
              <div className={styles.actions}><Button type="submit" disabled={busy || !title.trim() || !description.trim()}>{busy ? '등록 중…' : '상점에 등록하기'}</Button><Button type="button" variant="secondary" onClick={() => setShowForm(false)}>취소</Button></div>
            </fieldset>
          </form> : null}
          <section className={styles.productSection} aria-labelledby="child-products-title">
            <h2 id="child-products-title">아이가 올린 상품 <small>{childProducts.length}</small></h2>
            <p className={styles.hint}>클로버를 정하면 아이가 모아서 선물을 요청할 수 있어요. 저장한 뒤에도 수정할 수 있어요.</p>
            {childProducts.length === 0 ? <p className={styles.hint}>아이가 갖고 싶은 상품을 올리면 여기에 보여요.</p> : childProducts.map((product) => (
              <FamilyShopProductRow key={product.productId} product={product} childName={data.children.find((child) => child.userId === product.childUserId)?.nickname ?? '아이'} onUpdated={refreshPrice} />
            ))}
          </section>
          <section className={styles.productSection} aria-labelledby="family-products-title">
            <h2 id="family-products-title">가족이 등록한 선물 <small>{familyProducts.length}</small></h2>
            {familyProducts.length === 0 ? <p className={styles.hint}>아직 등록한 선물이 없어요. 아이에게 제안할 선물을 올려보세요.</p> : familyProducts.map((product) => (
              <FamilyShopProductRow key={product.productId} product={product} childName={data.children.find((child) => child.userId === product.childUserId)?.nickname ?? '아이'} onUpdated={refreshPrice} />
            ))}
          </section>
        </> : null}
        <div className={styles.footer}>
          <Link to="/family-settings" className={styles.link}>아이가 보낸 선물 요청 보기 →</Link>
          <Button type="button" variant="ghost" disabled={isLoading || busy} onClick={() => void refetch()}>새로고침</Button>
        </div>
      </div>
    </PageSection>
  );
}
export default FamilyShopPage;
