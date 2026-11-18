import { useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAsyncData } from '../../hooks/useAsyncData';
import { fetchChildProfile } from '../../services/api/childProfileApi';
import type { FamilyConnection } from '../../types/familyConnection';
import ProfileAvatar from '../common/ProfileAvatar';
import Button from '../common/Button';
import StatusMessage from '../common/StatusMessage';
import NavIcon from '../common/NavIcon';
import styles from './ChildSummaryCard.module.css';

export default function ChildSummaryCard({ child, todayDiaryCount }: { child: FamilyConnection; todayDiaryCount: number | null }) {
  const load = useCallback(() => fetchChildProfile(child.userId), [child.userId]);
  const { data, error, isLoading, refetch } = useAsyncData(load, true);
  const pendingGifts = data?.giftRequests.filter(gift => gift.status.toLowerCase() === 'pending').length;
  return <div className={styles.container}>
    <Link to={`/family/children/${child.userId}`} className={styles.card} aria-label={`${child.nickname} 아이 요약, 프로필 보기`}>
      <div className={styles.heading}>
        <ProfileAvatar preset={child.avatarPreset} character={child.avatarCharacter} imageUrl={child.avatarImage} size="md" />
        <div className={styles.identity}><h3>{child.nickname}</h3><span>아이 프로필 보기</span></div>
        <NavIcon name="arrowRight" />
      </div>
      <dl className={styles.stats} aria-busy={isLoading}>
        <div><dt>보유 클로버</dt><dd>{data ? `${data.clovers.balance}개` : '—'}</dd></div>
        <div><dt>오늘 공유한 일기</dt><dd>{todayDiaryCount === null ? '—' : `${todayDiaryCount}개`}</dd></div>
        <div><dt>승인 대기 선물</dt><dd>{pendingGifts === undefined ? '—' : `${pendingGifts}개`}</dd></div>
      </dl>
    </Link>
    {isLoading && !data ? <StatusMessage kind="loading" message="아이 요약을 불러오고 있어요." /> : null}
    {error ? <><StatusMessage kind="error" message="아이 요약을 불러오지 못했어요." /><Button variant="ghost" onClick={() => void refetch()}>요약 다시 불러오기</Button></> : null}
  </div>;
}
