import { useRef, useState } from 'react';
import Button from '../common/Button';
import StatusMessage from '../common/StatusMessage';
import { setProductPrice } from '../../services/api/shopApi';
import type { ShopProduct } from '../../types/shop';
import { formatWon } from '../../utils/currency';
import { getProductUrlHost } from '../../utils/productUrl';
import ProductArtwork from './ProductArtwork';
import styles from './FamilyShopProductRow.module.css';

interface ProductRowProps {
  product: ShopProduct;
  childName: string;
  onUpdated: () => Promise<void>;
}

function ProductPriceEditor({ product, onSaved, onCancel }: {
  product: ShopProduct;
  onSaved: () => Promise<void>;
  onCancel: () => void;
}) {
  const [price, setPrice] = useState(product.cloverPrice > 0 ? String(product.cloverPrice) : '');
  const [purchasePrice, setPurchasePrice] = useState(product.purchasePriceWon == null ? '' : String(product.purchasePriceWon));
  const [busy, setBusy] = useState(false);
  const busyRef = useRef(false);
  const [error, setError] = useState<string | null>(null);

  async function save(event: React.FormEvent) {
    event.preventDefault();
    if (busyRef.current) return;
    const clovers = Number(price);
    const won = purchasePrice.trim() ? Number(purchasePrice) : null;
    if (!Number.isSafeInteger(clovers) || clovers < 1 || clovers > 2147483647
      || (won !== null && (!Number.isSafeInteger(won) || won < 0 || won > 2147483647))) {
      setError('클로버는 1 이상, 실제 구매가는 0 이상의 정수로 입력해 주세요.'); return;
    }
    busyRef.current = true; setBusy(true); setError(null);
    try {
      await setProductPrice(product.productId, clovers, won);
      await onSaved();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '클로버 수를 저장하지 못했어요.');
    } finally { busyRef.current = false; setBusy(false); }
  }

  return (
    <form className={styles.editor} aria-label={`${product.title} 클로버 설정`} onSubmit={(event) => void save(event)}>
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <fieldset disabled={busy}>
        <div className={styles.fields}>
          <label>필요한 클로버<input type="number" inputMode="numeric" min="1" max="2147483647" step="1" required value={price} autoFocus onChange={(event) => setPrice(event.target.value)} /></label>
          <label>실제 구매가 (선택)<input type="number" inputMode="numeric" min="0" max="2147483647" step="1" value={purchasePrice} onChange={(event) => setPurchasePrice(event.target.value)} /></label>
        </div>
        <p className={styles.hint}>저장하면 아이의 갖고 싶은 선물에 반영돼요. 이미 보낸 선물 요청의 클로버는 바뀌지 않아요.</p>
        <div className={styles.actions}>
          <Button type="submit" disabled={busy}>{busy ? '저장 중…' : '클로버 저장'}</Button>
          <Button type="button" variant="secondary" onClick={onCancel}>취소</Button>
        </div>
      </fieldset>
    </form>
  );
}

function FamilyShopProductRow({ product, childName, onUpdated }: ProductRowProps) {
  const [editing, setEditing] = useState(false);
  const priced = product.cloverPrice > 0;
  return (
    <article className={styles.product} aria-label={product.title}>
      <div className={styles.summary}>
        <ProductArtwork title={product.title} imageUrl={product.imageUrl} className={styles.image} />
        <div className={styles.copy}>
          <h3>{product.title}</h3><p>{childName}의 선물</p>
          <strong>{priced ? `${product.cloverPrice} 클로버` : '클로버 설정 대기'}</strong>
          {product.purchasePriceWon != null ? <small>{formatWon(product.purchasePriceWon)}</small> : null}
        </div>
      </div>
      <p className={styles.description}>{product.description}</p>
      <div className={styles.toolbar}>
        {product.productUrl ? <a href={product.productUrl} target="_blank" rel="noreferrer">{getProductUrlHost(product.productUrl) ?? '상품'} 확인 ↗</a> : null}
        {!editing ? <Button type="button" variant={priced ? 'secondary' : 'primary'} size="sm" onClick={() => setEditing(true)}>{priced ? '클로버 수정' : '클로버 정하기'}</Button> : null}
      </div>
      {editing ? <ProductPriceEditor product={product} onCancel={() => setEditing(false)} onSaved={async () => {
        await onUpdated(); setEditing(false);
      }} /> : null}
    </article>
  );
}

export default FamilyShopProductRow;
