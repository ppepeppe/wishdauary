import { getAvatarCharacter, isAvatarPreset } from '../../constants/avatar';
import styles from './ProfileAvatar.module.css';

interface ProfileAvatarProps {
  preset?: string | null;
  character?: string | null;
  imageUrl?: string | null;
  size?: 'sm' | 'md' | 'lg';
  alt?: string;
}

function ProfileAvatar({ preset, character, imageUrl, size = 'md', alt = '' }: ProfileAvatarProps) {
  const presetKey = isAvatarPreset(preset) ? preset : 'peach';

  if (imageUrl) {
    return (
      <span className={`${styles.frame} ${styles[size]}`}>
        <img src={imageUrl} alt={alt} className={styles.photo} />
      </span>
    );
  }

  return (
    <span className={`${styles.frame} ${styles[size]} ${styles[presetKey]}`} role={alt ? 'img' : undefined} aria-label={alt || undefined} aria-hidden={alt ? undefined : true}>
      <img className={styles.character} src={getAvatarCharacter(character).imageUrl} alt="" />
    </span>
  );
}

export default ProfileAvatar;
