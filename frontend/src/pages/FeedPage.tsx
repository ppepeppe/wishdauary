import { Link } from 'react-router-dom';
import StatusMessage from '../components/common/StatusMessage';
import PageSection from '../components/common/PageSection';
import ReactionFeedList from '../components/diary/ReactionFeedList';
import { useReactionFeed } from '../hooks/useReactionFeed';

function FeedPage() {
  const { data, isLoading, error } = useReactionFeed();

  return (
    <PageSection
      title="최근 반응"
      description="가족이 남긴 따뜻한 반응을 시간순으로 확인할 수 있어요."
    >
      <p><Link to="/shared-feed">공개·그룹 피드 보기 →</Link></p>
      {isLoading ? <StatusMessage kind="loading" message="최근 반응을 천천히 모아오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="반응을 불러오다 잠시 멈췄어요. 조금 뒤에 다시 보면 괜찮아요." /> : null}
      {data && data.length > 0 ? <ReactionFeedList items={data} /> : null}
      {data && data.length === 0 ? <StatusMessage kind="empty" message="아직 도착한 반응이 없어요. 기록을 남기면 가족의 마음이 여기에 차곡차곡 보여요." /> : null}
    </PageSection>
  );
}

export default FeedPage;
