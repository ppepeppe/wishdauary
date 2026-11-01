import DiaryAudiencePicker from '../components/sharing/DiaryAudiencePicker';
import { getAuthSession } from '../services/session/authSession';
import { useEffect, useState } from 'react';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import Button from '../components/common/Button';
import HoverHint from '../components/common/HoverHint';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import EmotionIcon from '../components/diary/EmotionIcon';
import EmotionSelector from '../components/diary/EmotionSelector';
import { getVisibilityScopeLabel } from '../constants/diary';
import { getEmotionMeta } from '../constants/emotion';
import { fetchDiaryComments, fetchDiaryDetail, updateDiary, deleteDiary } from '../services/api/diaryApi';
import type { DiaryComment, DiaryDetail, DiaryEmotion, DiaryVisibilityScope } from '../types/diary';
import { getActorDisplayName } from '../utils/actorDisplay';
import styles from './DiaryDetailPage.module.css';

function formatDate(value?: string) {
  if (!value) {
    return '';
  }

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function DiaryDetailPage() {
  const { diaryId } = useParams();
  const navigate = useNavigate();
  const parsedDiaryId = Number(diaryId);
  const [diary, setDiary] = useState<DiaryDetail | null>(null);
  const [comments, setComments] = useState<DiaryComment[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [content, setContent] = useState('');
  const [visibilityScope, setVisibilityScope] = useState<DiaryVisibilityScope>('private');
  const [sharingGroupIds, setSharingGroupIds] = useState<number[]>([]);
  const isOwner = diary?.authorId === getAuthSession()?.userId;
  const [emotion, setEmotion] = useState<DiaryEmotion | null>(null);

  useEffect(() => {
    if (!Number.isFinite(parsedDiaryId)) {
      return;
    }

    let active = true;
    setDiary(null); setComments([]); setIsEditing(false); setActionError(null);

    async function load() {
      try {
        setIsLoading(true);
        setError(null);
        const detail = await fetchDiaryDetail(parsedDiaryId);
        if (!active) return;
        setDiary(detail);
        setContent(detail.content);
        setVisibilityScope(detail.visibilityScope);
        setSharingGroupIds(detail.sharingGroupIds ?? []);
        setEmotion(detail.emotion ?? null);
        if ((detail.commentCount ?? 0) > 0) {
          const nextComments = await fetchDiaryComments(parsedDiaryId);
          if (active) setComments(nextComments);
        } else {
          setComments([]);
        }
      } catch (caughtError) {
        if (active) setError(caughtError instanceof Error ? caughtError.message : '일기를 불러오지 못했어요.');
      } finally {
        if (active) setIsLoading(false);
      }
    }

    void load();
    return () => { active = false; };
  }, [parsedDiaryId]);

  if (!Number.isFinite(parsedDiaryId)) {
    return <Navigate to="/diaries" replace />;
  }

  async function handleSave(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedContent = content.trim();

    if (!isOwner || !trimmedContent || (visibilityScope === 'group' && sharingGroupIds.length === 0)) {
      return;
    }

    try {
      setIsSaving(true);
      setActionError(null);
      const updatedDiary = await updateDiary(parsedDiaryId, {
        content: trimmedContent,
        visibilityScope,
        sharingGroupIds,
        emotion,
      });
      setDiary(updatedDiary);
      setContent(updatedDiary.content);
      setVisibilityScope(updatedDiary.visibilityScope);
      setSharingGroupIds(updatedDiary.sharingGroupIds ?? []);
      setEmotion(updatedDiary.emotion ?? null);
      setIsEditing(false);
    } catch (caughtError) {
      setActionError(caughtError instanceof Error ? caughtError.message : '일기를 수정하지 못했어요.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm('이 일기를 삭제할까요? 삭제한 일기는 다시 볼 수 없어요.')) {
      return;
    }

    try {
      setIsDeleting(true);
      setActionError(null);
      await deleteDiary(parsedDiaryId);
      navigate('/diaries', { replace: true });
    } catch (caughtError) {
      setActionError(caughtError instanceof Error ? caughtError.message : '일기를 삭제하지 못했어요.');
    } finally {
      setIsDeleting(false);
    }
  }

  return (
    <PageSection
      title="일기 상세"
      description="남겨둔 마음을 다시 읽고 필요하면 조용히 다듬을 수 있어요."
    >
      {isLoading ? <StatusMessage kind="loading" message="일기를 펼치고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message={error} /> : null}
      {actionError ? <StatusMessage kind="error" message={actionError} /> : null}

      {diary ? (
        <div className={styles.container}>
          <Link to={isOwner ? "/diaries" : "/shared-feed"} className={styles.backLink}>
            {isOwner ? "내 일기 목록으로 가기" : "공개·그룹 피드로 가기"}
          </Link>

          {!isEditing ? (
            <article className={styles.detailCard}>
              <div className={styles.metaRow}>
                <span>{formatDate(diary.createdAt)}</span>
                <span className={styles.scopeBadge}>{getVisibilityScopeLabel(diary.visibilityScope)}</span>
              </div>
              {getEmotionMeta(diary.emotion) ? (
                <p className={styles.emotionRow}>
                  <span className={styles.emotionEmoji} aria-hidden="true">
                    <EmotionIcon emotion={getEmotionMeta(diary.emotion)!.key} />
                  </span>
                  이 날의 기분: {getEmotionMeta(diary.emotion)?.label}
                </p>
              ) : null}
              <p className={styles.content}>{diary.content}</p>
              <div className={styles.reactionRow}>
                <span>하트 {diary.heartCount ?? 0}</span>
                <span>댓글 {diary.commentCount ?? 0}</span>
              </div>
              <div className={styles.actionRow}>
                {isOwner ? <>
                <Button type="button" onClick={() => setIsEditing(true)}>
                  수정하기
                </Button>
                <button type="button" className={styles.deleteButton} disabled={isDeleting} onClick={() => void handleDelete()}>
                  {isDeleting ? '삭제 중...' : '삭제하기'}
                </button>
                </> : null}
              </div>
            </article>
          ) : (
            <form className={styles.form} onSubmit={handleSave}>
              <label className={styles.field}>
                <span>일기 내용</span>
                <textarea
                  value={content}
                  rows={12}
                  disabled={isSaving}
                  onChange={(event) => setContent(event.target.value)}
                />
              </label>
              <label className={styles.field}>
                <span>오늘 기분</span>
                <EmotionSelector value={emotion} onChange={setEmotion} disabled={isSaving} />
              </label>
              <DiaryAudiencePicker scope={visibilityScope} groupIds={sharingGroupIds}
                onScopeChange={setVisibilityScope} onGroupIdsChange={setSharingGroupIds} disabled={isSaving} />
              <div className={styles.actionRow}>
                <HoverHint hint={!isSaving && !content.trim() ? '일기 내용을 한 글자라도 적으면 저장할 수 있어요' : null}>
                  <Button type="submit" disabled={isSaving || !content.trim() || (visibilityScope === 'group' && sharingGroupIds.length === 0)}>
                    {isSaving ? '저장 중...' : '저장하기'}
                  </Button>
                </HoverHint>
                <button type="button" className={styles.cancelButton} disabled={isSaving} onClick={() => { setContent(diary.content); setVisibilityScope(diary.visibilityScope); setSharingGroupIds(diary.sharingGroupIds ?? []); setEmotion(diary.emotion ?? null); setIsEditing(false); }}>
                  취소
                </button>
              </div>
            </form>
          )}

          {comments.length > 0 ? (
            <section className={styles.comments}>
              <h2>댓글</h2>
              <ul>
                {comments.map((comment) => (
                  <li key={`${comment.id}-${comment.createdAt ?? comment.content}`}>
                    <p>
                      <strong>{getActorDisplayName(comment.actorRelationLabel, comment.actorNickname)}</strong>
                      <span>{comment.content}</span>
                    </p>
                    {comment.createdAt ? <small>{formatDate(comment.createdAt)}</small> : null}
                  </li>
                ))}
              </ul>
            </section>
          ) : null}
        </div>
      ) : null}
    </PageSection>
  );
}

export default DiaryDetailPage;
