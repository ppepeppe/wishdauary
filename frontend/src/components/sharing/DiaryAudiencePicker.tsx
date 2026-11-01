import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Capacitor } from '@capacitor/core';
import { fetchSharingGroups } from '../../services/api/sharingApi';
import type { DiaryVisibilityScope } from '../../types/diary';
import type { SharingGroup } from '../../types/sharing';
import styles from './DiaryAudiencePicker.module.css';

interface Props {
  scope: DiaryVisibilityScope;
  groupIds: number[];
  onScopeChange: (scope: DiaryVisibilityScope) => void;
  onGroupIdsChange: (ids: number[]) => void;
  disabled?: boolean;
}

export default function DiaryAudiencePicker({ scope, groupIds, onScopeChange, onGroupIdsChange, disabled }: Props) {
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

  return (
    <fieldset className={styles.picker} disabled={disabled}>
      <legend>누구에게 보여줄까요?</legend>
      <label className={styles.scope}>
        <span className={styles.label}>공개 범위</span>
        <select value={scope} onChange={(event) => onScopeChange(event.target.value as DiaryVisibilityScope)}>
          <option value="private">나만 보기</option>
          <option value="public">전체공개</option>
          <option value="group">그룹 선택</option>
          <option value="family">연결된 가족 공개</option>
          {scope === 'friends' ? <option value="friends">이전 친구 공개 설정</option> : null}
        </select>
      </label>
      {scope === 'public' ? <p className={styles.hint}>로그인한 서비스 사용자 모두가 이 일기를 볼 수 있어요.</p> : null}
      {scope === 'family' ? <p className={styles.hint}>내 그룹에 추가한 가족 계정 모두에게 보여요.</p> : null}
      {scope === 'group' ? (
        <div className={styles.groups}>
          <p className={styles.hint}>여러 그룹을 선택할 수 있어요. 선택한 그룹 중 하나에 속하면 볼 수 있어요.</p>
          {loading ? <p role="status">그룹을 불러오는 중이에요.</p> : null}
          {error ? <p role="alert">{error} <button type="button" onClick={() => setRevision((value) => value + 1)}>다시 불러오기</button></p> : null}
          {!loading && !error && groups.map((group) => (
            <label key={group.id} className={styles.group}>
              <input type="checkbox" checked={groupIds.includes(group.id)} onChange={(event) => onGroupIdsChange(
                event.target.checked ? [...groupIds, group.id] : groupIds.filter((id) => id !== group.id),
              )} />
              <span>{group.name}<small>{group.memberIds.length}명</small></span>
            </label>
          ))}
          {!loading && !error && missingIds.length > 0 ? (
            <p role="alert">삭제된 그룹이 선택되어 있어요. <button type="button" onClick={() => onGroupIdsChange(groupIds.filter((id) => !missingIds.includes(id)))}>선택에서 지우기</button></p>
          ) : null}
          {!loading && !error && groups.length === 0 ? <p>아직 만든 그룹이 없어요. 먼저 그룹을 만들어 주세요.</p> : null}
          {groupIds.length === 0 ? <p className={styles.hint}>저장하려면 그룹을 하나 이상 선택해 주세요.</p> : null}
          <button type="button" disabled={loading} onClick={() => setRevision((value) => value + 1)}>그룹 새로고침</button>
          <Link to="/sharing-groups" target={Capacitor.isNativePlatform() ? undefined : '_blank'} rel="noopener noreferrer">그룹 관리 ↗</Link>
        </div>
      ) : null}
    </fieldset>
  );
}
