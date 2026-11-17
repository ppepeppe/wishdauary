import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import PageNavigation from '../components/common/PageNavigation';
import StatusMessage from '../components/common/StatusMessage';
import FamilyFeedList from '../components/diary/FamilyFeedList';
import { useFamilyFeed } from '../hooks/useFamilyFeed';

function FamilyFeedPage() {
  const {
    data,
    isLoading,
    error,
    actionError,
    toggleHeart,
    isHeartPending,
    loadComments,
    submitComment,
    getComments,
    getCommentError,
    isCommentLoading,
    isCommentSubmitting,
  } = useFamilyFeed();

  return (
    <PageSection title="아이 일기">
      <PageNavigation><Link to="/family-connections">아이 연결 →</Link><Link to="/shared-feed">공개·그룹 일기 →</Link></PageNavigation>
      {isLoading ? <StatusMessage kind="loading" message="아이의 일기를 불러오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="일기를 불러오지 못했어요. 잠시 후 다시 시도해 주세요." /> : null}
      {actionError ? <StatusMessage kind="error" message="반응을 남기지 못했어요. 다시 시도해 주세요." /> : null}
      {data && data.length > 0 ? (
        <FamilyFeedList
          diaries={data}
          onToggleHeart={toggleHeart}
          isHeartPending={isHeartPending}
          onLoadComments={loadComments}
          onSubmitComment={submitComment}
          getComments={getComments}
          getCommentError={getCommentError}
          isCommentLoading={isCommentLoading}
          isCommentSubmitting={isCommentSubmitting}
        />
      ) : null}
      {data && data.length === 0 ? (
        <StatusMessage kind="empty" message="공유받은 일기가 없어요." />
      ) : null}
    </PageSection>
  );
}

export default FamilyFeedPage;
