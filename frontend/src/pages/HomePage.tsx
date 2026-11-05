import FamilyConnectionNotice from '../components/family/FamilyConnectionNotice';
import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import EmotionIcon from '../components/diary/EmotionIcon';
import ProfileAvatar from '../components/common/ProfileAvatar';
import StatusMessage from '../components/common/StatusMessage';
import CloverIcon from '../components/common/CloverIcon';
import ProductArtwork from '../components/shop/ProductArtwork';
import { formatKoreanDate, getTodayDateInKorea } from '../constants/date';
import { EMOTIONS } from '../constants/emotion';
import { useHomeData } from '../hooks/useHomeData';
import { useWishGoal } from '../hooks/useWishGoal';
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
  const { goal } = useWishGoal();
  const [showSavedMessage, setShowSavedMessage] = useState(Boolean(locationState?.showDiarySavedMessage));
  const [selectedEmotion, setSelectedEmotion] = useState<DiaryEmotion>('happy');
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
  const goalTarget = goal?.targetCloverPrice ?? 0;
  const currentGoal = goal ? Math.min(goalTarget, goal.currentCloverBalance) : 0;
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
      {showSavedMessage ? <StatusMessage kind="success" message="오늘 일기를 저장했어요. 클로버도 함께 모았어요." /> : null}
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
            <p className={styles.greeting}>안녕!</p>
            <p className={styles.name}>{nickname}</p>
            <span className={styles.profileHint}>마이페이지</span>
          </div>
        </Link>
        <div className={styles.balanceChip} role="group" aria-label={`모은 클로버 ${(data?.cloverBalance ?? 0).toFixed(1)}개`}>
          <span className={styles.balanceIcon} aria-hidden="true">
            <CloverIcon />
          </span>
          <strong className={styles.balanceValue}>{(data?.cloverBalance ?? 0).toFixed(1)}</strong>
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
            <p className={styles.streakTitle}>{data?.currentStreak ? `${data.currentStreak}일째 일기를 쓰고 있어요` : '첫 일기를 기다리고 있어요'}</p>
            <p className={styles.streakDescription}>
              {data?.currentStreak && data.currentStreak >= 3
                ? '꾸준히 기록하며 클로버를 모아보세요.'
                : '짧은 일기라도 괜찮아요.'}
            </p>
          </div>
        </div>
      </section>

      <section className={styles.diaryCard}>
        <h2 className={styles.diaryTitle}>오늘 기분은 어땠어?</h2>
        <p className={styles.diarySubtitle}>지금 마음과 가까운 표정을 골라줘.</p>

        <div className={styles.emotionRow}>
          {EMOTIONS.map((emotion) => {
            const isActive = selectedEmotion === emotion.key;

            return (
              <button
                key={emotion.key}
                type="button"
                className={isActive ? styles.emotionButtonActive : styles.emotionButton}
                aria-pressed={isActive}
                onClick={() => setSelectedEmotion(emotion.key)}
              >
                <span className={styles.emotionEmoji}>
                  <EmotionIcon emotion={emotion.key} />
                </span>
                <span className={styles.emotionLabel}>{emotion.label}</span>
              </button>
            );
          })}
        </div>

        <button type="button" className={styles.writeButton} onClick={handleWriteNow}>
          오늘 마음 기록하기
          <span aria-hidden="true">→</span>
        </button>

        <div className={styles.diaryMetaRow}>
          <p className={styles.diaryMetaText}>
            {data?.hasTodayDiary
              ? `오늘은 ${data.todayDiaryCount}개나 남겼어!`
              : '아직 오늘 일기는 없어요'}
          </p>
          <Link to={data?.hasTodayDiary ? '/diaries' : '/feed'} className={styles.metaLink}>
            {data?.hasTodayDiary ? '내 일기 보기' : '피드 보러 가기'}
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

      <section className={styles.goalCard}>
        {goal ? (
          <>
            <div className={styles.goalTopRow}>
              <div>
                <p className={styles.goalHeading}>내가 모으는 목표</p>
                <p className={styles.goalName}>{goal.productTitle}</p>
              </div>
              <span className={styles.goalBadge}>
                {goalRemaining === 0 ? '달성!' : `${goalRemaining} 클로버 남음`}
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
                  {goalRemaining === 0
                    ? '이제 가족에게 부탁할 수 있어!'
                    : `${goalRemaining} 클로버만 더 모으면 돼!`}
                </p>
              </div>
            </div>
            <Link to={`/shop/products/${goal.productId}`} className={styles.goalDetailLink}>
              목표 선물 보러 가기
            </Link>
          </>
        ) : (
          <div className={styles.goalEmpty}>
            <div>
              <p className={styles.goalHeading}>내가 모으는 목표</p>
              <p className={styles.goalName}>아직 정한 목표가 없어요</p>
            </div>
            <Link to="/shop" className={styles.goalDetailLink}>
              갖고 싶은 선물 고르기
            </Link>
          </div>
        )}
      </section>
    </div>
  );
}

export default HomePage;
