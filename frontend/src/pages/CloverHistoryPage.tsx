import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import Button from '../components/common/Button';
import { useAsyncData } from '../hooks/useAsyncData';
import { fetchCloverHistory } from '../services/api/cloverApi';
import styles from './CloverHistoryPage.module.css';

import { CLOVER_REASON_LABELS } from '../constants/clover';

function formatHistoryDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' }).format(new Date(value));
}

export default function CloverHistoryPage() {
  const { data, isLoading, error, refetch } = useAsyncData(fetchCloverHistory, true);
  const history = data ?? [];
  return <PageSection title="클로버 적립 내역">
    <Link to="/me" className={styles.backLink}>← 마이페이지</Link>
    {isLoading ? <StatusMessage kind="loading" message="적립 내역을 불러오고 있어요." /> : null}
    {error ? <><StatusMessage kind="error" message={error} /><Button onClick={() => void refetch()}>다시 불러오기</Button></> : null}
    {history.length > 0 ? (
      <section id="clover-history" className={styles.panel} aria-label="클로버 적립 내역">
        <h2 className={styles.heading}>전체 {history.length}건</h2>
        <ol className={styles.list} aria-label="최신순 클로버 내역">
          {history.map(entry => (
            <li key={entry.ledgerId} className={styles.entry}>
              <div className={styles.copy}>
                <strong>{CLOVER_REASON_LABELS[entry.reason] ?? entry.reason}</strong>
                <time dateTime={entry.createdAt}>{formatHistoryDate(entry.createdAt)}</time>
                {entry.note || entry.givenByName ? <p>{[entry.givenByName, entry.note].filter(Boolean).join(' · ')}</p> : null}
              </div>
              <strong className={entry.amount >= 0 ? styles.gain : styles.spend}>
                {entry.amount >= 0 ? `+${entry.amount}` : entry.amount} 클로버
              </strong>
            </li>
          ))}
        </ol>
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
