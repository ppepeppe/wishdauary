import CardCarousel from '../components/common/CardCarousel';
import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../components/common/Button';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { deleteSharingGroup, fetchSharingGroups, saveSharingGroup, searchSharingUsers } from '../services/api/sharingApi';
import type { SharingGroup, SharingUser } from '../types/sharing';
import styles from './SharingGroupsPage.module.css';

export default function SharingGroupsPage() {
  const [groups, setGroups] = useState<SharingGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | undefined>();
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [members, setMembers] = useState<SharingUser[]>([]);
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SharingUser[]>([]);
  const [searched, setSearched] = useState(false);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const busyRef = useRef(false);
  const searchVersion = useRef(0);
  const customGroupCount = groups.filter((group) => !group.defaultGroup).length;

  async function load() {
    setLoading(true);
    setLoadError(null);
    try { setGroups(await fetchSharingGroups()); }
    catch (caught) { setLoadError(caught instanceof Error ? caught.message : '그룹을 불러오지 못했어요.'); }
    finally { setLoading(false); }
  }
  useEffect(() => { void load(); return () => { searchVersion.current += 1; }; }, []);

  function openForm(group?: SharingGroup) {
    setEditingId(group?.id);
    setName(group?.name ?? '');
    setMembers(group?.members ?? []);
    setSearched(false); setQuery(''); setResults([]); setSearchError(null); setSearching(false);
    searchVersion.current += 1;
    setError(null); setMessage(null); setShowForm(true);
  }

  async function search() {
    if (!query.trim()) return;
    const version = ++searchVersion.current;
    setSearching(true); setSearchError(null); setSearched(false);
    try {
      const users = await searchSharingUsers(query.trim());
      if (version === searchVersion.current) { setResults(users); setSearched(true); }
    } catch (caught) {
      if (version === searchVersion.current) setSearchError(caught instanceof Error ? caught.message : '검색하지 못했어요.');
    } finally { if (version === searchVersion.current) setSearching(false); }
  }

  async function save(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busyRef.current || !name.trim()) return;
    busyRef.current = true; setBusy(true); setError(null); setMessage(null);
    try {
      const saved = await saveSharingGroup({ name: name.trim(), memberIds: members.map((member) => member.userId) }, editingId);
      setGroups((current) => editingId === undefined ? [...current, saved] : current.map((group) => group.id === saved.id ? saved : group));
      setShowForm(false); setMessage('그룹을 저장했어요.');
    } catch (caught) { setError(caught instanceof Error ? caught.message : '그룹을 저장하지 못했어요.'); }
    finally { busyRef.current = false; setBusy(false); }
  }

  async function remove(group: SharingGroup) {
    if (busyRef.current || !window.confirm(`‘${group.name}’ 그룹을 삭제할까요? 일기는 남지만, 이 그룹으로만 볼 수 있던 사람은 더 이상 볼 수 없어요. 다른 그룹에 없는 가족과의 연결도 해제돼요.`)) return;
    busyRef.current = true; setBusy(true); setError(null); setMessage(null);
    try {
      await deleteSharingGroup(group.id);
      setGroups((current) => current.filter((item) => item.id !== group.id));
      if (editingId === group.id) setShowForm(false);
      setMessage('그룹을 삭제했어요. 일기는 그대로 보관돼요.');
    } catch (caught) { setError(caught instanceof Error ? caught.message : '그룹을 삭제하지 못했어요.'); }
    finally { busyRef.current = false; setBusy(false); }
  }

  return (
    <PageSection title="그룹 관리" description="일기를 공유할 사람과 선물을 부탁할 가족을 모아두세요. 직접 만드는 그룹은 최대 5개예요.">
      <div className={styles.container}>
        <div className={styles.backLinks}>
          <Link to="/me">← 마이페이지</Link>
          <Link to="/family-connections">가족 연결 요청 확인 →</Link>
        </div>
        {loading ? <StatusMessage kind="loading" message="그룹을 불러오고 있어요." /> : null}
        {loadError ? <div><StatusMessage kind="error" message={loadError} /><Button onClick={() => void load()}>다시 불러오기</Button></div> : null}
        {error ? <StatusMessage kind="error" message={error} /> : null}
        {message ? <StatusMessage kind="success" message={message} /> : null}
        <div className={styles.heading}><strong>직접 만든 그룹 {customGroupCount} / 5</strong><Button disabled={loading || !!loadError || busy || customGroupCount >= 5} onClick={() => openForm()}>새 그룹 만들기</Button></div>
        {customGroupCount >= 5 ? <p className={styles.hint}>5개의 그룹을 모두 만들었어요. 기존 그룹을 수정하거나 삭제할 수 있어요.</p> : null}
        {!loading && !loadError && groups.length === 0 ? <StatusMessage kind="empty" message="첫 그룹을 만들어 볼까요? 예: 우리 가족, 친한 친구" /> : null}
        {showForm ? (
          <form onSubmit={(event) => void save(event)} className={styles.editor}>
            <h2>{editingId === undefined ? '새 그룹 만들기' : '그룹 수정'}</h2>
            <fieldset disabled={busy} className={styles.fields}>
              <label>그룹 이름<input value={name} maxLength={30} required placeholder="예: 나의 응원단" onChange={(event) => setName(event.target.value)} /></label>
              <div className={styles.search}>
                <label>구성원 검색<input value={query} maxLength={50} placeholder="닉네임으로 찾아보세요" onChange={(event) => { setQuery(event.target.value); setSearched(false); setResults([]); setSearchError(null); setSearching(false); searchVersion.current += 1; }} onKeyDown={(event) => { if (event.key === 'Enter') { event.preventDefault(); void search(); } }} /></label>
                <Button type="button" disabled={searching || !query.trim()} onClick={() => void search()}>{searching ? '검색 중…' : '검색'}</Button>
              </div>
              <p className={styles.hint}>닉네임 검색 결과는 최대 20명이에요. 같은 이름은 사용자 번호로 구분해 주세요.</p>
              {searchError ? <StatusMessage kind="error" message={searchError} /> : null}
              {searched && results.length === 0 ? <p role="status" className={styles.hint}>검색 결과가 없어요. 다른 닉네임으로 찾아보세요.</p> : null}
              <div className={styles.results} aria-live="polite">
                {results.map((user) => <div key={user.userId} className={styles.person}><span>{user.nickname} <small>#{user.userId} · {user.appRole === 'child' ? '아이' : '가족'}</small></span><Button type="button" disabled={members.some((member) => member.userId === user.userId)} onClick={() => setMembers((current) => [...current, user])}>추가</Button></div>)}
              </div>
              <h3>선택한 사람 {members.length}명</h3>
              {members.length === 0 ? <p className={styles.hint}>구성원이 없는 그룹의 일기는 나만 볼 수 있어요.</p> : null}
              <div className={styles.results}>{members.map((user) => <div key={user.userId} className={styles.person}><span>{user.nickname} <small>#{user.userId}</small></span><button type="button" onClick={() => setMembers((current) => current.filter((member) => member.userId !== user.userId))} aria-label={`${user.nickname} 제외`}>제외</button></div>)}</div>
              <p className={styles.hint}>구성원을 바꾸면 이 그룹에 공개한 이전 일기에도 바로 적용돼요. 가족을 직접 추가하면 연결에 동의한 것으로 처리되고, 해당 가족이 보낸 연결 요청도 승인돼요.</p>
              <div className={styles.actions}><Button type="submit" disabled={!name.trim()}>{busy ? '저장 중…' : '그룹 저장'}</Button><Button type="button" variant="secondary" onClick={() => setShowForm(false)}>취소</Button></div>
            </fieldset>
          </form>
        ) : null}
        <CardCarousel title="내 그룹" itemLabel="그룹">{groups.map((group) => (
          <article key={group.id} className={styles.card}>
            <div className={styles.heading}><h2>{group.name}</h2><span>{group.defaultGroup ? '기본 · ' : ''}{group.memberIds.length}명</span></div>
            <p>{group.members.map((member) => member.nickname).join(', ') || '아직 구성원이 없어요.'}</p>
            <div className={styles.actions}><Button variant="secondary" disabled={busy} onClick={() => openForm(group)}>수정</Button><button type="button" disabled={busy} onClick={() => void remove(group)}>그룹 삭제</button></div>
          </article>
        ))}</CardCarousel>
      </div>
    </PageSection>
  );
}
