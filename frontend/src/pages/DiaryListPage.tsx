import { Link } from 'react-router-dom';
import { useMemo, useState } from 'react';
import PageSection from '../components/common/PageSection';
import PageNavigation from '../components/common/PageNavigation';
import EmptyState from '../components/common/EmptyState';
import LinkButton from '../components/common/LinkButton';
import StatusMessage from '../components/common/StatusMessage';
import DiaryCalendar from '../components/diary/DiaryCalendar';
import DiaryList from '../components/diary/DiaryList';
import { formatKoreanDate, getDateKey, getTodayDateInKorea, shiftDiaryDate } from '../constants/date';
import { useDiaryList } from '../hooks/useDiaryList';
import styles from './DiaryListPage.module.css';

function DiaryListPage() {
  const { data, isLoading, error } = useDiaryList();
  const [selectedDate, setSelectedDate] = useState<string>(getTodayDateInKorea);
  const filteredDiaries = useMemo(() => {
    if (!data || !selectedDate) {
      return data ?? [];
    }

    return data.filter((diary) => getDateKey(diary.writtenDate ?? diary.createdAt) === selectedDate);
  }, [data, selectedDate]);

  return (
    <PageSection title="내 일기">
      <PageNavigation><Link to="/sharing-groups">그룹 관리 →</Link><Link to="/shared-feed">공개·그룹 피드 →</Link></PageNavigation>
      {isLoading ? <StatusMessage kind="loading" message="일기를 불러오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="일기를 불러오지 못했어요. 잠시 후 다시 시도해 주세요." /> : null}
      {data && data.length > 0 ? (
        <div className={styles.content}>
          <details className={styles.calendarPicker}><summary>달력에서 날짜 선택</summary><DiaryCalendar diaries={data} selectedDate={selectedDate} onSelectDate={date => { if (date) setSelectedDate(date); }} /></details>
          <div className={styles.entries}>
          <nav className={styles.dateNavigation} aria-label="일기 날짜 이동">
            <button type="button" aria-label="이전 날짜" onClick={() => setSelectedDate(shiftDiaryDate(selectedDate, -1))}>←</button>
            <label><span className={styles.dateLabel}>일기 날짜</span><input aria-label="일기 날짜" type="date" value={selectedDate} max={getTodayDateInKorea()} onChange={event => { if (event.target.value && event.target.value <= getTodayDateInKorea()) setSelectedDate(event.target.value); }} /></label>
            <button type="button" aria-label="다음 날짜" disabled={selectedDate >= getTodayDateInKorea()} onClick={() => setSelectedDate(shiftDiaryDate(selectedDate, 1))}>→</button>
          </nav>
          {filteredDiaries.length > 0 ? <DiaryList key={selectedDate} diaries={filteredDiaries} layout="list" /> : null}
          {selectedDate && filteredDiaries.length === 0 ? (
            <StatusMessage kind="empty" message={`${formatKoreanDate(selectedDate)}에는 아직 기록이 없어요.`} />
          ) : null}
          </div>
        </div>
      ) : null}
      {data && data.length === 0 ? (
        <EmptyState title="아직 쓴 일기가 없어요">
          <LinkButton to="/diaries/write">첫 일기 쓰기</LinkButton>
        </EmptyState>
      ) : null}
    </PageSection>
  );
}

export default DiaryListPage;
