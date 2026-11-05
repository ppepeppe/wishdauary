import CardCarousel from '../components/common/CardCarousel';
import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import Button from '../components/common/Button';
import { useAsyncData } from '../hooks/useAsyncData';
import { fetchFamilyConnectionState, processFamilyConnection, requestFamilyConnection, searchChildren } from '../services/api/familyConnectionApi';
import { getAuthSession } from '../services/session/authSession';
import type { SharingUser } from '../types/sharing';
import styles from './FamilyConnectionsPage.module.css';

function FamilyConnectionsPage() {
  const isFamily = getAuthSession()?.appRole === 'family';
  const { data, isLoading, error, refetch } = useAsyncData(fetchFamilyConnectionState);
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SharingUser[]>([]);
  const [searched, setSearched] = useState(false);
  const [searching, setSearching] = useState(false);
  const [busy, setBusy] = useState(false);
  const busyRef = useRef(false);
  const version = useRef(0);
  const [actionError, setActionError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  useEffect(() => () => { version.current += 1; }, []);
  useEffect(() => {
    const refresh = () => void refetch();
    window.addEventListener('focus', refresh);
    return () => window.removeEventListener('focus', refresh);
  }, [refetch]);

  async function search(event: React.FormEvent) {
    event.preventDefault();
    const current = ++version.current;
    setSearching(true); setActionError(null); setSearched(false);
    try {
      const people = await searchChildren(query.trim());
      if (current === version.current) { setResults(people); setSearched(true); }
    } catch (caught) {
      if (current === version.current) setActionError(caught instanceof Error ? caught.message : '아이를 찾지 못했어요.');
    } finally { if (current === version.current) setSearching(false); }
  }

  async function act(operation: () => Promise<unknown>, success: string) {
    if (busyRef.current) return;
    busyRef.current = true; setBusy(true); setActionError(null); setMessage(null);
    try {
      await operation();
      setMessage(success);
      await refetch();
    } catch (caught) { setActionError(caught instanceof Error ? caught.message : '요청을 처리하지 못했어요.'); }
    finally { busyRef.current = false; setBusy(false); }
  }

  const connectedIds = new Set(data?.connections.map((person) => person.userId) ?? []);
  const pending = data?.requests.filter((item) => item.status === 'pending') ?? [];
  const history = data?.requests.filter((item) => item.status !== 'pending') ?? [];

  return (
    <PageSection title={isFamily ? '아이와 연결하기' : '가족 연결 요청'} description={isFamily
      ? '아이의 닉네임을 찾아 요청을 보내세요. 아이가 승인한 뒤 가족으로 연결돼요.'
      : '아는 가족인지 확인하고 승인해 주세요. 승인 전에는 가족으로 연결되지 않아요.'}>
      <div className={styles.container}>
        <div className={styles.toolbar}>
          <Link to={isFamily ? '/family-home' : '/sharing-groups'} className={styles.backLink}>{isFamily ? '← 홈' : '← 그룹 관리'}</Link>
          <Button type="button" variant="ghost" disabled={isLoading || busy} onClick={() => void refetch()}>새로고침</Button>
        </div>
        {isLoading ? <StatusMessage kind="loading" message="가족 연결을 확인하고 있어요." /> : null}
        {error ? <div><StatusMessage kind="error" message={error} /><Button onClick={() => void refetch()}>다시 불러오기</Button></div> : null}
        {actionError ? <StatusMessage kind="error" message={actionError} /> : null}
        {message ? <StatusMessage kind="success" message={message} /> : null}
        {isFamily ? (
          <section className={styles.section}>
            <h2>아이 찾기</h2>
            <form className={styles.search} onSubmit={(event) => void search(event)}>
              <label>아이 닉네임<input value={query} minLength={2} maxLength={50} required placeholder="두 글자 이상 입력해 주세요" onChange={(event) => {
                setQuery(event.target.value); setResults([]); setSearched(false); setSearching(false); version.current += 1;
              }} /></label>
              <Button type="submit" disabled={searching || query.trim().length < 2}>{searching ? '찾는 중…' : '아이 검색'}</Button>
            </form>
            <p className={styles.hint}>같은 닉네임은 사용자 번호로 구분해 주세요. 검색 결과는 최대 20명이에요.</p>
            {searched && results.length === 0 ? <p role="status">검색된 아이가 없어요. 닉네임을 다시 확인해 주세요.</p> : null}
            <div aria-live="polite"><CardCarousel title="검색된 아이" itemLabel="아이">{results.map((child) => {
              const connected = connectedIds.has(child.userId);
              const requested = pending.some((item) => item.childUserId === child.userId);
              return <div key={child.userId} className={styles.row}>
                <span><strong>{child.nickname}</strong><small>#{child.userId}</small></span>
                <Button disabled={busy || !data || connected || requested} onClick={() => void act(() => requestFamilyConnection(child.userId), `${child.nickname}에게 연결 요청을 보냈어요. 아이의 승인을 기다려 주세요.`)}>
                  {connected ? '연결됨' : requested ? '승인 기다리는 중' : '연결 요청'}
                </Button>
              </div>;
            })}</CardCarousel></div>
          </section>
        ) : null}
        {data ? <>
          <section className={styles.section}>
            <h2>{isFamily ? '보낸 연결 요청' : '받은 연결 요청'} <small>{pending.length}</small></h2>
            {pending.length === 0 ? <p className={styles.hint}>기다리는 연결 요청이 없어요.</p> : <CardCarousel label={isFamily ? '보낸 연결 요청' : '받은 연결 요청'} itemLabel="요청">{pending.map((item) => (
              <article key={item.requestId} className={styles.row}>
                <span><strong>{isFamily ? item.childNickname : item.familyNickname}</strong>
                  <small>#{isFamily ? item.childUserId : item.familyUserId}{!isFamily ? ` · ${item.relationLabel}` : ''}</small></span>
                {isFamily ? <span className={styles.status}>아이의 승인 대기</span> : <div className={styles.actions}>
                  <Button disabled={busy} onClick={() => void act(() => processFamilyConnection(item.requestId, 'APPROVE'), '가족으로 연결됐어요. 그룹 관리의 ‘우리 가족’에서도 확인할 수 있어요.')}>승인</Button>
                  <Button variant="secondary" disabled={busy} onClick={() => void act(() => processFamilyConnection(item.requestId, 'REJECT'), '요청을 거절했어요. 가족으로 연결되지 않았어요.')}>거절</Button>
                </div>}
              </article>
            ))}</CardCarousel>}
          </section>
          <section className={styles.section}>
            <h2>{isFamily ? '연결된 아이' : '연결된 가족'} <small>{data.connections.length}</small></h2>
            {data.connections.length === 0 ? <p className={styles.hint}>아직 연결된 {isFamily ? '아이' : '가족'}가 없어요.</p> : <CardCarousel label={isFamily ? '연결된 아이' : '연결된 가족'} itemLabel="구성원">{data.connections.map((person) => (
              <div key={person.userId} className={styles.row}><span><strong>{person.nickname}</strong><small>#{person.userId}{person.relationLabel ? ` · ${person.relationLabel}` : ''}</small></span><span className={styles.status}>연결됨</span></div>
            ))}</CardCarousel>}
            {!isFamily ? <p className={styles.hint}>연결을 해제하려면 그룹 관리에서 해당 가족을 모든 그룹에서 제외해 주세요.</p> : null}
          </section>
          {history.length > 0 ? <section className={styles.section}><CardCarousel title="처리한 요청" itemLabel="요청">{history.map((item) => {
            const connected = connectedIds.has(isFamily ? item.childUserId : item.familyUserId);
            return <div key={item.requestId} className={styles.row}><span>{isFamily ? item.childNickname : item.familyNickname}</span><small>{item.status === 'rejected' ? '거절됨' : connected ? '승인됨' : '연결 해제됨'}</small></div>;
          })}</CardCarousel></section> : null}
        </> : null}
      </div>
    </PageSection>
  );
}
export default FamilyConnectionsPage;
