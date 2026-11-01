export function getActorDisplayName(actorRelationLabel?: string | null, actorNickname?: string | null) {
  const relationLabel = actorRelationLabel?.trim();

  if (relationLabel) {
    return relationLabel;
  }

  const nickname = actorNickname?.trim();

  if (nickname) {
    return nickname;
  }

  return '누군가';
}

export function getActorActionDisplayName(actorRelationLabel?: string | null, actorNickname?: string | null) {
  const relationLabel = actorRelationLabel?.trim();

  if (relationLabel) {
    return `${relationLabel}가`;
  }

  const nickname = actorNickname?.trim();

  if (nickname) {
    return `${nickname}님`;
  }

  return '누군가가';
}
