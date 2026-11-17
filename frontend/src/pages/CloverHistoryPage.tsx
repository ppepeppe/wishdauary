import CardCarousel from '../components/common/CardCarousel';
import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import Button from '../components/common/Button';
import { useAsyncData } from '../hooks/useAsyncData';
import { fetchCloverHistory } from '../services/api/cloverApi';
import styles from './ShopPage.module.css';

const CLOVER_REASON_LABELS: Record<string, string> = {
  FAMILY_AWARD: '가족의 칭찬',
  ENGLISH_STUDY: '한 줄 영어',
  DIARY_WRITE: '일기 쓰기',
  STREAK_3: '3일 연속 보너스',
  STREAK_5: '5일 연속 보너스',
  STREAK_7: '7일 연속 보너스',
  GIFT_REQUEST: '선물 부탁하기',
  GIFT_REQUEST_REFUND: '클로버 돌려받기',
};

function formatHistoryDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', { month: 'long', day: 'numeric' }).format(new Date(value));
}

export default function CloverHistoryPage() {
  const { data, isLoading, error, refetch } = useAsyncData(fetchCloverHistory, true);
  const history = data ?? [];
  return <PageSection title="클로버 적립 내역">
    <Link to="/me">← 마이페이지</Link>
    {isLoading ? <StatusMessage kind="loading" message="적립 내역을 불러오고 있어요." /> : null}
    {error ? <><StatusMessage kind="error" message={error} /><Button onClick={() => void refetch()}>다시 불러오기</Button></> : null}
          {history.length > 0 ? (
            <section id="clover-history" className={styles.historyPanel} aria-label="클로버 적립 내역">
              <CardCarousel title="적립 내역" itemLabel="기록">
                {history.map((entry) => (
                  <article key={entry.ledgerId} className={styles.historyItem}>
                    <span className={styles.historyReason}>
                      {CLOVER_REASON_LABELS[entry.reason] ?? entry.reason}
                    </span>
                    <span className={styles.historyDate}>{formatHistoryDate(entry.createdAt)}</span>
                    {entry.note || entry.givenByName ? <p className={styles.historyNote}>{[entry.givenByName, entry.note].filter(Boolean).join(' · ')}</p> : null}
                    <strong className={entry.amount >= 0 ? styles.historyGain : styles.historySpend}>
                      {entry.amount >= 0 ? `+${entry.amount}` : entry.amount} 클로버
                    </strong>
                  </article>
                ))}
              </CardCarousel>
              <details className={styles.rewardGuide}>
                <summary>연속 기록 보너스 기준</summary>
                <p>3일 +1 · 5일 +2 · 7일 +3 클로버</p>
                <p>같은 날짜는 한 번만 계산하며, 기록이 끊기면 1일부터 다시 시작합니다.</p>
              </details>
            </section>
          ) : null}

    {data && history.length === 0 ? <StatusMessage kind="empty" message="아직 클로버 내역이 없어요." /> : null}
  </PageSection>;
}
