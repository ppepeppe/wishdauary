import CardCarousel from '../common/CardCarousel';
import { Link } from 'react-router-dom';
import { getVisibilityScopeLabel } from '../../constants/diary';
import { useState } from 'react';
import { getEmotionMeta } from '../../constants/emotion';
import type { DiaryComment, DiaryEntry } from '../../types/diary';
import { getActorDisplayName } from '../../utils/actorDisplay';
import FamilyFeedComments from './FamilyFeedComments';
import EmotionIcon from './EmotionIcon';
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

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(new Date(value));
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
        const emotion = getEmotionMeta(diary.emotion);
        const hearted = diary.hearted ?? false;
        const heartCount = diary.heartCount ?? 0;
        const pending = isHeartPending(diary.id);
        const commentsExpanded = expandedDiaryIds.includes(diary.id);

        return (
          <article key={getDiaryItemKey(diary)} className={styles.card}>
            <div className={styles.header}>
              <div className={styles.headerMain}>
                {diary.actorRelationLabel || diary.actorNickname ? (
                  <span className={styles.actor}>
                    {getActorDisplayName(diary.actorRelationLabel, diary.actorNickname)}
                  </span>
                ) : null}
                <div className={styles.meta}><time dateTime={getDiaryDisplayDate(diary)}>{formatDate(getDiaryDisplayDate(diary))}</time>
                  <span>{getVisibilityScopeLabel(diary.visibilityScope)}</span></div>
              </div>
              {emotion ? (
                <span className={styles.emotionBadge}>
                  <span aria-hidden="true">
                    <EmotionIcon emotion={emotion.key} />
                  </span>
                  {emotion.label}
                </span>
              ) : null}
            </div>

            <Link to={`/diaries/${diary.id}`} className={styles.previewLink} aria-label={`${diary.actorNickname || '아이'}의 일기 읽기`}>
              <p className={styles.preview}>{diary.contentPreview}</p>
              <span className={styles.readLink}>일기 읽기 <NavIcon name="arrowRight" /></span>
            </Link>

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
          </article>
        );
      })}
    </CardCarousel>
  );
}

export default FamilyFeedList;
