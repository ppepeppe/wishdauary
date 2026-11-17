import { Link } from 'react-router-dom';
import StatusMessage from '../components/common/StatusMessage';
import PageSection from '../components/common/PageSection';
import ReactionFeedList from '../components/diary/ReactionFeedList';
import { useReactionFeed } from '../hooks/useReactionFeed';

function FeedPage() {
  const { data, isLoading, error } = useReactionFeed();

  return (
    <PageSection title="소식">
      <p><Link to="/shared-feed">공개·그룹 피드 보기 →</Link></p>
      {isLoading ? <StatusMessage kind="loading" message="소식을 불러오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="소식을 불러오지 못했어요. 잠시 후 다시 시도해 주세요." /> : null}
      {data && data.length > 0 ? <ReactionFeedList items={data} /> : null}
      {data && data.length === 0 ? <StatusMessage kind="empty" message="아직 받은 반응이 없어요. 가족이 남긴 하트와 댓글이 여기에 표시돼요." /> : null}
    </PageSection>
  );
}

export default FeedPage;
