import CardCarousel from '../components/common/CardCarousel';
import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import DiaryNavigation from '../components/diary/DiaryNavigation';
import FamilyDiaryNavigation from '../components/diary/FamilyDiaryNavigation';
import { getSharedFeedGroupTabs } from '../utils/sharedFeedGroups';
import Tabs from '../components/common/Tabs';
import { useAsyncData } from '../hooks/useAsyncData';
import Button from '../components/common/Button';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { fetchSharedDiaryFeed, fetchSharedFeedGroups, type SharedDiaryItem } from '../services/api/diaryApi';
import { getAuthSession } from '../services/session/authSession';
import DiaryFeedCard from '../components/diary/DiaryFeedCard';
import styles from './SharedDiaryFeedPage.module.css';

export default function SharedDiaryFeedPage() {
  const isChild = getAuthSession()?.appRole === 'child';
  const groups = useAsyncData(fetchSharedFeedGroups, true);
  const [params, setParams] = useSearchParams();
  const groupTabs = getSharedFeedGroupTabs(groups.data ?? []);
  const requested = Number(params.get('group') ?? -1);
  const selectedGroup = groupTabs.find(group => group.groupIds.includes(requested));
  const groupId = requested === -1 || requested === 0 ? requested : selectedGroup?.id ?? -1;
  const groupIdsKey = selectedGroup?.groupIds.join(',') ?? '';
  const [page, setPage] = useState(0);
  const [items, setItems] = useState<SharedDiaryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    let active = true;
    setLoading(true); setError(null); setItems([]);
    const selectedIds = groupIdsKey ? groupIdsKey.split(',').map(Number) : [];
    fetchSharedDiaryFeed(page, groupId, selectedIds.length > 1 ? selectedIds : undefined).then((data) => { if (active) setItems(data); })
      .catch((caught: unknown) => { if (active) setError(caught instanceof Error ? caught.message : '일기를 불러오지 못했어요.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [page, revision, groupId, groupIdsKey]);

  return (
    <PageSection title={isChild ? '일기' : '공개·그룹 일기'}>
      {isChild ? <DiaryNavigation /> : <FamilyDiaryNavigation />}
      <div className={styles.container}>
        {groups.error ? <><StatusMessage kind="error" message={groups.error} /><Button onClick={() => void groups.refetch()}>그룹 다시 불러오기</Button></> : null}
        <Tabs label="그룹 선택" value={String(groupId)} onChange={value => { const next = new URLSearchParams(params); next.set('group', value); setParams(next, { replace: true }); setPage(0); }} options={[
          { value: '-1', label: '전체 그룹' },
          ...groupTabs.map(group => ({ value: String(group.id), label: group.name })),
          { value: '0', label: '전체 공개' },
        ]}>
        {loading ? <StatusMessage kind="loading" message="이야기를 모으고 있어요." /> : null}
        {error ? <div><StatusMessage kind="error" message={error} /><Button onClick={() => setRevision((value) => value + 1)}>다시 불러오기</Button></div> : null}
        {!loading && !error && items.length === 0 ? <StatusMessage kind="empty" message="아직 볼 수 있는 일기가 없어요." /> : null}
        <CardCarousel key={`${groupId}:${groupIdsKey}`} title="공유된 이야기" itemLabel="일기">{items.map(item => <DiaryFeedCard
          key={item.diaryId} diaryId={item.diaryId} author={item.authorNickname} writtenDate={item.writtenDate}
          visibilityScope={item.visibilityScope} emotion={item.emotion} contentPreview={item.contentPreview} />)}</CardCarousel>
        <div className={styles.actions}>
          <Button variant="secondary" disabled={loading || page === 0} onClick={() => setPage((value) => value - 1)}>이전</Button>
          <span>{page + 1} 페이지</span>
          <Button variant="secondary" disabled={loading || !!error || items.length < 30} onClick={() => setPage((value) => value + 1)}>다음</Button>
        </div>
        </Tabs>
      </div>
    </PageSection>
  );
}
