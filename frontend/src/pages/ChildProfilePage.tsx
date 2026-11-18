import { useCallback } from 'react';
import { Link, useParams } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import Button from '../components/common/Button';
import ProfileAvatar from '../components/common/ProfileAvatar';
import FamilyCloverAward from '../components/rewards/FamilyCloverAward';
import { useAsyncData } from '../hooks/useAsyncData';
import { fetchChildProfile } from '../services/api/childProfileApi';
import { fetchFamilyConnections } from '../services/api/familyConnectionApi';
import { formatKoreanDate } from '../constants/date';
import styles from './ChildProfilePage.module.css';
import historyStyles from './CloverHistoryPage.module.css';
import { CLOVER_REASON_LABELS } from '../constants/clover';

const GIFT_STATUS: Record<string, string> = { PENDING: '승인 대기', APPROVED: '승인됨', REJECTED: '거절됨' };

export default function ChildProfilePage() {
  const childId = Number(useParams().childId);
  const load = useCallback(() => fetchChildProfile(childId), [childId]);
  const profile = useAsyncData(load, true);
  const connections = useAsyncData(fetchFamilyConnections, true);
  const child = connections.data?.find(item => item.userId === childId);
  const data = profile.data;
  return <PageSection title="아이 프로필">
    <Link to="/family-connections" className={historyStyles.backLink}>← 연결된 아이</Link>
    {profile.isLoading && !data ? <StatusMessage kind="loading" message="아이 프로필을 불러오고 있어요." /> : null}
    {profile.error ? <><StatusMessage kind="error" message={profile.error} /><Button onClick={() => void profile.refetch()}>다시 불러오기</Button></> : null}
    {data ? <div className={styles.content}>
      <section className={styles.profile} aria-label="아이 정보">
        <div className={styles.identity}>
          <ProfileAvatar preset={child?.avatarPreset} character={child?.avatarCharacter} imageUrl={child?.avatarImage} size="lg" alt={`${data.nickname} 프로필`} />
          <div><h2>{data.nickname}</h2><p>보유 클로버 <strong>{data.clovers.balance}개</strong></p></div>
        </div>
        <Link to={`/family-feed?child=${childId}`}>아이 일기 보기 →</Link>
        <FamilyCloverAward key={childId} childId={childId} childName={data.nickname} onAwarded={profile.refetch} />
      </section>
      <section className={styles.panel} aria-label="선물 요청 내역">
        <h2>선물 요청 내역 <small>{data.giftRequests.length}건</small></h2>
        {data.giftRequests.length === 0 ? <StatusMessage kind="empty" message="아직 요청한 선물이 없어요." /> : null}
        <ul className={historyStyles.list}>{data.giftRequests.map(gift => <li key={gift.notificationId} className={historyStyles.entry}>
          <div className={historyStyles.copy}><strong>{gift.productTitle}</strong><time>{formatKoreanDate(gift.createdAt)}</time><span>{gift.cloverPrice} 클로버</span></div>
          <span>{gift.receivedAt ? '수령 완료' : GIFT_STATUS[gift.status.toUpperCase()] ?? gift.status}</span>
        </li>)}</ul>
      </section>
      <section className={styles.panel} aria-label="클로버 적립·사용 내역">
        <h2>클로버 적립·사용 내역 <small>{data.history.entries.length}건</small></h2>
        {data.history.entries.length === 0 ? <StatusMessage kind="empty" message="아직 클로버 내역이 없어요." /> : null}
        <ol className={historyStyles.list}>{data.history.entries.map(entry => <li key={entry.ledgerId} className={historyStyles.entry}>
          <div className={historyStyles.copy}>
            <strong>{CLOVER_REASON_LABELS[entry.reason] ?? entry.reason}</strong><time dateTime={entry.createdAt}>{formatKoreanDate(entry.createdAt)}</time>
            {entry.note || entry.givenByName ? <p>{[entry.givenByName, entry.note].filter(Boolean).join(' · ')}</p> : null}
          </div>
          <strong className={entry.amount >= 0 ? historyStyles.gain : historyStyles.spend}>{entry.amount > 0 ? '+' : ''}{entry.amount} 클로버</strong>
        </li>)}</ol>
      </section>
    </div> : null}
  </PageSection>;
}
