import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../components/common/Button';
import PageSection from '../components/common/PageSection';
import AdminNavigation from '../components/common/AdminNavigation';
import StatusMessage from '../components/common/StatusMessage';
import EnglishContentForm from '../components/rewards/EnglishContentForm';
import { useAsyncData } from '../hooks/useAsyncData';
import { fetchEnglishContents } from '../services/api/adminEnglishApi';
import type { EnglishContent } from '../types/englishContent';
import styles from './AdminEnglishPage.module.css';

const PAGE_SIZE = 8;
export default function AdminEnglishPage() {
  const { data, isLoading, error, refetch } = useAsyncData(fetchEnglishContents, true);
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<EnglishContent | null>(null);
  const [busy, setBusy] = useState(false);
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(0);
  const [message, setMessage] = useState<string | null>(null);
  useEffect(() => setPage(0), [query]);
  const today = data?.contents.find(content => content.contentId === data.todayContentId);
  const search = query.trim().toLocaleLowerCase();
  const filtered = [...(data?.contents ?? [])].reverse().filter(content =>
    `${content.sentence} ${content.meaning}`.toLocaleLowerCase().includes(search));
  const pages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const currentPage = Math.min(page, pages - 1);
  const visible = filtered.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);
  function edit(content: EnglishContent) { setEditing(content); setCreating(false); setMessage(null); }
  async function saved() {
    setMessage(creating ? '영어 문장을 추가했어요.' : '영어 문장을 수정했어요.');
    if (creating) { setQuery(''); setPage(0); }
    setEditing(null); setCreating(false); await refetch();
  }
  return <PageSection title="영어 콘텐츠 관리" actions={<Button disabled={busy} onClick={() => { setCreating(true); setEditing(null); setMessage(null); }}>+ 문장 추가</Button>}>
    <AdminNavigation />
    <div className={styles.container}>
      <Link className={styles.backLink} to="/me">← 마이페이지</Link>
      {error ? <StatusMessage kind="error" message={error} /> : null}
      {isLoading && !data ? <StatusMessage kind="loading" message="영어 문장을 불러오고 있어요." /> : null}
      {message ? <StatusMessage kind="success" message={message} /> : null}
      {today ? <section className={styles.today} aria-label="오늘의 영어 문장">
        <div className={styles.heading}><h2>오늘의 문장</h2><Button variant="ghost" size="sm" disabled={busy} onClick={() => edit(today)}>오늘 문장 수정</Button></div>
        <p lang="en">{today.sentence}</p><span>{today.meaning}</span>
      </section> : null}
      <details className={styles.guide}><summary>콘텐츠 반영 기준</summary>
        <p>문장을 추가해도 오늘 선정된 문장은 유지됩니다. 수정은 아직 확인을 요청하지 않은 아이에게 반영되며, 이미 보낸 요청에는 당시 내용이 유지됩니다.</p>
      </details>
      {creating || editing ? <EnglishContentForm key={editing ? `${editing.contentId}-${editing.version}` : 'new'} initial={editing}
        onSaved={saved} onBusyChange={setBusy} onCancel={() => { setEditing(null); setCreating(false); }} /> : null}
      <div className={styles.tools}>
        <label className={styles.search}>문장 찾기<input type="search" value={query} onChange={event => setQuery(event.target.value)} placeholder="영어 또는 한국어" /></label>
        <div className={styles.heading}><span>문장 {filtered.length}개</span><Button variant="ghost" size="sm" disabled={busy || isLoading} onClick={() => { setEditing(null); setCreating(false); void refetch(); }}>목록 새로고침</Button></div>
      </div>
      <div className={styles.list} role="region" aria-label="영어 문장 목록">
        {visible.map(content => <article key={content.contentId} className={styles.content}>
          <div><h3 lang="en">{content.sentence}</h3><p>{content.meaning}</p>{content.contentId === data?.todayContentId ? <small className={styles.todayLabel}>오늘의 문장</small> : null}</div>
          <Button variant="neutral" size="sm" disabled={busy} aria-label={`${content.sentence} 수정`} onClick={() => edit(content)}>수정</Button>
        </article>)}
      </div>
      {data && filtered.length === 0 ? <StatusMessage kind="empty" message={search ? '검색 결과가 없어요.' : '등록된 영어 문장이 없어요.'} /> : null}
      {pages > 1 ? <nav className={styles.pagination} aria-label="문장 페이지">
        <Button variant="ghost" size="sm" disabled={busy || currentPage === 0} onClick={() => setPage(currentPage - 1)}>이전</Button>
        <span role="status" aria-live="polite">{currentPage + 1} / {pages}</span>
        <Button variant="ghost" size="sm" disabled={busy || currentPage === pages - 1} onClick={() => setPage(currentPage + 1)}>다음</Button>
      </nav> : null}
    </div>
  </PageSection>;
}
