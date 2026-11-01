import styles from './StreakCard.module.css';

interface StreakCardProps {
  currentStreak?: number;
  maxStreak?: number;
  isLoading?: boolean;
  error?: string | null;
}

function getStreakTitle(currentStreak: number) {
  if (currentStreak <= 0) {
    return '오늘부터 천천히 이어가도 괜찮아요';
  }

  if (currentStreak >= 7) {
    return `🔥 ${currentStreak}일째 다정하게 이어가고 있어요!`;
  }

  if (currentStreak >= 3) {
    return `🔥 ${currentStreak}일 연속으로 기록하고 있어요!`;
  }

  return `🔥 ${currentStreak}일 연속 기록 중!`;
}

function getStreakDescription(currentStreak: number, maxStreak: number, isLoading: boolean, hasError: boolean) {
  if (isLoading) {
    return '기록 흐름을 살펴보고 있어요.';
  }

  if (hasError) {
    return '스트릭을 잠시 불러오지 못했어요. 오늘 기록은 그대로 이어갈 수 있어요.';
  }

  if (currentStreak <= 0) {
    return '오늘도 이어가볼까? 첫 기록부터 가볍게 시작해 보세요.';
  }

  if (currentStreak >= 7) {
    return `연속 기록이 차분히 쌓이고 있어요. 이렇게 이어가면 클로버도 더 반갑게 모일 수 있어요.${maxStreak > currentStreak ? ` 가장 길게 이어간 ${maxStreak}일 기록도 함께 남아 있어요.` : ''}`;
  }

  if (currentStreak >= 3) {
    return '연속 기록이 쌓이면 더 많은 클로버를 받을 수 있어요. 오늘도 부담 없이 이어가면 충분해요.';
  }

  return '오늘도 이어가볼까? 짧게 남겨도 충분하고, 이어질수록 클로버도 함께 모여요.';
}

function getStreakBadge(currentStreak: number, isLoading: boolean, hasError: boolean) {
  if (isLoading) {
    return '확인 중';
  }

  if (hasError) {
    return '잠시 멈춤';
  }

  if (currentStreak >= 7) {
    return '꾸준히 이어지는 중';
  }

  if (currentStreak >= 3) {
    return '조금씩 쌓이는 중';
  }

  return '가볍게 이어가기';
}

function StreakCard({ currentStreak = 0, maxStreak = 0, isLoading = false, error = null }: StreakCardProps) {
  const hasError = Boolean(error);

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <p className={styles.eyebrow}>기록 흐름</p>
        <span className={styles.badge}>{getStreakBadge(currentStreak, isLoading, hasError)}</span>
      </div>
      <p className={styles.title}>{getStreakTitle(currentStreak)}</p>
      <p className={styles.description}>{getStreakDescription(currentStreak, maxStreak, isLoading, hasError)}</p>
      {!isLoading && !hasError && maxStreak > 0 ? (
        <p className={styles.helper}>가장 길게 이어간 기록은 {maxStreak}일이에요.</p>
      ) : null}
    </div>
  );
}

export default StreakCard;
