import { useEffect, useRef, useState } from 'react';
import type { DiaryComment, DiaryEntry } from '../types/diary';
import { createDiaryComment, fetchDiaryComments, fetchFamilyFeed, toggleDiaryHeart } from '../services/api/diaryApi';
import { useAsyncData } from './useAsyncData';

const DEFAULT_TOGGLE_HEART_ERROR_MESSAGE = '하트 반응을 남기지 못했어요. 잠시 후 다시 시도해 주세요.';
const DEFAULT_COMMENT_ERROR_MESSAGE = '댓글을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.';
const DEFAULT_COMMENT_CREATE_ERROR_MESSAGE = '댓글을 남기지 못했어요. 잠시 후 다시 시도해 주세요.';

function updateDiaryHeartState(
  diaries: DiaryEntry[],
  diaryId: number,
  hearted: boolean,
  heartCount: number,
) {
  return diaries.map((diary) =>
    diary.id === diaryId
      ? {
          ...diary,
          hearted,
          heartCount,
        }
      : diary,
  );
}

export function useFamilyFeed() {
  const { data, isLoading, error, refetch } = useAsyncData(fetchFamilyFeed, true);
  const commentRequests = useRef(new Set<number>());
  const [diaries, setDiaries] = useState<DiaryEntry[]>([]);
  const [pendingIds, setPendingIds] = useState<number[]>([]);
  const [actionError, setActionError] = useState<string | null>(null);
  const [commentsByDiary, setCommentsByDiary] = useState<Record<number, DiaryComment[]>>({});
  const [commentLoadingIds, setCommentLoadingIds] = useState<number[]>([]);
  const [commentSubmittingIds, setCommentSubmittingIds] = useState<number[]>([]);
  const [commentErrors, setCommentErrors] = useState<Record<number, string | null>>({});

  useEffect(() => {
    setDiaries(data ?? []);
  }, [data]);

  function isHeartPending(diaryId: number) {
    return pendingIds.includes(diaryId);
  }

  function isCommentLoading(diaryId: number) {
    return commentLoadingIds.includes(diaryId);
  }

  function isCommentSubmitting(diaryId: number) {
    return commentSubmittingIds.includes(diaryId);
  }

  function getComments(diaryId: number) {
    return commentsByDiary[diaryId] ?? [];
  }

  function getCommentError(diaryId: number) {
    return commentErrors[diaryId] ?? null;
  }

  async function loadComments(diaryId: number) {
    if (isCommentLoading(diaryId)) {
      return;
    }

    setCommentErrors((current) => ({
      ...current,
      [diaryId]: null,
    }));
    setCommentLoadingIds((current) => [...current, diaryId]);

    try {
      const comments = await fetchDiaryComments(diaryId);

      setCommentsByDiary((current) => ({
        ...current,
        [diaryId]: comments,
      }));
    } catch {
      setCommentErrors((current) => ({
        ...current,
        [diaryId]: DEFAULT_COMMENT_ERROR_MESSAGE,
      }));
    } finally {
      setCommentLoadingIds((current) => current.filter((id) => id !== diaryId));
    }
  }

  async function submitComment(diaryId: number, content: string) {
    if (commentRequests.current.has(diaryId)) {
      return false;
    }
    commentRequests.current.add(diaryId);

    setCommentErrors((current) => ({
      ...current,
      [diaryId]: null,
    }));
    setCommentSubmittingIds((current) => [...current, diaryId]);

    try {
      const comment = await createDiaryComment({
        diaryId,
        content,
      });
      setCommentsByDiary((current) => ({
        ...current,
        [diaryId]: [...(current[diaryId] ?? []), comment],
      }));
      setDiaries((current) => current.map((diary) => diary.id === diaryId
        ? { ...diary, commentCount: (diary.commentCount ?? 0) + 1 } : diary));
      return true;
    } catch {
      setCommentErrors((current) => ({
        ...current,
        [diaryId]: DEFAULT_COMMENT_CREATE_ERROR_MESSAGE,
      }));
      return false;
    } finally {
      commentRequests.current.delete(diaryId);
      setCommentSubmittingIds((current) => current.filter((id) => id !== diaryId));
    }
  }

  async function toggleHeart(diaryId: number) {
    const targetDiary = diaries.find((diary) => diary.id === diaryId);

    if (!targetDiary || isHeartPending(diaryId)) {
      return;
    }

    setActionError(null);

    const previousHearted = targetDiary.hearted ?? false;
    const previousHeartCount = targetDiary.heartCount ?? 0;
    const nextHearted = !previousHearted;
    const nextHeartCount = Math.max(0, previousHeartCount + (nextHearted ? 1 : -1));

    setPendingIds((current) => [...current, diaryId]);
    setDiaries((current) => updateDiaryHeartState(current, diaryId, nextHearted, nextHeartCount));

    try {
      const result = await toggleDiaryHeart(diaryId);

      setDiaries((current) => updateDiaryHeartState(current, diaryId, result.hearted, result.heartCount));
    } catch {
      setDiaries((current) => updateDiaryHeartState(current, diaryId, previousHearted, previousHeartCount));
      setActionError(DEFAULT_TOGGLE_HEART_ERROR_MESSAGE);
    } finally {
      setPendingIds((current) => current.filter((id) => id !== diaryId));
    }
  }

  return {
    data: diaries,
    isLoading,
    error,
    actionError,
    refetch,
    toggleHeart,
    isHeartPending,
    loadComments,
    submitComment,
    getComments,
    getCommentError,
    isCommentLoading,
    isCommentSubmitting,
  };
}
