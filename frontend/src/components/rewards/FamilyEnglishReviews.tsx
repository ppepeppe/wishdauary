import { getChildEnglishRequests } from '../../utils/childRequests';
import { useEffect, useRef, useState } from 'react';
import Button from '../common/Button';
import CardCarousel from '../common/CardCarousel';
import StatusMessage from '../common/StatusMessage';
import { useAsyncData } from '../../hooks/useAsyncData';
import { fetchEnglishReviews, reviewEnglish } from '../../services/api/rewardApi';
import styles from './Rewards.module.css';
import { GIFT_REQUESTS_CHANGED_EVENT } from '../../constants/events';

export default function FamilyEnglishReviews({ onChanged, onPendingCountChange, embedded = false, childId }: {
  childId?: number;
  embedded?: boolean;
  onChanged?: () => Promise<void>;
  onPendingCountChange?: (count: number | undefined) => void;
}) {
  const { data, isLoading, error, refetch } = useAsyncData(fetchEnglishReviews, true);
  useEffect(() => {
    onPendingCountChange?.(!error && data ? (childId === undefined ? data.filter(study => study.status === 'pending') : getChildEnglishRequests(data, childId)).length : undefined);
  }, [data, error, childId, onPendingCountChange]);
  const pending = useRef(new Set<number>());
  const [busyIds, setBusyIds] = useState<number[]>([]);
  const [actionError, setActionError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  async function review(id: number, action: 'APPROVE' | 'REJECT') {
    if (pending.current.has(id)) return;
    pending.current.add(id); setBusyIds(current => [...current, id]); setActionError(null); setMessage(null);
    try {
      await reviewEnglish(id, action);
      await Promise.all([refetch(), onChanged?.()]);
      window.dispatchEvent(new Event(GIFT_REQUESTS_CHANGED_EVENT));
      setMessage(action === 'APPROVE' ? '확인했어요. 아이에게 클로버 1개를 줬어요.' : '아이에게 다시 읽어 달라고 전했어요.');
    } catch (caught) { setActionError(caught instanceof Error ? caught.message : '확인하지 못했어요.'); }
    finally { pending.current.delete(id); setBusyIds(current => current.filter(value => value !== id)); }
  }
  const studies = data ? (childId === undefined ? data.filter(study => study.status === 'pending') : getChildEnglishRequests(data, childId)) : undefined;
  return <section className={styles.reviews} aria-label={embedded ? '영어 확인 요청' : undefined} aria-labelledby={embedded ? undefined : 'english-reviews-title'}>
    {!embedded ? <div className={styles.heading}><h2 id="english-reviews-title">영어 확인 요청 {studies?.length ? <span>{studies.length}</span> : null}</h2></div> : null}
    {isLoading && !data ? <StatusMessage kind="loading" message="영어 확인 요청을 불러오고 있어요." /> : null}
    {error ? <><StatusMessage kind="error" message={error} /><Button variant="ghost" onClick={() => void refetch()}>다시 불러오기</Button></> : null}
    {actionError ? <StatusMessage kind="error" message={actionError} /> : null}
    {message ? <StatusMessage kind="success" message={message} /> : null}
    {!embedded && studies?.length === 0 ? <p className={styles.hint}>확인을 기다리는 영어 공부가 없어요.</p> : null}
    <CardCarousel label="영어 확인 요청" itemLabel="요청">{studies?.map(study => <article key={study.requestId} className={styles.reviewCard}>
      <div className={styles.heading}><strong>{study.childNickname}{embedded ? ' · 영어 확인' : ''}</strong><time dateTime={study.studyDate}>{new Intl.DateTimeFormat('ko-KR',{month:'long',day:'numeric'}).format(new Date(study.studyDate))}</time></div>
      <div className={styles.lesson}><p lang="en">{study.sentence}</p><span>{study.meaning}</span></div>
      <p className={styles.hint}>아이가 읽고 뜻을 이해했는지 확인해 주세요.</p>
      <div className={styles.reviewActions}>
        <Button variant="neutral" size="sm" disabled={busyIds.includes(study.requestId)} onClick={() => void review(study.requestId, 'REJECT')}>다시 해보기</Button>
        <Button size="sm" disabled={busyIds.includes(study.requestId)} onClick={() => void review(study.requestId, 'APPROVE')}>확인했어요 +1</Button>
      </div>
    </article>)}</CardCarousel>
  </section>;
}
