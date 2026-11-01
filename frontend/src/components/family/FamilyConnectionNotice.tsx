import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAsyncData } from '../../hooks/useAsyncData';
import { fetchFamilyConnectionRequests } from '../../services/api/familyConnectionApi';
import styles from './FamilyConnectionNotice.module.css';

export default function FamilyConnectionNotice() {
  const { data, refetch } = useAsyncData(fetchFamilyConnectionRequests);
  useEffect(() => {
    const refresh = () => void refetch();
    window.addEventListener('focus', refresh);
    return () => window.removeEventListener('focus', refresh);
  }, [refetch]);
  const count = data?.filter((item) => item.status === 'pending').length ?? 0;
  return count > 0 ? <Link to="/family-connections" className={styles.notice}>가족 연결 요청이 {count}개 있어요 <span>확인하기 →</span></Link> : null;
}
