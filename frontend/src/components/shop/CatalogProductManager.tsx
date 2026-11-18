import { useRef, useState } from 'react';
import Button from '../common/Button';
import CloverIcon from '../common/CloverIcon';
import StatusMessage from '../common/StatusMessage';
import ProductArtwork from './ProductArtwork';
import { deleteCatalogProduct } from '../../services/api/adminShopApi';
import CatalogProductEditor from './CatalogProductEditor';
import { formatWon } from '../../utils/currency';
import type { ShopProduct } from '../../types/shop';
import styles from './CatalogProductManager.module.css';

export default function CatalogProductManager({ product, onUpdated }: {
  product: ShopProduct; onUpdated: () => Promise<void>;
}) {
  const [mode, setMode] = useState<'view' | 'edit' | 'delete'>('view');
  const [busy, setBusy] = useState(false);
  const pending = useRef(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  function open(next: typeof mode) {
    setError(null); setMessage(null); setMode(next);
  }

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (pending.current) return;
    pending.current = true; setBusy(true); setError(null);
    try {
      await deleteCatalogProduct(product.productId);
      await onUpdated(); setMode('view'); setMessage('상품을 삭제했어요.');
    } catch (caught) { setError(caught instanceof Error ? caught.message : '저장하지 못했어요.'); }
    finally { pending.current = false; setBusy(false); }
  }

  return <article className={styles.product} aria-label={product.title}>
    <div className={styles.summary}>
      <ProductArtwork title={product.title} imageUrl={product.imageUrl} className={styles.photo} />
      <div className={styles.copy}><h3>{product.title}</h3>
        <strong><CloverIcon />{product.cloverPrice} 클로버</strong>
        {formatWon(product.purchasePriceWon) ? <small>실제 구매가 {formatWon(product.purchasePriceWon)}</small> : null}
        <p>{product.description}</p>
        {product.postedByName ? <small>등록 · {product.postedByName}</small> : null}
      </div>
    </div>
    {error ? <StatusMessage kind="error" message={error} /> : null}
    {message ? <StatusMessage kind="success" message={message} /> : null}
    {mode === 'view' ? <div className={styles.actions}>
      {product.productUrl ? <a href={product.productUrl} target="_blank" rel="noreferrer">상품 확인 ↗</a> : <span />}
      <Button variant="neutral" size="sm" onClick={() => open('edit')}>상품 수정</Button>
      <Button variant="ghost" size="sm" onClick={() => open('delete')}>삭제</Button>
    </div> : mode === 'edit' ? <CatalogProductEditor product={product} onCancel={() => open('view')}
      onSaved={async () => { await onUpdated(); setMode('view'); setMessage('상품을 수정했어요.'); }} /> : <form className={styles.editor} onSubmit={(event) => void submit(event)}>
      <fieldset disabled={busy}>
        <div><strong>이 상품을 상점에서 삭제할까요?</strong><p>이미 보낸 선물 요청은 유지됩니다.</p></div>
        <div className={styles.editorActions}>
          <Button type="button" variant="neutral" onClick={() => open('view')}>취소</Button>
          <Button type="submit" variant="danger" disabled={busy}>
            {busy ? '처리 중…' : '상품 삭제'}
          </Button>
        </div>
      </fieldset>
    </form>}
  </article>;
}
