import { Link } from 'react-router-dom';
import { useMemo, useState } from 'react';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import DiaryCalendar from '../components/diary/DiaryCalendar';
import DiaryList from '../components/diary/DiaryList';
import { formatKoreanDate, getDateKey } from '../constants/date';
import { useDiaryList } from '../hooks/useDiaryList';
import styles from './DiaryListPage.module.css';

function DiaryListPage() {
  const { data, isLoading, error } = useDiaryList();
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const filteredDiaries = useMemo(() => {
    if (!data || !selectedDate) {
      return data ?? [];
    }

    return data.filter((diary) => getDateKey(diary.writtenDate ?? diary.createdAt) === selectedDate);
  }, [data, selectedDate]);

  return (
    <PageSection
      title="내 일기"
      description="날짜를 골라 그날의 이야기를 다시 읽어봐요."
    >
      <div className={styles.toolbar}><Link to="/sharing-groups">그룹 관리 <span aria-hidden="true">›</span></Link><Link to="/shared-feed">공개·그룹 피드 <span aria-hidden="true">›</span></Link></div>
      {isLoading ? <StatusMessage kind="loading" message="일기를 불러오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="일기를 불러오지 못했어요. 잠시 후 다시 시도해 주세요." /> : null}
      {data && data.length > 0 ? (
        <div className={styles.content}>
          <DiaryCalendar diaries={data} selectedDate={selectedDate} onSelectDate={setSelectedDate} />
          <div className={styles.entries}>
          {filteredDiaries.length > 0 ? <DiaryList diaries={filteredDiaries} /> : null}
          {selectedDate && filteredDiaries.length === 0 ? (
            <StatusMessage kind="empty" message={`${formatKoreanDate(selectedDate)}에는 아직 기록이 없어요.`} />
          ) : null}
          </div>
        </div>
      ) : null}
      {data && data.length === 0 ? (
        <div className={styles.empty}><StatusMessage kind="empty" message="아직 쓴 일기가 없어요. 오늘의 이야기부터 남겨볼까요?" /><Link to="/diaries/write">첫 일기 쓰기 <span aria-hidden="true">→</span></Link></div>
      ) : null}
    </PageSection>
  );
}

export default DiaryListPage;
