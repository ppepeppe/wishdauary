import { useRef, useState } from 'react';
import Button from '../common/Button';
import StatusMessage from '../common/StatusMessage';
import { useAsyncData } from '../../hooks/useAsyncData';
import { fetchFamilyConnectionRequests, processFamilyConnection } from '../../services/api/familyConnectionApi';
import { GIFT_REQUESTS_CHANGED_EVENT } from '../../constants/events';
import styles from './FamilyConnectionFeed.module.css';

export default function FamilyConnectionFeed() {
  const { data, error, isLoading, refetch } = useAsyncData(fetchFamilyConnectionRequests, true);
  const pending = useRef(false);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  async function act(id: number, action: 'APPROVE' | 'REJECT') {
    if (pending.current) return;
    pending.current = true; setBusy(true); setActionError(null); setMessage(null);
    try {
      await processFamilyConnection(id, action); await refetch();
      window.dispatchEvent(new Event(GIFT_REQUESTS_CHANGED_EVENT));
      setMessage(action === 'APPROVE' ? '가족으로 연결했어요.' : '연결 요청을 거절했어요.');
    } catch (caught) { setActionError(caught instanceof Error ? caught.message : '요청을 처리하지 못했어요.'); }
    finally { pending.current = false; setBusy(false); }
  }
  return <section className={styles.section} aria-labelledby="connection-news-title">
    <h2 id="connection-news-title">가족 연결 요청</h2>
    {isLoading && !data ? <StatusMessage kind="loading" message="연결 요청을 확인하고 있어요." /> : null}
    {error ? <><StatusMessage kind="error" message={error} /><Button onClick={() => void refetch()}>다시 불러오기</Button></> : null}
    {actionError ? <StatusMessage kind="error" message={actionError} /> : null}
    {message ? <StatusMessage kind="success" message={message} /> : null}
    {data?.length === 0 ? <p className={styles.hint}>받은 가족 연결 요청이 없어요.</p> : null}
    {[...(data ?? [])].sort((a, b) => Number(b.status === 'pending') - Number(a.status === 'pending')).map(item => <article key={item.requestId} className={styles.card}>
      <div><strong>{item.familyNickname}</strong><p>{item.relationLabel} · 가족 연결을 요청했어요.</p></div>
      {item.status === 'pending' ? <div className={styles.actions}>
        <Button size="sm" variant="neutral" disabled={busy} onClick={() => void act(item.requestId, 'REJECT')}>거절</Button>
        <Button size="sm" disabled={busy} onClick={() => void act(item.requestId, 'APPROVE')}>승인</Button>
      </div> : <span className={styles.hint}>{item.status === 'approved' ? '승인한 요청' : '거절한 요청'}</span>}
    </article>)}
  </section>;
}
