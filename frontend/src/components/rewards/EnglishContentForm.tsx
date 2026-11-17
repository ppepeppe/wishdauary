import { useRef, useState } from 'react';
import Button from '../common/Button';
import StatusMessage from '../common/StatusMessage';
import { createEnglishContent, updateEnglishContent } from '../../services/api/adminEnglishApi';
import type { EnglishContent } from '../../types/englishContent';
import styles from '../../pages/AdminEnglishPage.module.css';

export default function EnglishContentForm({ initial, onSaved, onCancel, onBusyChange }: {
  initial: EnglishContent | null;
  onSaved: () => Promise<void>;
  onCancel: () => void;
  onBusyChange: (busy: boolean) => void;
}) {
  const [sentence, setSentence] = useState(initial?.sentence ?? '');
  const [meaning, setMeaning] = useState(initial?.meaning ?? '');
  const [busy, setBusy] = useState(false);
  const pending = useRef(false);
  const [error, setError] = useState<string | null>(null);
  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (pending.current || !sentence.trim() || !meaning.trim()) return;
    pending.current = true; setBusy(true); onBusyChange(true); setError(null);
    const body = { sentence: sentence.trim(), meaning: meaning.trim() };
    try {
      if (initial) await updateEnglishContent(initial.contentId, body, initial.version);
      else await createEnglishContent(body);
      await onSaved();
    } catch (caught) { setError(caught instanceof Error ? caught.message : '문장을 저장하지 못했어요.'); }
    finally { pending.current = false; setBusy(false); onBusyChange(false); }
  }
  return <form className={styles.form} aria-label={initial ? '영어 문장 수정' : '영어 문장 추가'} onSubmit={event => void submit(event)}>
    <h2>{initial ? '문장 수정' : '문장 추가'}</h2>
    {error ? <StatusMessage kind="error" message={error} /> : null}
    <fieldset disabled={busy}>
      <label>영어 문장<input lang="en" autoFocus autoComplete="off" required maxLength={120} value={sentence} onChange={event => setSentence(event.target.value)} /></label>
      <label>한국어 뜻<input lang="ko" autoComplete="off" required maxLength={120} value={meaning} onChange={event => setMeaning(event.target.value)} /></label>
      <div className={styles.actions}><Button type="button" variant="neutral" onClick={onCancel}>취소</Button>
        <Button type="submit" disabled={busy || !sentence.trim() || !meaning.trim()}>{busy ? '저장 중…' : initial ? '변경 저장' : '문장 추가'}</Button></div>
    </fieldset>
  </form>;
}
