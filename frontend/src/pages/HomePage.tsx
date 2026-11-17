import FamilyConnectionNotice from '../components/family/FamilyConnectionNotice';
import TodayEnglishCard from '../components/rewards/TodayEnglishCard';
import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import EmotionIcon from '../components/diary/EmotionIcon';
import ProfileAvatar from '../components/common/ProfileAvatar';
import Button from '../components/common/Button';
import StatusMessage from '../components/common/StatusMessage';
import CloverIcon from '../components/common/CloverIcon';
import NavIcon from '../components/common/NavIcon';
import ProductArtwork from '../components/shop/ProductArtwork';
import { formatKoreanDate, getTodayDateInKorea } from '../constants/date';
import EmotionSelector from '../components/diary/EmotionSelector';
import { useHomeData } from '../hooks/useHomeData';
import { useWishGoal } from '../hooks/useWishGoal';
import { useAsyncData } from '../hooks/useAsyncData';
import { fetchSentGiftRequests } from '../services/api/shopApi';
import { getPendingGoalRequest } from '../utils/giftRequest';
import { fetchMyProfile } from '../services/api/userApi';
import { getAuthSession } from '../services/session/authSession';
import type { DiaryEmotion } from '../types/diary';
import type { MyProfile } from '../types/user';
import styles from './HomePage.module.css';

interface HomePageLocationState {
  showDiarySavedMessage?: boolean;
  refetchHomeData?: boolean;
}

