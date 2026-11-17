import { useId } from 'react';
import PhotoPicker from '../common/PhotoPicker';
import styles from './DiaryContentField.module.css';

interface DiaryContentFieldProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  imageData: string | null;
  onPhotoChange: (photo: string | null) => void;
  onPhotoBusyChange: (busy: boolean) => void;
  disabled?: boolean;
  rows?: number;
  placeholder?: string;
}

export default function DiaryContentField({ label, value, onChange, imageData, onPhotoChange,
  onPhotoBusyChange, disabled = false, rows = 7, placeholder }: DiaryContentFieldProps) {
  const id = useId();

  return <div className={styles.field}>
    <label htmlFor={id}>{label}</label>
    <div className={styles.editor}>
      <textarea id={id} value={value} rows={rows} placeholder={placeholder}
        disabled={disabled} onChange={(event) => onChange(event.target.value)} />
      <div className={styles.toolbar}>
        <PhotoPicker label="일기 사진" variant="compact" value={imageData}
          onChange={onPhotoChange} onBusyChange={onPhotoBusyChange} disabled={disabled} />
      </div>
    </div>
  </div>;
}
