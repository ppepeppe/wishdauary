import { Link } from 'react-router-dom';
import ProfileAvatar from '../common/ProfileAvatar';
import type { FamilyConnection } from '../../types/familyConnection';
import styles from './ConnectedPeopleCards.module.css';

export default function ConnectedPeopleCards({ people, isFamily }: { people: FamilyConnection[]; isFamily: boolean }) {
  return <div className={styles.grid} role="region" aria-label={isFamily ? '연결된 아이 카드' : '연결된 가족 카드'}>
    {people.map(person => <article key={person.userId} className={styles.card}>
      <ProfileAvatar preset={person.avatarPreset} character={person.avatarCharacter} imageUrl={person.avatarImage}
        size="md" alt={`${person.nickname} 프로필`} />
      <h3>{person.nickname}</h3>
      <p>{isFamily ? '연결된 아이' : person.relationLabel || '연결된 가족'}</p>
      {isFamily ? <Link to={`/family-feed?child=${person.userId}`}>아이 일기 보기 →</Link> : null}
    </article>)}
  </div>;
}
