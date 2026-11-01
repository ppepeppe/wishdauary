import { isAvatarPreset } from '../../constants/avatar';
import styles from './ProfileAvatar.module.css';

interface ProfileAvatarProps {
  preset?: string | null;
  imageUrl?: string | null;
  size?: 'sm' | 'md' | 'lg';
  alt?: string;
}

function ProfileAvatar({ preset, imageUrl, size = 'md', alt = '' }: ProfileAvatarProps) {
  const presetKey = isAvatarPreset(preset) ? preset : 'peach';

  if (imageUrl) {
    return (
      <span className={`${styles.frame} ${styles[size]}`}>
        <img src={imageUrl} alt={alt} className={styles.photo} />
      </span>
    );
  }

  return (
    <span className={`${styles.frame} ${styles[size]} ${styles[presetKey]}`} aria-hidden={alt ? undefined : true}>
      <span className={styles.face} />
    </span>
  );
}

export default ProfileAvatar;
