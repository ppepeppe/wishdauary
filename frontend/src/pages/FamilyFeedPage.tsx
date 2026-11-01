import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
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
    <PageSection
      title="아이 일기"
      description="아이가 공유한 하루를 읽고, 댓글과 하트로 마음을 전해 주세요."
    >
      <p><Link to="/family-connections">연결된 아이 관리 →</Link> · <Link to="/shared-feed">공개·그룹 일기 →</Link></p>
      {isLoading ? <StatusMessage kind="loading" message="아이의 일기를 불러오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="가족 이야기를 가져오다 잠시 멈췄어요. 조금 뒤에 다시 보면 괜찮아요." /> : null}
      {actionError ? <StatusMessage kind="error" message="반응을 남기는 중에 잠깐 멈췄어요. 한 번만 다시 시도해 주세요." /> : null}
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
        <StatusMessage kind="empty" message="아직 공유된 일기가 없어요. 아이와 연결된 뒤 아이가 가족에게 공유한 일기가 여기에 보여요." />
      ) : null}
    </PageSection>
  );
}

export default FamilyFeedPage;
