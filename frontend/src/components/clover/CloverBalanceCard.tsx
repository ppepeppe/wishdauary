import styles from './CloverBalanceCard.module.css';
import CloverIcon from '../common/CloverIcon';

interface CloverBalanceCardProps {
  balance?: number;
  currentStreak?: number;
  recentGain?: number;
  isLoading?: boolean;
}

function getCloverDescription(currentStreak: number, isLoading: boolean) {
  if (isLoading) {
    return '지금 잔액을 확인하고 있어요.';
  }

  if (currentStreak >= 3) {
    return '연속 기록이 쌓이면 더 많은 클로버를 받을 수 있어요.';
  }

  return '천천히 쌓인 기록만큼 클로버도 함께 모여요.';
}

function CloverBalanceCard({
  balance,
  currentStreak = 0,
  recentGain = 0,
  isLoading = false,
}: CloverBalanceCardProps) {
  const safeBalance = balance ?? 0;

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <span className={styles.icon}><CloverIcon /></span>
        <div className={styles.headerMeta}>
          <p className={styles.badge}>오늘의 여유</p>
          {recentGain > 0 ? <span className={styles.gainBadge}>{`+${recentGain} 적립`}</span> : null}
        </div>
      </div>
      <div className={styles.body}>
        <p className={styles.label}>현재 클로버 잔액</p>
        <strong className={styles.value}>
          {isLoading ? '불러오는 중...' : `${safeBalance.toLocaleString()} clover`}
        </strong>
        {recentGain > 0 ? (
          <p className={styles.gainMessage}>오늘 기록이 반영되면서 클로버가 바로 쌓였어요.</p>
        ) : null}
        <p className={styles.description}>{getCloverDescription(currentStreak, isLoading)}</p>
      </div>
    </div>
  );
}

export default CloverBalanceCard;
