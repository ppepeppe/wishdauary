import { useSearchParams } from 'react-router-dom';
import { getSelectedChildId } from '../utils/childDiaries';

export function useSelectedChild(children: { userId: number }[]) {
  const [params, setParams] = useSearchParams();
  const childId = getSelectedChildId(children, params.get('child'));
  function selectChild(value: string) {
    const next = new URLSearchParams(params); next.set('child', value);
    setParams(next, { replace: true });
  }
  return { childId, selectChild };
}
