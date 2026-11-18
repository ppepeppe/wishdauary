import { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getSelectedChildId } from '../utils/childDiaries';
import { getAuthSession } from '../services/session/authSession';
import { readSelectedChild, saveSelectedChild } from '../services/session/selectedChildSession';

export function useSelectedChild(children: { userId: number }[]) {
  const [params, setParams] = useSearchParams();
  const userId = getAuthSession()?.userId;
  const requested = params.get('child');
  const remembered = userId === undefined ? null : readSelectedChild(userId);
  const childId = getSelectedChildId(children, requested, remembered);

  useEffect(() => {
    // Empty lists during loading must not overwrite the previous selection.
    if (childId === undefined) return;
    if (userId !== undefined) saveSelectedChild(userId, childId);
    if (requested !== String(childId)) {
      const next = new URLSearchParams(params);
      next.set('child', String(childId));
      setParams(next, { replace: true });
    }
  }, [childId, userId, requested, params, setParams]);

  function selectChild(value: string) {
    const selected = Number(value);
    if (!children.some(child => child.userId === selected)) return;
    if (userId !== undefined) saveSelectedChild(userId, selected);
    const next = new URLSearchParams(params);
    next.set('child', String(selected));
    setParams(next, { replace: true });
  }
  return { childId, selectChild };
}
