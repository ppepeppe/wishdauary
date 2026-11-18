import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import type { DiaryEntry } from '../../types/diary';
import { getEmotionMeta } from '../../constants/emotion';
import { getVisibilityScopeLabel } from '../../constants/diary';
import EmotionIcon from './EmotionIcon';
import NavIcon from '../common/NavIcon';
import styles from './DiaryFeedCard.module.css';

interface DiaryFeedCardProps {
  diaryId: number;
  author: string;
  writtenDate: string;
  visibilityScope: DiaryEntry['visibilityScope'];
  emotion: DiaryEntry['emotion'];
  contentPreview: string;
  children?: ReactNode;
}

export default function DiaryFeedCard({ diaryId, author, writtenDate, visibilityScope, emotion, contentPreview, children }: DiaryFeedCardProps) {
  const feeling = getEmotionMeta(emotion);
  const date = new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' }).format(new Date(writtenDate));
  return <article className={styles.card}>
    <div className={styles.header}>
      <div className={styles.headerMain}>
        <span className={styles.actor}>{author}</span>
        <div className={styles.meta}><time dateTime={writtenDate}>{date}</time><span>{getVisibilityScopeLabel(visibilityScope)}</span></div>
      </div>
      {feeling ? <span className={styles.emotionBadge}><span aria-hidden="true"><EmotionIcon emotion={feeling.key} /></span>{feeling.label}</span> : null}
    </div>
    <Link to={`/diaries/${diaryId}`} className={styles.previewLink} aria-label={`${author}의 일기 읽기`}>
      <p className={styles.preview}>{contentPreview}</p>
      <span className={styles.readLink}>일기 읽기 <NavIcon name="arrowRight" /></span>
    </Link>
    {children}
  </article>;
}
