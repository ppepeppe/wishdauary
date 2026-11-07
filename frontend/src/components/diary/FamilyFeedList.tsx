import CardCarousel from '../common/CardCarousel';
import { Link } from 'react-router-dom';
import { getVisibilityScopeLabel } from '../../constants/diary';
import { useState } from 'react';
import { getEmotionMeta } from '../../constants/emotion';
import type { DiaryComment, DiaryEntry } from '../../types/diary';
import { getActorDisplayName } from '../../utils/actorDisplay';
import FamilyFeedComments from './FamilyFeedComments';
import EmotionIcon from './EmotionIcon';
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
                <strong>{formatDate(getDiaryDisplayDate(diary))}</strong>
                {diary.actorRelationLabel || diary.actorNickname ? (
                  <span className={styles.actor}>
                    {getActorDisplayName(diary.actorRelationLabel, diary.actorNickname)}
                  </span>
                ) : null}
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

            <div className={styles.previewBox}>
              <small>{getVisibilityScopeLabel(diary.visibilityScope)}</small>
              <p className={styles.preview}>{diary.contentPreview}</p>
              <Link to={`/diaries/${diary.id}`}>이야기 읽기 →</Link>
            </div>

            <div className={styles.footer}>
              <div className={styles.summary}>
                <span className={styles.countLabel}>하트 {heartCount}</span>
                <span className={styles.countLabel}>댓글 {diary.commentCount ?? 0}</span>
              </div>
              <div className={styles.actions}>
                <button
                  type="button"
                  className={`${styles.heartButton} ${hearted ? styles.heartButtonActive : ''}`}
                  disabled={pending}
                  onClick={() => void onToggleHeart(diary.id)}
                >
                  <span aria-hidden="true">{hearted ? '♥' : '♡'}</span>
                  {pending ? '반영 중...' : '하트 보내기'}
                </button>
                <button
                  type="button"
                  className={styles.commentButton}
                  onClick={() => void handleToggleComments(diary.id)}
                >
                  {commentsExpanded ? '댓글 닫기' : '댓글 보기'}
                </button>
              </div>
            </div>

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
          </article>
        );
      })}
    </CardCarousel>
  );
}

export default FamilyFeedList;
