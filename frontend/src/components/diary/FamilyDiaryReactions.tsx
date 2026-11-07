import { useEffect, useRef, useState } from 'react';
import Button from '../common/Button';
import StatusMessage from '../common/StatusMessage';
import FamilyFeedComments from './FamilyFeedComments';
import { createDiaryComment, toggleDiaryHeart } from '../../services/api/diaryApi';
import type { DiaryComment, DiaryDetail, ToggleDiaryHeartResponse } from '../../types/diary';

interface Props {
  diary: DiaryDetail;
  comments: DiaryComment[];
  onCommentCreated: (comment: DiaryComment) => void;
  onHeartChanged: (result: ToggleDiaryHeartResponse) => void;
}

export default function FamilyDiaryReactions({ diary, comments, onCommentCreated, onHeartChanged }: Props) {
  const [commentBusy, setCommentBusy] = useState(false);
  const [heartBusy, setHeartBusy] = useState(false);
  const [commentError, setCommentError] = useState<string | null>(null);
  const [heartError, setHeartError] = useState<string | null>(null);
  const commentPending = useRef(false);
  const heartPending = useRef(false);
  const mounted = useRef(true);
  useEffect(() => {
    mounted.current = true;
    return () => { mounted.current = false; };
  }, []);

  async function submitComment(diaryId: number, content: string) {
    if (commentPending.current) return false;
    commentPending.current = true;
    setCommentBusy(true); setCommentError(null);
    try {
      const comment = await createDiaryComment({ diaryId, content });
      if (mounted.current) onCommentCreated(comment);
      return true;
    } catch (caught) {
      setCommentError(caught instanceof Error ? caught.message : '댓글을 남기지 못했어요.');
      return false;
    } finally {
      commentPending.current = false;
      setCommentBusy(false);
    }
  }

  async function toggleHeart() {
    if (heartPending.current) return;
    heartPending.current = true;
    setHeartBusy(true); setHeartError(null);
    try {
      const result = await toggleDiaryHeart(diary.id);
      if (mounted.current) onHeartChanged(result);
    } catch (caught) {
      setHeartError(caught instanceof Error ? caught.message : '하트를 보내지 못했어요.');
    } finally {
      heartPending.current = false;
      setHeartBusy(false);
    }
  }

  return <>
    <Button type="button" variant="secondary" aria-pressed={Boolean(diary.hearted)} disabled={heartBusy}
      onClick={() => void toggleHeart()}>
      {heartBusy ? '반영 중…' : diary.hearted ? '♥ 하트 취소하기' : '♡ 하트 보내기'}
    </Button>
    {heartError ? <StatusMessage kind="error" message={heartError} /> : null}
    <FamilyFeedComments diaryId={diary.id} comments={comments} isLoading={false}
      isSubmitting={commentBusy} error={commentError} onSubmitComment={submitComment} />
  </>;
}
