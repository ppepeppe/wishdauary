import { Link } from 'react-router-dom';
import Tabs from '../components/common/Tabs';
import Button from '../components/common/Button';
import { useAsyncData } from '../hooks/useAsyncData';
import { useSelectedChild } from '../hooks/useSelectedChild';
import { fetchFamilyConnections } from '../services/api/familyConnectionApi';
import { getChildDiaries } from '../utils/childDiaries';
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

  const connections = useAsyncData(fetchFamilyConnections, true);
  const children = connections.data ?? [];
  const { childId, selectChild } = useSelectedChild(children);
  const childDiaries = getChildDiaries(data ?? [], childId);

  return (
    <PageSection title="아이 일기">
      <PageNavigation><Link to="/family-connections">아이 연결 →</Link><Link to="/shared-feed">공개·그룹 일기 →</Link></PageNavigation>
      {isLoading ? <StatusMessage kind="loading" message="아이의 일기를 불러오고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message="일기를 불러오지 못했어요. 잠시 후 다시 시도해 주세요." /> : null}
      {actionError ? <StatusMessage kind="error" message="반응을 남기지 못했어요. 다시 시도해 주세요." /> : null}
      {connections.error ? <><StatusMessage kind="error" message={connections.error} /><Button variant="ghost" onClick={() => void connections.refetch()}>아이 목록 다시 불러오기</Button></> : null}
      {children.length > 0 ? <Tabs label="아이 선택" value={String(childId)} onChange={selectChild}
        options={children.map(child => ({ value: String(child.userId), label: child.nickname }))}>
      {childDiaries.length > 0 ? (
        <FamilyFeedList
          key={childId}
          diaries={childDiaries}
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
      {data && childDiaries.length === 0 ? (
        <StatusMessage kind="empty" message="공유받은 일기가 없어요." />
      ) : null}
      </Tabs> : null}
      {connections.data?.length === 0 ? <StatusMessage kind="empty" message="연결된 아이가 없어요. 아이 연결에서 요청을 보내 주세요." /> : null}
    </PageSection>
  );
}

export default FamilyFeedPage;
