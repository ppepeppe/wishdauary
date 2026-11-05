import { useEffect, useId, useRef, useState } from 'react';
import { prepareShopPhoto } from '../../utils/shopPhoto';
import styles from './ShopPhotoPicker.module.css';

interface ShopPhotoPickerProps {
  value: string | null;
  onChange: (photo: string | null) => void;
  onBusyChange: (busy: boolean) => void;
  disabled?: boolean;
}

function ShopPhotoPicker({ value, onChange, onBusyChange, disabled = false }: ShopPhotoPickerProps) {
  const input = useRef<HTMLInputElement>(null);
  const selection = useRef(0);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const hintId = useId();

  useEffect(() => () => { selection.current += 1; }, []);

  async function choose(file?: File) {
    if (!file) return;
    const version = ++selection.current;
    setError(null); setBusy(true); onBusyChange(true);
    try {
      const photo = await prepareShopPhoto(file);
      if (version === selection.current) onChange(photo);
    } catch (caught) {
      if (version === selection.current) setError(caught instanceof Error ? caught.message : '사진을 불러오지 못했어요.');
    } finally {
      if (version === selection.current) { setBusy(false); onBusyChange(false); }
    }
  }

  return (
    <div className={styles.field} role="group" aria-label="선물 사진">
      <span className={styles.title}>선물 사진 <small>(선택)</small></span>
      <div className={styles.panel}>
        {value ? <img className={styles.preview} src={value} alt="선택한 선물 사진 미리보기" /> : null}
        <div className={styles.content}>
          <p id={hintId}>{busy ? '사진을 준비하고 있어요…' : '갖고 싶은 선물을 사진으로 보여주세요.'}</p>
          <input ref={input} className={styles.fileInput} type="file" accept="image/*" aria-label="선물 사진 선택" aria-describedby={hintId}
            disabled={disabled || busy} onChange={(event) => { void choose(event.target.files?.[0]); event.target.value = ''; }} />
          <div className={styles.actions}>
            <button type="button" disabled={disabled || busy} onClick={() => input.current?.click()}>{value ? '사진 바꾸기' : '사진 선택하기'}</button>
            {value ? <button type="button" disabled={disabled || busy} onClick={() => { onChange(null); setError(null); }}>사진 삭제</button> : null}
          </div>
          <small>20MB 이하 · 사진은 자동으로 크기를 줄여요.</small>
        </div>
      </div>
      {error ? <p className={styles.error} role="alert">{error}</p> : null}
    </div>
  );
}

export default ShopPhotoPicker;
