import Button from '../common/Button';
import StatusMessage from '../common/StatusMessage';
import type { SharingGroup } from '../../types/sharing';
import styles from './SharingGroupMembers.module.css';

export default function SharingGroupMembers({ group, busy, onEdit, onDelete }: {
  group: SharingGroup; busy: boolean; onEdit: () => void; onDelete: () => void;
}) {
  return <section aria-label={`${group.name} 구성원`} className={styles.section}>
    <div className={styles.toolbar}>
      <p>구성원 {group.members.length}명</p>
      <div className={styles.actions}>
        <Button variant="secondary" size="sm" disabled={busy} onClick={onEdit}>그룹 수정</Button>
        <Button variant="ghost" size="sm" disabled={busy} onClick={onDelete}>그룹 삭제</Button>
      </div>
    </div>
    {group.members.length === 0 ? <StatusMessage kind="empty" message="아직 구성원이 없어요." /> :
      <div className={styles.members}>
        {group.members.map(member => <article key={member.userId} className={styles.member}>
          <span className={styles.initial} aria-hidden="true">{Array.from(member.nickname.trim())[0] || '?'}</span>
          <h2>{member.nickname}</h2>
          <p>{member.appRole === 'child' ? '아이' : '가족'}</p>
        </article>)}
      </div>}
  </section>;
}
