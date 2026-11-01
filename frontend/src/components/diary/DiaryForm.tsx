import DiaryAudiencePicker from '../sharing/DiaryAudiencePicker';
import { useState } from 'react';
import { DIARY_PLACEHOLDER } from '../../constants/diary';
import { getTodayDateInKorea } from '../../constants/date';
import type { CreateDiaryRequest, DiaryEmotion, DiaryVisibilityScope } from '../../types/diary';
import Button from '../common/Button';
import HoverHint from '../common/HoverHint';
import EmotionSelector from './EmotionSelector';
import styles from './DiaryForm.module.css';

interface DiaryFormProps {
  onSubmit: (payload: CreateDiaryRequest) => Promise<void>;
  isSubmitting: boolean;
  initialEmotion?: DiaryEmotion | null;
  writtenDate: string;
  onWrittenDateChange: (date: string) => void;
}

function DiaryForm({
  onSubmit,
  isSubmitting,
  initialEmotion = null,
  writtenDate,
  onWrittenDateChange,
}: DiaryFormProps) {
  const [content, setContent] = useState('');
  const [visibilityScope, setVisibilityScope] = useState<DiaryVisibilityScope>('private');
  const [sharingGroupIds, setSharingGroupIds] = useState<number[]>([]);
  const [emotion, setEmotion] = useState<DiaryEmotion | null>(initialEmotion);
  const trimmedContent = content.trim();

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!trimmedContent || (visibilityScope === 'group' && sharingGroupIds.length === 0)) {
      return;
    }

    await onSubmit({
      content: trimmedContent,
      visibilityScope,
      sharingGroupIds,
      emotion,
      writtenDate,
    });
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <label className={styles.field}>
        <span>기록 날짜</span>
        <input
          className={styles.dateInput}
          type="date"
          value={writtenDate}
          max={getTodayDateInKorea()}
          required
          disabled={isSubmitting}
          onChange={(event) => onWrittenDateChange(event.target.value)}
        />
      </label>

      <label className={styles.field}>
        <span>이날의 이야기</span>
        <textarea
          className={styles.textarea}
          rows={10}
          value={content}
          placeholder={DIARY_PLACEHOLDER}
          disabled={isSubmitting}
          onChange={(event) => setContent(event.target.value)}
        />
      </label>

      <label className={styles.field}>
        <span>오늘 기분</span>
        <EmotionSelector value={emotion} onChange={setEmotion} disabled={isSubmitting} />
      </label>

      <DiaryAudiencePicker scope={visibilityScope} groupIds={sharingGroupIds}
        onScopeChange={setVisibilityScope} onGroupIdsChange={setSharingGroupIds} disabled={isSubmitting} />

      <HoverHint hint={!isSubmitting && !trimmedContent ? '오늘의 이야기를 한 글자라도 적으면 저장할 수 있어요' : null}>
        <Button type="submit" fullWidth disabled={isSubmitting || !trimmedContent || (visibilityScope === 'group' && sharingGroupIds.length === 0)}>
          {isSubmitting ? '저장 중...' : '저장하기'}
        </Button>
      </HoverHint>
    </form>
  );
}

export default DiaryForm;
