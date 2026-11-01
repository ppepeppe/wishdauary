import { EMOTIONS } from '../../constants/emotion';
import type { DiaryEmotion } from '../../types/diary';
import EmotionIcon from './EmotionIcon';
import styles from './EmotionSelector.module.css';

interface EmotionSelectorProps {
  value: DiaryEmotion | null;
  onChange: (emotion: DiaryEmotion) => void;
  disabled?: boolean;
}

function EmotionSelector({ value, onChange, disabled = false }: EmotionSelectorProps) {
  return (
    <div className={styles.group} role="radiogroup" aria-label="오늘 감정">
      {EMOTIONS.map((emotion) => {
        const isSelected = value === emotion.key;
        const className = [styles.option, isSelected ? styles.optionSelected : ''].filter(Boolean).join(' ');

        return (
          <button
            key={emotion.key}
            type="button"
            role="radio"
            aria-checked={isSelected}
            disabled={disabled}
            className={className}
            onClick={() => onChange(emotion.key)}
          >
            <span className={styles.emoji}>
              <EmotionIcon emotion={emotion.key} />
            </span>
            <span className={styles.label}>{emotion.label}</span>
          </button>
        );
      })}
    </div>
  );
}

export default EmotionSelector;
