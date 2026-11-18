import { useState } from 'react';
import Tabs from '../components/common/Tabs';
import Button from '../components/common/Button';
import { useAsyncData } from '../hooks/useAsyncData';
import { useSelectedChild } from '../hooks/useSelectedChild';
import { fetchFamilyConnections } from '../services/api/familyConnectionApi';
import { getChildDiaries } from '../utils/childDiaries';
import PageSection from '../components/common/PageSection';
import FamilyDiaryNavigation from '../components/diary/FamilyDiaryNavigation';
import { Link } from 'react-router-dom';
import StatusMessage from '../components/common/StatusMessage';
import FamilyFeedList from '../components/diary/FamilyFeedList';
import { useFamilyFeed } from '../hooks/useFamilyFeed';
import DiaryCalendar from '../components/diary/DiaryCalendar';
import { formatKoreanDate, getTodayDateInKorea, shiftDiaryDate } from '../constants/date';
import styles from './DiaryListPage.module.css';

function FamilyFeedPage() {
  const {
    data,
    isLoading,
    error,
    actionError,
    toggleHeart,
    isHeartPending,
    loadComments,
    submitComment,
    getComments,
    getCommentError,
    isCommentLoading,
    isCommentSubmitting,
  } = useFamilyFeed();

  const connections = useAsyncData(fetchFamilyConnections, true);
  const children = connections.data ?? [];
  const { childId, selectChild } = useSelectedChild(children);
  const childDiaries = getChildDiaries(data ?? [], childId);
  const [selectedDate, setSelectedDate] = useState(getTodayDateInKorea);
  const datedDiaries = getChildDiaries(childDiaries, childId, selectedDate);

  return (
    <PageSection title="아이 일기">
      <FamilyDiaryNavigation />
      {isLoading ? <StatusMessage kind="loading" message="아이의 일기를 불러오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="일기를 불러오지 못했어요. 잠시 후 다시 시도해 주세요." /> : null}
      {actionError ? <StatusMessage kind="error" message="반응을 남기지 못했어요. 다시 시도해 주세요." /> : null}
      {connections.error ? <><StatusMessage kind="error" message={connections.error} /><Button variant="ghost" onClick={() => void connections.refetch()}>아이 목록 다시 불러오기</Button></> : null}
      {children.length > 0 ? <Tabs label="아이 선택" value={String(childId)} onChange={selectChild}
        options={children.map(child => ({ value: String(child.userId), label: child.nickname }))}>
      {data ? <div className={styles.content}>
        <details className={styles.calendarPicker} open>
          <summary>달력에서 날짜 선택</summary>
          <DiaryCalendar diaries={childDiaries} selectedDate={selectedDate} onSelectDate={date => { if (date) setSelectedDate(date); }} />
        </details>
        <div className={styles.entries}>
          <nav className={styles.dateNavigation} aria-label="일기 날짜 이동">
            <button type="button" aria-label="이전 날짜" onClick={() => setSelectedDate(shiftDiaryDate(selectedDate, -1))}>←</button>
            <label><span className={styles.dateLabel}>일기 날짜</span><input aria-label="일기 날짜" type="date" value={selectedDate} max={getTodayDateInKorea()} onChange={event => { if (event.target.value && event.target.value <= getTodayDateInKorea()) setSelectedDate(event.target.value); }} /></label>
            <button type="button" aria-label="다음 날짜" disabled={selectedDate >= getTodayDateInKorea()} onClick={() => setSelectedDate(shiftDiaryDate(selectedDate, 1))}>→</button>
          </nav>
      {datedDiaries.length > 0 ? (
        <FamilyFeedList
          key={`${childId}-${selectedDate}`}
          diaries={datedDiaries}
          onToggleHeart={toggleHeart}
          isHeartPending={isHeartPending}
          onLoadComments={loadComments}
          onSubmitComment={submitComment}
          getComments={getComments}
          getCommentError={getCommentError}
          isCommentLoading={isCommentLoading}
          isCommentSubmitting={isCommentSubmitting}
        />
      ) : null}
      {datedDiaries.length === 0 ? (
        <StatusMessage kind="empty" message={`${formatKoreanDate(selectedDate)}에는 공유받은 일기가 없어요.`} />
      ) : null}
        </div>
      </div> : null}
      </Tabs> : null}
      {connections.data?.length === 0 ? <><StatusMessage kind="empty" message="연결된 아이가 없어요." /><Link to="/family-connections">마이페이지에서 아이 연결하기 →</Link></> : null}
    </PageSection>
  );
}

export default FamilyFeedPage;
