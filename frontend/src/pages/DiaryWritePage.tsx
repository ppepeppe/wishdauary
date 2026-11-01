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
    <PageSection
      title="일기 작성"
      description="날짜를 고르고 그날의 마음을 편하게 남겨보세요."
    >
      <div className={styles.container}>
        <div className={styles.rewardCard}>
          <p className={styles.rewardEyebrow}>기록 보상</p>
          <p className={styles.rewardTitle}>일기를 저장하면 클로버 1개</p>
          <p className={styles.rewardDescription}>
            선택한 날짜에 기록이 저장돼요. 미래 날짜는 선택할 수 없어요.
          </p>
        </div>
        {isStatusLoading ? <StatusMessage kind="loading" message="오늘 작성 상태를 확인하고 있어요." /> : null}
        {statusError ? <StatusMessage kind="error" message={statusError} /> : null}
        {dateStatus?.exists ? (
          <StatusMessage
            kind="success"
            message={`${selectedDateLabel} 기록이 ${dateStatus.count}개 있어요. 하나 더 남겨도 좋아요.`}
          />
        ) : null}
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
