import { useEffect, useId, useState } from 'react';
import { fetchSharingGroups } from '../../services/api/sharingApi';
import type { DiaryVisibilityScope } from '../../types/diary';
import type { SharingGroup } from '../../types/sharing';
import { selectDiaryAudience, toggleDiaryAudienceGroup } from '../../utils/diaryAudience';
import styles from './DiaryAudiencePicker.module.css';

interface Props {
  scope: DiaryVisibilityScope;
  groupIds: number[];
  onScopeChange: (scope: DiaryVisibilityScope) => void;
  onGroupIdsChange: (ids: number[]) => void;
  disabled?: boolean;
}

export default function DiaryAudiencePicker({ scope, groupIds, onScopeChange, onGroupIdsChange, disabled }: Props) {
  const scopeName = useId();
  const panelId = useId();
  const [expanded, setExpanded] = useState(false);
  const [groups, setGroups] = useState<SharingGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    fetchSharingGroups().then((items) => { if (active) setGroups(items); })
      .catch((caught: unknown) => { if (active) setError(caught instanceof Error ? caught.message : '그룹을 불러오지 못했어요.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [revision]);

  useEffect(() => {
    const refresh = () => setRevision((value) => value + 1);
    window.addEventListener('focus', refresh);
    return () => window.removeEventListener('focus', refresh);
  }, []);

  const missingIds = groupIds.filter((id) => !groups.some((group) => group.id === id));
  const scopeLabel = { private: '나만 보기', family: '가족 보기', public: '전체 공개',
    group: groupIds.length > 0 ? `그룹 ${groupIds.length}개` : '그룹 선택 필요', friends: '이전 친구 공개' }[scope];

  function chooseScope(next: DiaryVisibilityScope) {
    const audience = selectDiaryAudience(next, groupIds);
    onScopeChange(audience.scope); onGroupIdsChange(audience.groupIds);
  }

  function toggleGroup(id: number, checked: boolean) {
    const audience = toggleDiaryAudienceGroup(scope, groupIds, id, checked);
    onScopeChange(audience.scope); onGroupIdsChange(audience.groupIds);
  }

  return (
    <fieldset className={styles.picker} disabled={disabled}>
      <legend>공개 범위</legend>
      <button type="button" className={styles.summary} aria-expanded={expanded} aria-controls={panelId}
        onClick={() => setExpanded(value => !value)}>
        <span>공개 범위</span>
        <span className={styles.selected}>{scopeLabel}<span className={styles.chevron} aria-hidden="true">⌄</span></span>
      </button>
      {expanded ? <div id={panelId} className={styles.panel}>
        <div className={styles.options} role="radiogroup" aria-label="공개 범위">
          {([
            { value: 'private', label: '나만 보기' },
            { value: 'family', label: '가족 보기' },
            { value: 'public', label: '전체 공개' },
            { value: 'group', label: '그룹 선택' },
            ...(scope === 'friends' ? [{ value: 'friends', label: '이전 친구 공개' }] : []),
          ] as { value: DiaryVisibilityScope; label: string }[]).map(option => (
            <label key={option.value} className={styles.option}>
              <input type="radio" name={scopeName} value={option.value} checked={scope === option.value}
                onChange={() => chooseScope(option.value)} />
              <span>{option.label}</span>
            </label>
          ))}
        </div>
        {scope === 'group' ? <div className={styles.groups}>
          {loading ? <p role="status">그룹을 불러오는 중이에요.</p> : null}
          {error ? <p role="alert">{error} <button type="button" onClick={() => setRevision((value) => value + 1)}>다시 불러오기</button></p> : null}
          {!loading && !error && groups.map((group) => (
            <label key={group.id} className={styles.group}>
              <input type="checkbox" checked={scope === 'group' && groupIds.includes(group.id)} onChange={event => toggleGroup(group.id, event.target.checked)} />
              <span>{group.name}<small>{group.memberIds.length}명</small></span>
            </label>
          ))}
          {scope === 'group' && !loading && !error && missingIds.length > 0 ? (
            <p role="alert">삭제된 그룹이 선택되어 있어요. <button type="button" onClick={() => onGroupIdsChange(groupIds.filter((id) => !missingIds.includes(id)))}>선택에서 지우기</button></p>
          ) : null}
          {!loading && !error && groups.length === 0 ? <p>아직 만든 그룹이 없어요. 먼저 그룹을 만들어 주세요.</p> : null}
          {scope === 'group' && groupIds.length === 0 ? <p className={styles.hint}>저장하려면 그룹을 하나 이상 선택해 주세요.</p> : null}
          <button type="button" disabled={loading} onClick={() => setRevision((value) => value + 1)}>그룹 새로고침</button>
        </div> : null}
      </div> : null}
    </fieldset>
  );
}
