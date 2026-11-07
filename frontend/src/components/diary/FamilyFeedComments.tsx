import CardCarousel from '../common/CardCarousel';
import { useState } from 'react';
import type { DiaryComment } from '../../types/diary';
import { getActorDisplayName } from '../../utils/actorDisplay';
import HoverHint from '../common/HoverHint';
import styles from './FamilyFeedComments.module.css';

interface FamilyFeedCommentsProps {
  diaryId: number;
  comments: DiaryComment[];
  isLoading: boolean;
  isSubmitting: boolean;
  error: string | null;
  onSubmitComment: (diaryId: number, content: string) => Promise<boolean>;
}

function formatDate(value?: string) {
  if (!value) {
    return null;
  }

  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value));
}

function getCommentItemKey(comment: DiaryComment) {
  return `${comment.id}-${comment.createdAt ?? comment.content}`;
}

function FamilyFeedComments({
  diaryId,
  comments,
  isLoading,
  isSubmitting,
  error,
  onSubmitComment,
}: FamilyFeedCommentsProps) {
  const [content, setContent] = useState('');
  const trimmedContent = content.trim();

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!trimmedContent || isSubmitting || isLoading) {
      return;
    }

    if (await onSubmitComment(diaryId, trimmedContent)) setContent('');
  }

  return (
    <div className={styles.panel}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <textarea
          className={styles.input}
          rows={3}
          value={content}
          disabled={isSubmitting}
          placeholder="따뜻한 한마디를 남겨보세요."
          onChange={(event) => setContent(event.target.value)}
        />
        <HoverHint
          hint={!isSubmitting && !trimmedContent ? '한마디를 적으면 남길 수 있어요' : null}
          className={styles.submitWrap}
        >
          <button type="submit" className={styles.submitButton} disabled={isSubmitting || isLoading || !trimmedContent}>
            {isSubmitting ? '남기는 중...' : '작성'}
          </button>
        </HoverHint>
      </form>

      {isLoading ? <p className={styles.status}>댓글을 불러오는 중이에요.</p> : null}
      {error ? <p className={styles.error}>{error}</p> : null}
      {!isLoading && !error && comments.length === 0 ? <p className={styles.status}>아직 댓글이 없어요.</p> : null}

      {comments.length > 0 ? (
        <CardCarousel title="댓글" itemLabel="댓글">
          {comments.map((comment) => (
            <article key={getCommentItemKey(comment)} className={styles.commentItem}>
              <p className={styles.commentContent}>
                <span className={styles.actor}>
                  {getActorDisplayName(comment.actorRelationLabel, comment.actorNickname)}
                </span>
                <span className={styles.separator}>·</span>
                {comment.content}
              </p>
              {formatDate(comment.createdAt) ? (
                <span className={styles.commentMeta}>{formatDate(comment.createdAt)}</span>
              ) : null}
            </article>
          ))}
        </CardCarousel>
      ) : null}
    </div>
  );
}

export default FamilyFeedComments;
