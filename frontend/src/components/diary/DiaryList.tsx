import CardCarousel from '../common/CardCarousel';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { getVisibilityScopeLabel } from '../../constants/diary';
import { getEmotionMeta } from '../../constants/emotion';
import { fetchDiaryComments } from '../../services/api/diaryApi';
import type { DiaryComment, DiaryEntry } from '../../types/diary';
import { getActorDisplayName } from '../../utils/actorDisplay';
import DiaryReactionSummary from './DiaryReactionSummary';
import EmotionIcon from './EmotionIcon';
import styles from './DiaryList.module.css';

interface DiaryListProps {
  diaries: DiaryEntry[];
  showVisibilityScope?: boolean;
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

function formatCommentDate(value?: string) {
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

function DiaryList({ diaries, showVisibilityScope = true }: DiaryListProps) {
  const [expandedDiaryIds, setExpandedDiaryIds] = useState<number[]>([]);
  const [commentsByDiaryId, setCommentsByDiaryId] = useState<Record<number, DiaryComment[]>>({});
  const [loadingDiaryIds, setLoadingDiaryIds] = useState<number[]>([]);
  const [commentErrors, setCommentErrors] = useState<Record<number, string | null>>({});

  function isExpanded(diaryId: number) {
    return expandedDiaryIds.includes(diaryId);
  }

  function isLoadingComments(diaryId: number) {
    return loadingDiaryIds.includes(diaryId);
  }

  async function handleToggleComments(diaryId: number) {
    if (isExpanded(diaryId)) {
      setExpandedDiaryIds((current) => current.filter((id) => id !== diaryId));
      return;
    }

    setExpandedDiaryIds((current) => [...current, diaryId]);

    if (commentsByDiaryId[diaryId] || isLoadingComments(diaryId)) {
      return;
    }

    setCommentErrors((current) => ({ ...current, [diaryId]: null }));
    setLoadingDiaryIds((current) => [...current, diaryId]);

    try {
      const comments = await fetchDiaryComments(diaryId);
      setCommentsByDiaryId((current) => ({ ...current, [diaryId]: comments }));
    } catch {
      setCommentErrors((current) => ({
        ...current,
        [diaryId]: '댓글을 불러오지 못했어요. 잠시 후 다시 확인해 주세요.',
      }));
    } finally {
      setLoadingDiaryIds((current) => current.filter((id) => id !== diaryId));
    }
  }

  return (
    <CardCarousel title="일기 목록" itemLabel="일기">
      {diaries.map((diary) => {
        const emotion = getEmotionMeta(diary.emotion);
        const commentCount = diary.commentCount ?? 0;
        const comments = commentsByDiaryId[diary.id] ?? [];
        const expanded = isExpanded(diary.id);

        return (
          <article key={getDiaryItemKey(diary)} className={styles.card}>
            <div className={styles.header}>
              <strong>{formatDate(getDiaryDisplayDate(diary))}</strong>
              {showVisibilityScope ? (
                <span className={styles.scope}>{getVisibilityScopeLabel(diary.visibilityScope)}</span>
              ) : null}
            </div>
            <p className={styles.preview}>
              {emotion ? (
                <span className={styles.emotion} aria-label={emotion.label}>
                  <EmotionIcon emotion={emotion.key} />
                </span>
              ) : null}
              {diary.contentPreview}
            </p>
            <Link to={`/diaries/${diary.id}`} className={styles.detailLink}>
              자세히 읽기
            </Link>
            <DiaryReactionSummary heartCount={diary.heartCount} commentCount={diary.commentCount} />
            {commentCount > 0 ? (
              <button
                type="button"
                className={styles.commentToggle}
                onClick={() => void handleToggleComments(diary.id)}
              >
                {expanded ? '댓글 닫기' : '댓글 보기'}
              </button>
            ) : null}
            {expanded ? (
              <div className={styles.commentPanel}>
                {isLoadingComments(diary.id) ? <p className={styles.commentStatus}>댓글을 불러오는 중이에요.</p> : null}
                {commentErrors[diary.id] ? <p className={styles.commentError}>{commentErrors[diary.id]}</p> : null}
                {!isLoadingComments(diary.id) && !commentErrors[diary.id] && comments.length === 0 ? (
                  <p className={styles.commentStatus}>아직 댓글이 없어요.</p>
                ) : null}
                {comments.length > 0 ? (
                  <CardCarousel title="댓글" itemLabel="댓글">
                    {comments.map((comment) => (
                      <article key={getCommentItemKey(comment)} className={styles.commentItem}>
                        <p className={styles.commentContent}>
                          <span className={styles.commentActor}>
                            {getActorDisplayName(comment.actorRelationLabel, comment.actorNickname)}
                          </span>
                          <span className={styles.commentSeparator}>·</span>
                          {comment.content}
                        </p>
                        {formatCommentDate(comment.createdAt) ? (
                          <span className={styles.commentMeta}>{formatCommentDate(comment.createdAt)}</span>
                        ) : null}
                      </article>
                    ))}
                  </CardCarousel>
                ) : null}
              </div>
            ) : null}
          </article>
        );
      })}
    </CardCarousel>
  );
}

export default DiaryList;
