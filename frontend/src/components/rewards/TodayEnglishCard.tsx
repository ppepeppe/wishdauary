import { useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../common/Button';
import CloverIcon from '../common/CloverIcon';
import StatusMessage from '../common/StatusMessage';
import { useAsyncData } from '../../hooks/useAsyncData';
import { fetchFamilyConnections } from '../../services/api/familyConnectionApi';
import { fetchTodayEnglish, requestEnglishReview } from '../../services/api/rewardApi';
import styles from './Rewards.module.css';

export default function TodayEnglishCard({ onRewardChanged }: { onRewardChanged: () => Promise<void> }) {
  const today = useAsyncData(fetchTodayEnglish, true);
  const family = useAsyncData(fetchFamilyConnections, true);
  const [choosing, setChoosing] = useState(false);
  const [receiver, setReceiver] = useState('');
  const [busy, setBusy] = useState(false);
  const pending = useRef(false);
  const [error, setError] = useState<string | null>(null);
  const selected = family.data?.find(person => String(person.userId) === receiver)?.userId;
  const study = today.data?.request;

  async function refresh() { await Promise.all([today.refetch(), onRewardChanged()]); }
  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (pending.current || !selected || !today.data) return;
    pending.current = true; setBusy(true); setError(null);
    try { await requestEnglishReview(selected, today.data.studyDate); setChoosing(false); await refresh(); }
    catch (caught) { setError(caught instanceof Error ? caught.message : '확인을 요청하지 못했어요.'); await today.refetch(); }
    finally { pending.current = false; setBusy(false); }
  }

  return <section className={styles.englishCard} aria-labelledby="today-english-title">
    <div className={styles.heading}><h2 id="today-english-title">오늘의 한 줄 영어</h2><span className={styles.reward}><CloverIcon /> +1</span></div>
    {today.isLoading && !today.data ? <StatusMessage kind="loading" message="오늘의 영어를 불러오고 있어요." /> : null}
    {today.error ? <><StatusMessage kind="error" message={today.error} /><Button variant="ghost" onClick={() => void refresh()}>다시 불러오기</Button></> : null}
    {today.data ? <>
      <div className={styles.lesson}><p lang="en">{today.data.content.sentence}</p><span>{today.data.content.meaning}</span></div>
      {study?.status === 'approved' ? <p className={styles.completed}>{study.receiverName} 확인 완료 · 클로버 +1</p> : study?.status === 'pending' ?
        <div className={styles.waiting}><span>{study.receiverName} 확인 기다리는 중</span><Button variant="ghost" size="sm" onClick={() => void refresh()}>상태 확인</Button></div> : <>
          <p className={styles.hint}>{study?.status === 'rejected' ? '한 번 더 읽고 확인을 요청해 보세요.' : '소리 내어 읽고 가족에게 확인받아요.'}</p>
          {family.error ? <><StatusMessage kind="error" message={family.error} /><Button variant="ghost" onClick={() => void family.refetch()}>가족 다시 불러오기</Button></> : null}
          {family.data?.length === 0 ? <Link className={styles.inlineLink} to="/family-connections">가족 연결하기 →</Link> : null}
          {!choosing && family.data?.length ? <Button variant="secondary" fullWidth onClick={() => { setChoosing(true); setError(null); }}>확인 요청</Button> : null}
          {choosing ? <form className={styles.form} onSubmit={(event) => void submit(event)}>
            {error ? <StatusMessage kind="error" message={error} /> : null}
            <fieldset disabled={busy}>
              <label>누구에게 확인받을까요?<select value={receiver} required onChange={event => setReceiver(event.target.value)}>
                <option value="">가족 선택</option>{family.data?.map(person => <option key={person.userId} value={person.userId}>{person.nickname}{person.relationLabel && person.relationLabel !== person.nickname ? ` (${person.relationLabel})` : ''}</option>)}
              </select></label>
              <div className={styles.actions}><Button type="button" variant="neutral" onClick={() => setChoosing(false)}>취소</Button>
                <Button type="submit" disabled={busy || !selected}>{busy ? '요청 중…' : '요청 보내기'}</Button></div>
            </fieldset>
          </form> : null}
        </>}
    </> : null}
  </section>;
}
