import CardCarousel from '../components/common/CardCarousel';
import Tabs from '../components/common/Tabs';
import FamilyEnglishReviews from '../components/rewards/FamilyEnglishReviews';
import { fetchEnglishReviews } from '../services/api/rewardApi';
import { useSelectedChild } from '../hooks/useSelectedChild';
import { getChildDiaries } from '../utils/childDiaries';
import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import Button from '../components/common/Button';
import NavIcon from '../components/common/NavIcon';
import StatusMessage from '../components/common/StatusMessage';
import { formatKoreanDate, getTodayDateInKorea } from '../constants/date';
import { useAsyncData } from '../hooks/useAsyncData';
import { fetchFamilyFeed } from '../services/api/diaryApi';
import { fetchFamilyConnectionState } from '../services/api/familyConnectionApi';
import { fetchProductPricingRequests, fetchReceivedGiftRequests } from '../services/api/shopApi';
import { getAuthSession } from '../services/session/authSession';
import styles from './FamilyHomePage.module.css';

async function loadRequests() {
  const [gifts, pricing, english] = await Promise.all([fetchReceivedGiftRequests(), fetchProductPricingRequests(), fetchEnglishReviews()]);
  return { gifts: gifts.filter((item) => item.status === 'pending'), pricing, englishCount: english.length };
}

