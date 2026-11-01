import type { DiaryEmotion } from '../../types/diary';
import styles from './EmotionIcon.module.css';

interface EmotionIconProps {
  emotion: DiaryEmotion;
  className?: string;
}

function EmotionIcon({ emotion, className }: EmotionIconProps) {
  const classNames = [styles.icon, styles[emotion], className ?? ''].filter(Boolean).join(' ');

  return <span className={classNames} aria-hidden="true" />;
}

export default EmotionIcon;
