import CardCarousel from '../common/CardCarousel';
import type { ReactionFeedItem } from '../../types/diary';
import { getActorActionDisplayName } from '../../utils/actorDisplay';
import styles from './ReactionFeedList.module.css';

interface ReactionFeedListProps {
  items: ReactionFeedItem[];
}

function formatDate(value?: string | null) {
  if (!value) {
    return '';
  }

  return new Intl.DateTimeFormat('ko-KR', {
    month: 'long',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value));
}

function getCommentPreview(content?: string | null) {
  if (!content) {
    return '댓글을 남겼어요';
  }

  const trimmed = content.trim();

  if (!trimmed) {
    return '댓글을 남겼어요';
  }

  const preview = trimmed.length > 28 ? `${trimmed.slice(0, 28)}...` : trimmed;

  return `댓글: ${preview}`;
}

function getReactionMessage(item: ReactionFeedItem) {
  const actorDisplayName = getActorActionDisplayName(item.actorRelationLabel, item.actorNickname);

  if (item.type === 'comment') {
    return `💬 ${actorDisplayName} ${getCommentPreview(item.content)}`;
  }

  return `❤️ ${actorDisplayName} 하트를 남겼어요`;
}

function ReactionFeedList({ items }: ReactionFeedListProps) {
  return (
    <CardCarousel title="받은 소식" itemLabel="소식">
      {items.map((item) => (
        <article key={item.id} className={styles.card}>
          <p className={styles.message}>{getReactionMessage(item)}</p>
          {formatDate(item.createdAt) ? <span className={styles.date}>{formatDate(item.createdAt)}</span> : null}
        </article>
      ))}
    </CardCarousel>
  );
}

export default ReactionFeedList;