function HomePage() {
  const location = useLocation();
  const navigate = useNavigate();
  const authSession = getAuthSession();
  const locationState = (location.state ?? null) as HomePageLocationState | null;
  const { data, isLoading, error, refetch } = useHomeData();
  const { goal, refetch: refreshGoal } = useWishGoal();
  const { data: giftRequests } = useAsyncData(fetchSentGiftRequests, true);
  const [showSavedMessage, setShowSavedMessage] = useState(Boolean(locationState?.showDiarySavedMessage));
  const [selectedEmotion, setSelectedEmotion] = useState<DiaryEmotion>('happy');
  const refreshRewards = useCallback(async () => { await Promise.all([refetch(), refreshGoal()]); }, [refetch, refreshGoal]);
  const [profile, setProfile] = useState<MyProfile | null>(null);

  const loadProfile = useCallback(async () => {
    try {
      setProfile(await fetchMyProfile());
    } catch {
      setProfile(null);
    }
  }, []);

  useEffect(() => {
    void loadProfile();
  }, [loadProfile, location.pathname]);

  useEffect(() => {
    if (locationState?.showDiarySavedMessage) {
      setShowSavedMessage(true);
    }
  }, [locationState?.showDiarySavedMessage]);

  useEffect(() => {
    if (!locationState?.refetchHomeData) {
      return;
    }

    void refetch();
    navigate(location.pathname, { replace: true, state: null });
  }, [location.pathname, locationState?.refetchHomeData, navigate, refetch]);

  useEffect(() => {
    if (!showSavedMessage) {
      return;
    }

    const timer = window.setTimeout(() => {
      setShowSavedMessage(false);
    }, 2800);

    return () => window.clearTimeout(timer);
  }, [showSavedMessage]);

  const nickname = profile?.nickname?.trim() || authSession?.nickname?.trim() || '친구';
  const pendingGoalRequest = getPendingGoalRequest(giftRequests ?? [], goal?.productId, authSession?.userId);
  const goalTarget = pendingGoalRequest?.cloverPrice ?? goal?.targetCloverPrice ?? 0;
  const currentGoal = pendingGoalRequest ? goalTarget : goal ? Math.min(goalTarget, goal.currentCloverBalance) : 0;
  const progressRatio = goalTarget > 0 ? Math.min(100, (currentGoal / goalTarget) * 100) : 0;
  const goalRemaining = goal?.remainingClovers ?? 0;

  function handleWriteNow() {
    navigate('/diaries/write', {
      state: {
        preselectedEmotion: selectedEmotion,
      },
    });
  }

  if (error && !data) {
    return (
      <div className={styles.container}>
        <FamilyConnectionNotice />
        <StatusMessage kind="error" message="홈을 불러오지 못했어요. 연결을 확인한 뒤 다시 시도해 주세요." />
        <button type="button" className={styles.writeButton} onClick={() => void refetch()}>
          다시 불러오기
        </button>
        <Link to="/me" className={styles.recoveryLink}>계정 확인·로그아웃</Link>
      </div>
    );
  }

  if (isLoading && !data) {
    return <div className={styles.container}><StatusMessage kind="loading" message="오늘의 기록을 불러오는 중이에요." /></div>;
  }

  return (
    <div className={styles.container}>
      <FamilyConnectionNotice />
      {showSavedMessage ? <StatusMessage kind="success" message="일기를 저장했어요. 클로버도 함께 모았어요." /> : null}
      {error ? <StatusMessage kind="error" message="홈을 새로 불러오지 못했어요. 잠시 후 다시 시도해 주세요." /> : null}

      <section className={styles.headerCard}>
        <Link to="/me" className={styles.profileBadge}>
          <ProfileAvatar
            preset={profile?.avatarPreset}
            character={profile?.avatarCharacter}
            imageUrl={profile?.avatarImage}
            size="sm"
            alt=""
          />
          <div className={styles.profileCopy}>
            <p className={styles.name}>{nickname}</p>
            <span className={styles.profileHint}>마이페이지 <span aria-hidden="true">›</span></span>
          </div>
        </Link>
        <div className={styles.balanceChip} role="group" aria-label={`모은 클로버 ${(data?.cloverBalance ?? 0).toLocaleString('ko-KR')}개`}>
          <span className={styles.balanceIcon} aria-hidden="true">
            <CloverIcon />
          </span>
          <span className={styles.balanceCopy}><small>모은 클로버</small><strong className={styles.balanceValue}>{(data?.cloverBalance ?? 0).toLocaleString('ko-KR')}</strong></span>
        </div>
      </section>

      <section className={styles.todayCard} aria-label="오늘의 기록">
        <div className={styles.dateBar}>
          <div>
            <span className={styles.dateCaption}>오늘</span>
            <strong>{formatKoreanDate(getTodayDateInKorea())}</strong>
          </div>
          <Link to="/diaries" className={styles.calendarLink}>
            기록 달력
          </Link>
        </div>

        <div className={styles.streakBanner}>
          <div className={styles.streakCopy}>
            <p className={styles.streakTitle}>{data?.currentStreak ? `${data.currentStreak}일 연속 기록` : '오늘의 첫 기록을 남겨보세요'}</p>
          </div>
        </div>
      </section>

      <section className={styles.diaryCard}>
        <h1 className={styles.diaryTitle}>오늘의 기분</h1>

        <div className={styles.emotionRow}>
          <EmotionSelector value={selectedEmotion} onChange={setSelectedEmotion} />
        </div>

        <Button type="button" className={styles.writeButton} fullWidth size="lg" onClick={handleWriteNow}>
          일기 쓰기
          <NavIcon name="arrowRight" />
        </Button>

        <div className={styles.diaryMetaRow}>
          <p className={styles.diaryMetaText}>
            {data?.hasTodayDiary
              ? `오늘 일기 ${data.todayDiaryCount}개`
              : '오늘 작성한 일기 없음'}
          </p>
          <Link to={data?.hasTodayDiary ? '/diaries' : '/feed'} className={styles.metaLink}>
            {data?.hasTodayDiary ? '내 일기 보기' : '받은 소식'}
          </Link>
        </div>
        {data?.hasTodayDiary ? (
          <p className={styles.reactionHint}>
            {data.todayDiaryEmotion ? (
              <span className={styles.reactionEmotion}>
                <EmotionIcon emotion={data.todayDiaryEmotion} />
              </span>
            ) : null}
            하트 {data.todayDiaryHeartCount} · 댓글 {data.todayDiaryCommentCount}
          </p>
        ) : null}
      </section>

      <TodayEnglishCard onRewardChanged={refreshRewards} />

      <section className={styles.goalCard}>
        {goal ? (
          <>
            <div className={styles.goalTopRow}>
              <div>
                <p className={styles.goalHeading}>목표 선물</p>
                <p className={styles.goalName}>{goal.productTitle}</p>
              </div>
              <span className={styles.goalBadge}>
                {pendingGoalRequest ? '선물 요청 중' : goalRemaining === 0 ? '달성!' : `${goalRemaining} 클로버 남음`}
              </span>
            </div>

            <div className={styles.goalContent}>
              <ProductArtwork
                title={goal.productTitle}
                imageUrl={goal.productImageUrl}
                className={styles.goalImage}
              />
              <div className={styles.goalProgressBlock}>
                <div className={styles.goalNumbers}>
                  <span>{currentGoal} 클로버</span>
                  <span>{goalTarget} 클로버</span>
                </div>
                <div className={styles.progressTrack}>
                  <div className={styles.progressFill} style={{ width: `${progressRatio}%` }} />
                </div>
                <p className={styles.goalHelper}>
                  {pendingGoalRequest ? '가족 확인 중' : goalRemaining === 0
                    ? '선물을 요청할 수 있어요'
                    : `${goalRemaining} 클로버 남음`}
                </p>
              </div>
            </div>
            <Link to={pendingGoalRequest ? '/shop/requests' : `/shop/products/${goal.productId}`} className={styles.goalDetailLink}>
              {pendingGoalRequest ? '선물 요청 내역 보기' : '선물 보기'}
            </Link>
          </>
        ) : (
          <div className={styles.goalEmpty}>
            <span className={styles.goalEmptyIcon} aria-hidden="true"><NavIcon name="gift" /></span>
            <div className={styles.goalEmptyCopy}>
              <p className={styles.goalHeading}>목표 선물</p>
              <p className={styles.goalName}>목표 선물이 없어요</p>
              <Link to="/shop" className={styles.goalDetailLink}>선물 고르기 <span aria-hidden="true">→</span></Link>
            </div>
          </div>
        )}
      </section>
    </div>
  );
}

export default HomePage;
