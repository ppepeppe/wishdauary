import { getChildGiftRequests, getChildPricingRequests } from '../utils/childRequests';
import ChildSummaryCard from '../components/family/ChildSummaryCard';
import CardCarousel from '../components/common/CardCarousel';
import Tabs from '../components/common/Tabs';
import FamilyEnglishReviews from '../components/rewards/FamilyEnglishReviews';
import { useSelectedChild } from '../hooks/useSelectedChild';
import { getChildDiaries } from '../utils/childDiaries';
import { useCallback, useEffect, useState } from 'react';
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
  const [gifts, pricing] = await Promise.all([fetchReceivedGiftRequests(), fetchProductPricingRequests()]);
  return { gifts: gifts.filter((item) => item.status === 'pending'), pricing: pricing.filter(product => product.cloverPrice <= 0) };
}

function FamilyHomePage() {
  const session = getAuthSession();
  const [englishPending, setEnglishPending] = useState<{ childId: number | undefined; count: number | undefined }>();
  const [summaryRevision, setSummaryRevision] = useState(0);
  const connections = useAsyncData(fetchFamilyConnectionState);
  const diaries = useAsyncData(fetchFamilyFeed);
  const requests = useAsyncData(loadRequests);
  const children = connections.data?.connections ?? [];
  const { childId, selectChild } = useSelectedChild(children);
  const selectedChild = children.find(child => child.userId === childId);
  const handleEnglishCountChange = useCallback((count: number | undefined) => {
    setEnglishPending({ childId, count });
  }, [childId]);
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
  const todayDiaryCount = diaries.data && !diaries.error
    ? getChildDiaries(diaries.data, childId, getTodayDateInKorea()).length : null;
  const childGifts = getChildGiftRequests(requests.data?.gifts ?? [], childId);
  const childPricing = getChildPricingRequests(requests.data?.pricing ?? [], childId);
  const englishPendingCount = englishPending?.childId === childId ? englishPending?.count : undefined;
  const pendingCount = requests.data && !requests.error && englishPendingCount !== undefined
    ? childGifts.length + childPricing.length + englishPendingCount : undefined;

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

      <section className={styles.section} aria-labelledby="family-children-title">
        <div className={styles.heading}><h2 id="family-children-title">함께하는 아이</h2><Link to="/family-connections">연결 관리 →</Link></div>
        {connections.isLoading && !connections.data ? <StatusMessage kind="loading" message="연결된 아이를 확인하고 있어요." /> : null}
        {connections.error ? <><StatusMessage kind="error" message={connections.error} /><Button variant="ghost" onClick={() => void refreshConnections()}>아이 연결 다시 불러오기</Button></> : null}
        {children.length > 0 ? <Tabs label="아이 선택" value={String(childId)} onChange={selectChild}
          options={children.map(child => ({ value: String(child.userId), label: child.nickname }))}>
          {selectedChild ? <ChildSummaryCard key={`${selectedChild.userId}:${summaryRevision}`} child={selectedChild} todayDiaryCount={todayDiaryCount} /> : null}
          {diaries.error ? <><StatusMessage kind="error" message="오늘 공유한 일기 수를 확인하지 못했어요." /><Button variant="ghost" onClick={() => void refreshDiaries()}>일기 수 다시 불러오기</Button></> : null}
        </Tabs> : null}
        {connections.data?.connections.length === 0 ? <p className={styles.helper}>{pendingConnections.length ? '아이의 연결 승인을 기다리고 있어요.' : '연결된 아이가 없어요.'}</p> : null}
        {pendingConnections.length > 0 ? <Link to="/family-connections" className={styles.pendingLink}>아이의 승인을 기다리는 요청 {pendingConnections.length}개 →</Link> : null}
        {connections.data && children.length === 0 && pendingConnections.length === 0 ? <Link to="/family-connections" className={styles.primaryLink}>아이 찾아 연결하기 <span aria-hidden="true">→</span></Link> : null}
      </section>

      {selectedChild ? <section className={styles.section} aria-labelledby="family-requests-title">
        <div className={styles.heading}><h2 id="family-requests-title">확인할 요청 {pendingCount !== undefined ? <span>{pendingCount}</span> : null}</h2><Link to="/family-settings">모든 아이 요청 보기 →</Link></div>
        {requests.isLoading && !requests.data ? <StatusMessage kind="loading" message="선물·가격 설정 요청을 불러오고 있어요." /> : null}
        {requests.error ? <><StatusMessage kind="error" message={requests.error} /><Button variant="ghost" onClick={() => void refreshRequests()}>선물 요청 다시 불러오기</Button></> : null}
        <CardCarousel label="선물 승인 요청" itemLabel="요청">{childGifts.map((gift) => <Link key={gift.notificationId} to="/family-settings" className={styles.requestRow}>
          <span><strong>{gift.productTitle}</strong><small>{gift.requesterChildNickname} · 선물 요청</small></span><span className={styles.requestStatus}>승인 대기 →</span>
        </Link>)}</CardCarousel>
        {childPricing.map(product => <Link key={product.productId} to="/family-settings" className={styles.requestRow}>
          <span><strong>{product.title}</strong><small>선물 가격 설정 요청</small></span><span className={styles.requestStatus}>가격 정하기 →</span>
        </Link>)}
        <FamilyEnglishReviews key={childId} childId={childId} embedded onPendingCountChange={handleEnglishCountChange}
          onChanged={async () => { await refreshRequests(); setSummaryRevision(value => value + 1); }} />
        {pendingCount === 0 ? <p className={styles.helper}>이 아이의 대기 중인 요청이 없어요.</p> : null}
      </section> : null}

      <Link to="/family-shop" className={styles.shopLink}>
        <NavIcon name="store" /><span><strong>상점 관리</strong><small>공통 상점 · 선물 관리</small></span><span aria-hidden="true">→</span>
      </Link>
    </div>
  );
}

export default FamilyHomePage;
