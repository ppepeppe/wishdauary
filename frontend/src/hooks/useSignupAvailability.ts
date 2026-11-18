import { useEffect, useId, useState } from 'react';
import { checkSignupAvailability } from '../services/api/authApi';

type Status = 'idle' | 'checking' | 'available' | 'unavailable' | 'error';
interface Check { value: string; status: Status; message: string }

export function useSignupAvailability(field: 'LOGIN_ID' | 'NICKNAME', value: string) {
  const cleaned = value.trim();
  const id = useId();
  const [revision, setRevision] = useState(0);
  const [result, setResult] = useState<Check>({ value: '', status: 'idle', message: '' });
  useEffect(() => {
    if (!cleaned) return;
    let active = true;
    const controller = new AbortController();
    setResult({ value: cleaned, status: 'checking', message: '중복 여부를 확인하고 있어요.' });
    const timer = setTimeout(() => {
      checkSignupAvailability(field, cleaned, controller.signal).then(data => {
        if (active) setResult({ value: cleaned, status: data.available ? 'available' : 'unavailable', message: data.message });
      }).catch(() => {
        if (active) setResult({ value: cleaned, status: 'error', message: '중복 여부를 확인하지 못했어요. 다시 확인해 주세요.' });
      });
    }, 350);
    return () => { active = false; clearTimeout(timer); controller.abort(); };
  }, [field, cleaned, revision]);
  const current: Check = !cleaned ? { value: '', status: 'idle', message: '' }
    : result.value === cleaned ? result : { value: cleaned, status: 'checking', message: '중복 여부를 확인하고 있어요.' };
  return { ...current, id, retry: () => { setResult({ value: cleaned, status: 'checking', message: '중복 여부를 확인하고 있어요.' }); setRevision(value => value + 1); } };
}
