import { useRef, useState } from 'react';
import Button from '../common/Button';
import StatusMessage from '../common/StatusMessage';
import ShopPhotoPicker from './ShopPhotoPicker';
import { updateCatalogProduct } from '../../services/api/adminShopApi';
import { isSupportedProductUrl, normalizeProductUrl } from '../../utils/productUrl';
import type { ShopProduct } from '../../types/shop';
import styles from './CatalogProductManager.module.css';

export default function CatalogProductEditor({ product, onSaved, onCancel }: {
  product: ShopProduct; onSaved: () => Promise<void>; onCancel: () => void;
}) {
  const [title, setTitle] = useState(product.title);
  const [description, setDescription] = useState(product.description);
  const [clovers, setClovers] = useState(String(product.cloverPrice));
  const [purchasePrice, setPurchasePrice] = useState(product.purchasePriceWon == null ? '' : String(product.purchasePriceWon));
  const [productUrl, setProductUrl] = useState(product.productUrl ?? '');
  const [image, setImage] = useState(product.imageUrl ?? null);
  const [photoChanged, setPhotoChanged] = useState(false);
  const [photoBusy, setPhotoBusy] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const pending = useRef(false);

  async function save(event: React.FormEvent) {
    event.preventDefault();
    if (pending.current || photoBusy || !title.trim() || !description.trim()) return;
    const cloverPrice = Number(clovers);
    const purchasePriceWon = purchasePrice.trim() ? Number(purchasePrice) : null;
    const url = normalizeProductUrl(productUrl);
    if (!Number.isSafeInteger(cloverPrice) || cloverPrice < 1 || cloverPrice > 2147483647
      || (purchasePriceWon !== null && (!Number.isSafeInteger(purchasePriceWon) || purchasePriceWon < 0 || purchasePriceWon > 2147483647))) {
      setError('클로버는 1 이상, 구매가는 0 이상의 정수로 입력해 주세요.'); return;
    }
    if (url && !isSupportedProductUrl(url)) { setError('올바른 쇼핑몰 주소를 입력해 주세요.'); return; }
    pending.current = true; setBusy(true); setError(null);
    try {
      await updateCatalogProduct(product.productId, { title: title.trim(), description: description.trim(),
        cloverPrice, purchasePriceWon, productUrl: url || null,
        imageData: photoChanged ? image : undefined, removeImage: photoChanged && image === null });
      await onSaved();
    } catch (caught) { setError(caught instanceof Error ? caught.message : '상품을 저장하지 못했어요.'); }
    finally { pending.current = false; setBusy(false); }
  }

  return <form className={styles.editor} onSubmit={event => void save(event)} aria-label={`${product.title} 상품 수정`}>
    {error ? <StatusMessage kind="error" message={error} /> : null}
    <fieldset disabled={busy}>
      <label>상품 이름<input required maxLength={120} value={title} onChange={event => setTitle(event.target.value)} autoFocus /></label>
      <label>상품 설명<textarea required maxLength={500} value={description} onChange={event => setDescription(event.target.value)} /></label>
      <div className={styles.prices}>
        <label>필요한 클로버<input type="number" inputMode="numeric" required min="1" max="2147483647" step="1" value={clovers} onChange={event => setClovers(event.target.value)} /></label>
        <label>실제 구매가 (선택)<input type="number" inputMode="numeric" min="0" max="2147483647" step="1" value={purchasePrice} onChange={event => setPurchasePrice(event.target.value)} /></label>
      </div>
      <label>쇼핑몰 링크 (선택)<input inputMode="url" maxLength={1000} value={productUrl} onChange={event => setProductUrl(event.target.value)} /></label>
      <ShopPhotoPicker value={image} onChange={value => { setImage(value); setPhotoChanged(true); }} onBusyChange={setPhotoBusy} disabled={busy} />
      <div className={styles.editorActions}>
        <Button type="button" variant="neutral" disabled={photoBusy} onClick={onCancel}>취소</Button>
        <Button type="submit" disabled={busy || photoBusy || !title.trim() || !description.trim() || !clovers.trim()}>{busy ? '저장 중…' : '상품 저장'}</Button>
      </div>
    </fieldset>
  </form>;
}
