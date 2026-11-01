import { useState } from 'react';
import NavIcon from '../common/NavIcon';
import styles from './ProductArtwork.module.css';

interface ProductArtworkProps {
  title: string;
  imageUrl?: string | null;
  className?: string;
}

function ProductArtwork({ title, imageUrl, className }: ProductArtworkProps) {
  const [hasImageError, setHasImageError] = useState(false);
  const source = hasImageError ? null : imageUrl?.trim();
  const classNames = [styles.artwork, className ?? ''].filter(Boolean).join(' ');

  return (
    <div className={classNames}>
      {source ? (
        <img src={source} alt={`${title} 상품 이미지`} onError={() => setHasImageError(true)} />
      ) : (
        <span className={styles.placeholder} role="img" aria-label={`${title} 상품 이미지 없음`}>
          <span className={styles.emptyIcon} aria-hidden="true"><NavIcon name="gift" /></span>
          <span>사진 준비 중</span>
        </span>
      )}
    </div>
  );
}

export default ProductArtwork;
