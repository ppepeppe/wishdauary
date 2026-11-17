import { useRef, useState } from 'react';
import Button from '../common/Button';
import { Link } from 'react-router-dom';
import CloverIcon from '../common/CloverIcon';
import StatusMessage from '../common/StatusMessage';
import { awardClovers } from '../../services/api/rewardApi';
import type { CloverAwardPayload } from '../../types/rewards';
import styles from './Rewards.module.css';

export default function FamilyCloverAward({ childId, childName, diaryPath }: { childId: number; childName: string; diaryPath: string }) {
  const [open, setOpen] = useState(false);
  const [amount, setAmount] = useState('1');
  const [reason, setReason] = useState('');
  const [busy, setBusy] = useState(false);
  const pending = useRef(false);
  const attempt = useRef<CloverAwardPayload | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    const count = Number(amount), note = reason.trim();
    if (pending.current || !note || !Number.isInteger(count) || count < 1 || count > 1000) return;
    if (!attempt.current || attempt.current.amount !== count || attempt.current.reason !== note) {
      attempt.current = { childUserId: childId, amount: count, reason: note, requestKey: crypto.randomUUID() };
    }
    pending.current = true; setBusy(true); setError(null); setMessage(null);
    try {
      await awardClovers(attempt.current);
      setMessage(`${childName}에게 클로버 ${count}개를 줬어요.`);
      attempt.current = null; setOpen(false); setReason(''); setAmount('1');
    } catch (caught) { setError(caught instanceof Error ? caught.message : '클로버를 주지 못했어요.'); }
    finally { pending.current = false; setBusy(false); }
  }

  return <div className={styles.award}>
    <div className={styles.heading}><h3><Link to={diaryPath}>최근 일기 <span aria-hidden="true">→</span></Link></h3>
      <Button variant="secondary" size="sm" onClick={() => { setOpen(!open); setError(null); setMessage(null); }} disabled={busy}>
        <CloverIcon /> {open ? '닫기' : '클로버 주기'}
      </Button>
    </div>
    {message ? <StatusMessage kind="success" message={message} /> : null}
    {open ? <form className={styles.form} onSubmit={(event) => void submit(event)} aria-label={`${childName}에게 클로버 주기`}>
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <fieldset disabled={busy}>
        <label>클로버 개수<input type="number" inputMode="numeric" min="1" max="1000" step="1" required value={amount} onChange={event => setAmount(event.target.value)} /></label>
        <label>칭찬하는 이유<textarea rows={2} maxLength={300} required value={reason} placeholder="장난감을 스스로 정리했어요" onChange={event => setReason(event.target.value)} /></label>
        <p className={styles.hint}>이유는 아이의 적립 내역에도 남아요.</p>
        <div className={styles.actions}><Button type="button" variant="neutral" onClick={() => setOpen(false)}>취소</Button>
          <Button type="submit" disabled={busy || !reason.trim()}>{busy ? '지급 중…' : '클로버 주기'}</Button></div>
      </fieldset>
    </form> : null}
  </div>;
}
