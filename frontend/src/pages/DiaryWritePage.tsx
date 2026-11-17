import { useCallback, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import DiaryForm from '../components/diary/DiaryForm';
import { useAsyncData } from '../hooks/useAsyncData';
import { formatKoreanDate, getTodayDateInKorea } from '../constants/date';
import { createDiary, fetchDiaryStatus } from '../services/api/diaryApi';
import type { DiaryEmotion } from '../types/diary';
import styles from './DiaryWritePage.module.css';

const DEFAULT_SAVE_ERROR_MESSAGE = '선택한 날짜의 기록을 저장하지 못했어요. 잠시 후 다시 시도해 주세요.';

interface DiaryWriteLocationState {
  preselectedEmotion?: DiaryEmotion;
}

function DiaryWritePage() {
  const location = useLocation();
  const navigate = useNavigate();
  const locationState = (location.state ?? null) as DiaryWriteLocationState | null;
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [writtenDate, setWrittenDate] = useState(getTodayDateInKorea);
  const loadDiaryStatus = useCallback(() => fetchDiaryStatus(writtenDate), [writtenDate]);
  const { data: dateStatus, isLoading: isStatusLoading, error: statusError } = useAsyncData(loadDiaryStatus);
  const selectedDateLabel = writtenDate === getTodayDateInKorea() ? '오늘' : formatKoreanDate(writtenDate, false);

  async function handleSubmit(payload: Parameters<typeof createDiary>[0]) {
    try {
      setIsSubmitting(true);
      setError(null);
      await createDiary(payload);
      navigate('/home', {
        replace: true,
        state: {
          showDiarySavedMessage: true,
          refetchHomeData: true,
        },
      });
    } catch (caughtError) {
      console.error(caughtError);
      setError(caughtError instanceof Error ? caughtError.message : DEFAULT_SAVE_ERROR_MESSAGE);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <PageSection title="일기 작성">
      <div className={styles.container}>
        <p className={styles.rewardHint}>일기 저장 시 클로버 1개</p>
        {isStatusLoading ? <StatusMessage kind="loading" message="선택한 날짜의 작성 상태를 확인하고 있어요." /> : null}
        {statusError ? <StatusMessage kind="error" message={statusError} /> : null}
        {dateStatus?.exists ? <p className={styles.existingCount}>{selectedDateLabel} 일기 {dateStatus.count}개 작성</p> : null}
      </div>
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <DiaryForm
        onSubmit={handleSubmit}
        isSubmitting={isSubmitting}
        initialEmotion={locationState?.preselectedEmotion ?? null}
        writtenDate={writtenDate}
        onWrittenDateChange={setWrittenDate}
      />
    </PageSection>
  );
}

export default DiaryWritePage;
