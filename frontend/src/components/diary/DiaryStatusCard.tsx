import { getEmotionMeta } from '../../constants/emotion';
import type { DiaryEmotion } from '../../types/diary';
import { Link } from 'react-router-dom';
import DiaryReactionSummary from './DiaryReactionSummary';
import EmotionIcon from './EmotionIcon';
import styles from './DiaryStatusCard.module.css';

interface DiaryStatusCardProps {
  hasTodayDiary: boolean;
  todayDiaryCount?: number;
  emotion?: DiaryEmotion | null;
  heartCount?: number;
  commentCount?: number;
  isLoading?: boolean;
}

function DiaryStatusCard({
  hasTodayDiary,
  todayDiaryCount = 0,
  emotion = null,
  heartCount = 0,
  commentCount = 0,
  isLoading = false,
}: DiaryStatusCardProps) {
  const emotionMeta = getEmotionMeta(emotion);

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <p className={styles.title}>오늘의 기록 상태</p>
        <span className={`${styles.badge} ${hasTodayDiary ? styles.doneBadge : styles.pendingBadge}`}>
          {hasTodayDiary ? '오늘 기록 있음' : '아직 안 썼어요'}
        </span>
      </div>
      {isLoading ? (
        <div className={styles.loadingBlock}>
          <p className={styles.description}>오늘 기록 상태를 천천히 확인하고 있어요.</p>
        </div>
      ) : hasTodayDiary ? (
        <div className={styles.completeBlock}>
          <p className={styles.complete}>오늘 이야기를 이미 남겨두었어요.</p>
          {emotionMeta ? (
            <p className={styles.emotion}>
              <span aria-hidden="true">
                <EmotionIcon emotion={emotionMeta.key} />
              </span>
              오늘 기분은 {emotionMeta.label}
            </p>
          ) : null}
          {todayDiaryCount > 1 ? (
            <p className={styles.countHint}>오늘은 이미 {todayDiaryCount}개의 기록을 남겼어요.</p>
          ) : null}
          <DiaryReactionSummary heartCount={heartCount} commentCount={commentCount} />
          <p className={styles.description}>지금은 반응을 살펴보거나, 남긴 기록을 다시 편하게 돌아봐도 괜찮아요.</p>
          <div className={styles.actionRow}>
            <Link to="/feed" className={styles.primaryLink}>
              가족 반응 확인하기
            </Link>
            <Link to="/diaries" className={styles.secondaryLink}>
              내 기록 보러 가기
            </Link>
          </div>
        </div>
      ) : (
        <div className={styles.ctaBlock}>
          <div className={styles.pendingBlock}>
            <p className={styles.pendingTitle}>아직 오늘의 기록이 없어요.</p>
            <p className={styles.description}>짧게 한 줄만 적어도 괜찮아요. 지금 떠오르는 마음부터 남겨보세요.</p>
          </div>
          <div className={styles.actionRow}>
            <Link to="/diaries/write" className={styles.primaryLink}>
              오늘의 마음 남기기
            </Link>
            <Link to="/diaries" className={styles.secondaryLink}>
              내 기록 보러 가기
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}

export default DiaryStatusCard;
