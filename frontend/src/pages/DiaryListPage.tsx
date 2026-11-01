import { Link } from 'react-router-dom';
import { useMemo, useState } from 'react';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import DiaryCalendar from '../components/diary/DiaryCalendar';
import DiaryList from '../components/diary/DiaryList';
import { formatKoreanDate, getDateKey } from '../constants/date';
import { useDiaryList } from '../hooks/useDiaryList';

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
      title="내 일기 목록"
      description="작성했던 기록을 날짜와 공개 범위 중심으로 빠르게 확인할 수 있어요."
    >
      <p><Link to="/sharing-groups">그룹 관리</Link> · <Link to="/shared-feed">공개·그룹 피드</Link></p>
      {isLoading ? <StatusMessage kind="loading" message="내 기록을 가지런히 모아오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="기록을 불러오다 잠시 멈췄어요. 잠깐 뒤에 다시 살펴보면 괜찮아요." /> : null}
      {data && data.length > 0 ? (
        <>
          <DiaryCalendar diaries={data} selectedDate={selectedDate} onSelectDate={setSelectedDate} />
          {filteredDiaries.length > 0 ? <DiaryList diaries={filteredDiaries} /> : null}
          {selectedDate && filteredDiaries.length === 0 ? (
            <StatusMessage kind="empty" message={`${formatKoreanDate(selectedDate)}에는 아직 기록이 없어요.`} />
          ) : null}
        </>
      ) : null}
      {data && data.length === 0 ? (
        <StatusMessage kind="empty" message="아직 남겨둔 기록이 없어요. 첫 마음 한 줄부터 가볍게 시작해 보세요." />
      ) : null}
    </PageSection>
  );
}

export default DiaryListPage;
