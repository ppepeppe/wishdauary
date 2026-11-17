import { EMOTIONS } from '../../constants/emotion';
import type { DiaryEmotion } from '../../types/diary';
import EmotionIcon from './EmotionIcon';
import { useId } from 'react';
import styles from './EmotionSelector.module.css';

interface EmotionSelectorProps {
  value: DiaryEmotion | null;
  onChange: (emotion: DiaryEmotion) => void;
  disabled?: boolean;
}

function EmotionSelector({ value, onChange, disabled = false }: EmotionSelectorProps) {
  const name = useId();
  return (
    <div className={styles.group} role="radiogroup" aria-label="오늘 기분">
      {EMOTIONS.map((emotion) => {
        const isSelected = value === emotion.key;
        const className = [styles.option, isSelected ? styles.optionSelected : ''].filter(Boolean).join(' ');

        return (
          <label
            key={emotion.key}
            className={className}
          >
            <input type="radio" name={name} value={emotion.key} checked={isSelected}
              disabled={disabled} onChange={() => onChange(emotion.key)} />
            <span className={styles.emoji} aria-hidden="true">
              <EmotionIcon emotion={emotion.key} />
            </span>
            <span className={styles.label}>{emotion.label}</span>
          </label>
        );
      })}
    </div>
  );
}

export default EmotionSelector;
