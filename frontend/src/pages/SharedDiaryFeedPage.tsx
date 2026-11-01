import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../components/common/Button';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { formatKoreanDate } from '../constants/date';
import { getVisibilityScopeLabel } from '../constants/diary';
import { fetchSharedDiaryFeed, type SharedDiaryItem } from '../services/api/diaryApi';
import { getAuthSession } from '../services/session/authSession';
import styles from './SharingGroupsPage.module.css';

export default function SharedDiaryFeedPage() {
  const [page, setPage] = useState(0);
  const [items, setItems] = useState<SharedDiaryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    let active = true;
    setLoading(true); setError(null); setItems([]);
    fetchSharedDiaryFeed(page).then((data) => { if (active) setItems(data); })
      .catch((caught: unknown) => { if (active) setError(caught instanceof Error ? caught.message : '일기를 불러오지 못했어요.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [page, revision]);

  return (
    <PageSection title="공개·그룹 피드" description="모두에게 공개한 이야기와 나에게 공유된 그룹 일기를 읽어보세요.">
      <div className={styles.container}>
        {getAuthSession()?.appRole === 'child' ? <Link to="/sharing-groups">그룹 관리 →</Link> : null}
        {loading ? <StatusMessage kind="loading" message="이야기를 모으고 있어요." /> : null}
        {error ? <div><StatusMessage kind="error" message={error} /><Button onClick={() => setRevision((value) => value + 1)}>다시 불러오기</Button></div> : null}
        {!loading && !error && items.length === 0 ? <StatusMessage kind="empty" message="아직 볼 수 있는 일기가 없어요." /> : null}
        {items.map((item) => <article className={styles.card} key={item.diaryId}>
          <div className={styles.heading}><strong>{item.authorNickname}</strong><span>{getVisibilityScopeLabel(item.visibilityScope)}</span></div>
          <p>{item.contentPreview}</p>
          <div className={styles.heading}><small>{formatKoreanDate(item.writtenDate)}</small><Link to={`/diaries/${item.diaryId}`}>이야기 읽기 →</Link></div>
        </article>)}
        <div className={styles.actions}>
          <Button variant="secondary" disabled={loading || page === 0} onClick={() => setPage((value) => value - 1)}>이전</Button>
          <span>{page + 1} 페이지</span>
          <Button variant="secondary" disabled={loading || !!error || items.length < 30} onClick={() => setPage((value) => value + 1)}>다음</Button>
        </div>
      </div>
    </PageSection>
  );
}