function FamilyHomePage() {
  const session = getAuthSession();
  const connections = useAsyncData(fetchFamilyConnectionState);
  const diaries = useAsyncData(fetchFamilyFeed);
  const requests = useAsyncData(loadRequests);
  const children = connections.data?.connections ?? [];
  const { childId, selectChild } = useSelectedChild(children);
  const selectedChild = children.find(child => child.userId === childId);
  const { refetch: refreshConnections } = connections;
  const { refetch: refreshDiaries } = diaries;
  const { refetch: refreshRequests } = requests;

  useEffect(() => {
    const refresh = () => {
      void refreshConnections(); void refreshDiaries(); void refreshRequests();
    };
    window.addEventListener('focus', refresh);
    return () => window.removeEventListener('focus', refresh);
  }, [refreshConnections, refreshDiaries, refreshRequests]);

  const pendingConnections = connections.data?.requests.filter((item) => item.status === 'pending') ?? [];
  const recentDiaries = getChildDiaries(diaries.data ?? [], childId).sort((a, b) =>
    (b.writtenDate ?? b.createdAt).localeCompare(a.writtenDate ?? a.createdAt)).slice(0, 3);
  const pendingCount = (requests.data?.gifts.length ?? 0) + (requests.data?.pricing.length ?? 0);

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <span className={styles.wordmark}>wish 다유어리 <span>가족</span></span>
        <Link to="/me" className={styles.profileLink} aria-label="마이페이지"><NavIcon name="profile" /></Link>
      </header>
      <section className={styles.greeting} aria-labelledby="family-home-title">
        <p>{formatKoreanDate(getTodayDateInKorea(), false)}</p>
        <h1 id="family-home-title"><span className={styles.greetingName}>{session?.nickname || session?.relationLabel || '가족'}님, 안녕하세요.</span></h1>
      </section>

      {requests.data ? <Link to="/family-settings" className={styles.requestOverview}>
        <span className={styles.overviewIcon}><NavIcon name="gift" /></span>
        <span><strong>받은 요청 <b>{pendingCount + (requests.data.englishCount ?? 0)}</b></strong></span>
        <NavIcon name="arrowRight" />
      </Link> : null}

      <section className={styles.section} aria-labelledby="family-children-title">
        <div className={styles.heading}><h2 id="family-children-title">함께하는 아이</h2><Link to="/family-connections">연결 관리 →</Link></div>
        {connections.isLoading && !connections.data ? <StatusMessage kind="loading" message="연결된 아이를 확인하고 있어요." /> : null}
        {connections.error ? <><StatusMessage kind="error" message={connections.error} /><Button variant="ghost" onClick={() => void refreshConnections()}>아이 연결 다시 불러오기</Button></> : null}
        {children.length > 0 ? <Tabs label="아이 선택" value={String(childId)} onChange={selectChild}
          options={children.map(child => ({ value: String(child.userId), label: child.nickname }))}>
          {selectedChild ? <Link to={`/family/children/${selectedChild.userId}`} className={styles.pendingLink}>{selectedChild.nickname} 프로필 · 선물 요청 · 클로버 내역 →</Link> : null}
          {diaries.isLoading && !diaries.data ? <StatusMessage kind="loading" message="아이의 일기를 불러오고 있어요." /> : null}
          {diaries.error ? <><StatusMessage kind="error" message={diaries.error} /><Button variant="ghost" onClick={() => void refreshDiaries()}>일기 다시 불러오기</Button></> : null}
          <CardCarousel key={`diaries-${childId}`} label="최근 아이 일기" itemLabel="일기">{recentDiaries.map(diary => <Link key={diary.id} to={`/diaries/${diary.id}`} className={styles.diaryRow}>
            <div className={styles.meta}><strong>{diary.actorNickname || '아이'}</strong><time>{formatKoreanDate(diary.writtenDate ?? diary.createdAt, false)}</time></div>
            <p>{diary.contentPreview}</p><span className={styles.readLink}>일기 읽기 <span aria-hidden="true">→</span></span>
          </Link>)}</CardCarousel>
          {diaries.data && recentDiaries.length === 0 ? <p className={styles.helper}>이 아이가 공유한 일기가 없어요.</p> : null}
        </Tabs> : null}
        {connections.data?.connections.length === 0 ? <p className={styles.helper}>{pendingConnections.length ? '아이의 연결 승인을 기다리고 있어요.' : '연결된 아이가 없어요.'}</p> : null}
        {pendingConnections.length > 0 ? <Link to="/family-connections" className={styles.pendingLink}>아이의 승인을 기다리는 요청 {pendingConnections.length}개 →</Link> : null}
        {connections.data && children.length === 0 && pendingConnections.length === 0 ? <Link to="/family-connections" className={styles.primaryLink}>아이 찾아 연결하기 <span aria-hidden="true">→</span></Link> : null}
      </section>

      <FamilyEnglishReviews onChanged={refreshRequests} />

      <section className={styles.section} aria-labelledby="family-gifts-title">
        <div className={styles.heading}><h2 id="family-gifts-title">확인할 선물 요청 {requests.data ? <span>{pendingCount}</span> : null}</h2><Link to="/family-settings">요청 보기 →</Link></div>
        {requests.isLoading && !requests.data ? <StatusMessage kind="loading" message="선물 요청을 확인하고 있어요." /> : null}
        {requests.error ? <><StatusMessage kind="error" message={requests.error} /><Button variant="ghost" onClick={() => void refreshRequests()}>선물 요청 다시 불러오기</Button></> : null}
        <CardCarousel label="확인할 선물 요청" itemLabel="요청">{requests.data?.gifts.map((gift) => <Link key={gift.notificationId} to="/family-settings" className={styles.requestRow}>
          <span><strong>{gift.productTitle}</strong><small>{gift.requesterChildNickname}의 요청</small></span><span className={styles.requestStatus}>승인 대기 →</span>
        </Link>)}</CardCarousel>
        {requests.data && requests.data.pricing.length > 0 ? <Link to="/family-settings" className={styles.pendingLink}>가격을 정해 줄 선물 {requests.data.pricing.length}개 →</Link> : null}
        {requests.data && pendingCount === 0 ? <p className={styles.helper}>대기 중인 요청이 없어요.</p> : null}
      </section>

      <Link to="/family-shop" className={styles.shopLink}>
        <NavIcon name="store" /><span><strong>상점 관리</strong><small>공통 상점 · 선물 관리</small></span><span aria-hidden="true">→</span>
      </Link>
    </div>
  );
}

export default FamilyHomePage;
