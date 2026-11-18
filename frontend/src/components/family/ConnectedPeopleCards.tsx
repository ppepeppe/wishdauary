import { Link } from 'react-router-dom';
import ProfileAvatar from '../common/ProfileAvatar';
import type { FamilyConnection } from '../../types/familyConnection';
import styles from './ConnectedPeopleCards.module.css';

export default function ConnectedPeopleCards({ people, isFamily }: { people: FamilyConnection[]; isFamily: boolean }) {
  return <div className={styles.grid} role="region" aria-label={isFamily ? '연결된 아이 카드' : '연결된 가족 카드'}>
    {people.map(person => <article key={person.userId} className={styles.card}>
      <ProfileAvatar preset={person.avatarPreset} character={person.avatarCharacter} imageUrl={person.avatarImage}
        size="md" alt={`${person.nickname} 프로필`} />
      <div className={styles.identity}>
        <h3>{person.nickname}</h3>
        {!isFamily ? <p>{person.relationLabel || '가족'}</p> : null}
      </div>
      {isFamily ? <Link to={`/family/children/${person.userId}`} aria-label={`${person.nickname} 아이 프로필 보기`}>프로필 보기 <span aria-hidden="true">→</span></Link> : null}
    </article>)}
  </div>;
}
