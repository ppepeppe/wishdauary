import styles from './DiaryReactionSummary.module.css';

interface DiaryReactionSummaryProps {
  heartCount?: number;
  commentCount?: number;
}

function DiaryReactionSummary({ heartCount = 0, commentCount = 0 }: DiaryReactionSummaryProps) {
  return (
    <div className={styles.reactions}>
      <span className={styles.reaction}>❤️ {heartCount}</span>
      <span className={styles.reaction}>💬 {commentCount}</span>
    </div>
  );
}

export default DiaryReactionSummary;
