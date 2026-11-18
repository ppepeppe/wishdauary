import CardCarousel from '../common/CardCarousel';
import { useState } from 'react';
import type { DiaryComment, DiaryEntry } from '../../types/diary';
import { getActorDisplayName } from '../../utils/actorDisplay';
import FamilyFeedComments from './FamilyFeedComments';
import DiaryFeedCard from './DiaryFeedCard';
import NavIcon from '../common/NavIcon';
import styles from './FamilyFeedList.module.css';

interface FamilyFeedListProps {
  diaries: DiaryEntry[];
  onToggleHeart: (diaryId: number) => Promise<void>;
  isHeartPending: (diaryId: number) => boolean;
  onLoadComments: (diaryId: number) => Promise<void>;
  onSubmitComment: (diaryId: number, content: string) => Promise<boolean>;
  getComments: (diaryId: number) => DiaryComment[];
  getCommentError: (diaryId: number) => string | null;
  isCommentLoading: (diaryId: number) => boolean;
  isCommentSubmitting: (diaryId: number) => boolean;
}

function getDiaryDisplayDate(diary: DiaryEntry) {
  return diary.writtenDate ?? diary.createdAt;
}

function getDiaryItemKey(diary: DiaryEntry) {
  return `${diary.id}-${getDiaryDisplayDate(diary)}`;
}

function FamilyFeedList({
  diaries,
  onToggleHeart,
  isHeartPending,
  onLoadComments,
  onSubmitComment,
  getComments,
  getCommentError,
  isCommentLoading,
  isCommentSubmitting,
}: FamilyFeedListProps) {
  const [expandedDiaryIds, setExpandedDiaryIds] = useState<number[]>([]);

  async function handleToggleComments(diaryId: number) {
    const isExpanded = expandedDiaryIds.includes(diaryId);

    if (isExpanded) {
      setExpandedDiaryIds((current) => current.filter((id) => id !== diaryId));
      return;
    }

    setExpandedDiaryIds((current) => [...current, diaryId]);
    await onLoadComments(diaryId);
  }

  return (
    <CardCarousel title="아이의 이야기" itemLabel="일기">
      {diaries.map((diary) => {
        const hearted = diary.hearted ?? false;
        const heartCount = diary.heartCount ?? 0;
        const pending = isHeartPending(diary.id);
        const commentsExpanded = expandedDiaryIds.includes(diary.id);

        return (
          <DiaryFeedCard key={getDiaryItemKey(diary)} diaryId={diary.id}
            author={getActorDisplayName(diary.actorRelationLabel, diary.actorNickname) || '아이'}
            writtenDate={getDiaryDisplayDate(diary)} visibilityScope={diary.visibilityScope}
            emotion={diary.emotion} contentPreview={diary.contentPreview}>
            <div className={styles.actions}>
              <button type="button" className={`${styles.reactionButton} ${hearted ? styles.heartButtonActive : ''}`}
                disabled={pending} aria-pressed={hearted} aria-label={`하트 ${heartCount}개, ${hearted ? '하트 취소' : '하트 보내기'}`}
                onClick={() => void onToggleHeart(diary.id)}>
                <NavIcon name="feed" /><span>하트 {heartCount}</span>
              </button>
              <button type="button" className={styles.reactionButton} aria-expanded={commentsExpanded}
                aria-controls={`diary-comments-${diary.id}`} onClick={() => void handleToggleComments(diary.id)}>
                <NavIcon name="comment" /><span>댓글 {diary.commentCount ?? 0}</span>
              </button>
            </div>

            <div id={`diary-comments-${diary.id}`} hidden={!commentsExpanded}>
            {commentsExpanded ? (
              <FamilyFeedComments
                diaryId={diary.id}
                comments={getComments(diary.id)}
                isLoading={isCommentLoading(diary.id)}
                isSubmitting={isCommentSubmitting(diary.id)}
                error={getCommentError(diary.id)}
                onSubmitComment={onSubmitComment}
              />
            ) : null}
            </div>
          </DiaryFeedCard>
        );
      })}
    </CardCarousel>
  );
}

export default FamilyFeedList;
