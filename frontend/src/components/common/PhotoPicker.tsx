import { useEffect, useId, useRef, useState } from 'react';
import { preparePhoto } from '../../utils/photo';
import styles from './PhotoPicker.module.css';

interface PhotoPickerProps {
  value: string | null;
  onChange: (photo: string | null) => void;
  onBusyChange: (busy: boolean) => void;
  disabled?: boolean;
  label?: string;
  description?: string;
  variant?: 'default' | 'compact';
}

function PhotoPicker({ value, onChange, onBusyChange, disabled = false, label = '선물 사진', description = '최대 1장', variant = 'default' }: PhotoPickerProps) {
  const input = useRef<HTMLInputElement>(null);
  const selection = useRef(0);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const hintId = useId();
  const compact = variant === 'compact';

  useEffect(() => () => { selection.current += 1; }, []);

  async function choose(file?: File) {
    if (!file) return;
    const version = ++selection.current;
    setError(null); setBusy(true); onBusyChange(true);
    try {
      const photo = await preparePhoto(file);
      if (version === selection.current) onChange(photo);
    } catch (caught) {
      if (version === selection.current) setError(caught instanceof Error ? caught.message : '사진을 불러오지 못했어요.');
    } finally {
      if (version === selection.current) { setBusy(false); onBusyChange(false); }
    }
  }

  return (
    <div className={styles.field} role="group" aria-label={label} aria-busy={busy}>
      <input ref={input} className={styles.fileInput} type="file" accept="image/*" aria-label={`${label} 선택`} aria-describedby={compact ? undefined : hintId}
        disabled={disabled || busy} onChange={(event) => { void choose(event.target.files?.[0]); event.target.value = ''; }} />
      {compact ? <div className={styles.attachment}>
        <button type="button" className={value ? styles.thumbnailButton : styles.addButton}
          aria-label={`${label} ${value ? '바꾸기' : '추가'}`} title={value ? '사진 바꾸기' : '사진 첨부'}
          disabled={disabled || busy} onClick={() => input.current?.click()}>
          {value ? <img src={value} alt={`${label} 미리보기`} /> : <><span aria-hidden="true">+</span><span>사진</span></>}
        </button>
        {value ? <div className={styles.attachmentCopy}>
          <span className={styles.attachmentLabel}>사진 1/1</span>
          <div className={styles.compactActions}>
            <button type="button" disabled={disabled || busy} onClick={() => input.current?.click()}>바꾸기</button>
            <button type="button" disabled={disabled || busy} onClick={() => { onChange(null); setError(null); }}>삭제</button>
          </div>
        </div> : null}
        {busy ? <span className={styles.busy} role="status">사진 준비 중…</span> : null}
      </div> : <>
      <span className={styles.title}>{label} <small>(선택)</small></span>
      <div className={styles.panel}>
        {value ? <img className={styles.preview} src={value} alt={`${label} 미리보기`} /> : null}
        <div className={styles.content}>
          <p id={hintId}>{busy ? '사진을 준비하고 있어요…' : description}</p>
          <div className={styles.actions}>
            <button type="button" disabled={disabled || busy} onClick={() => input.current?.click()}>{value ? '사진 바꾸기' : '사진 선택하기'}</button>
            {value ? <button type="button" disabled={disabled || busy} onClick={() => { onChange(null); setError(null); }}>사진 삭제</button> : null}
          </div>
          <small>20MB 이하</small>
        </div>
      </div>
      </>}
      {error ? <p className={styles.error} role="alert">{error}</p> : null}
    </div>
  );
}

export default PhotoPicker;
