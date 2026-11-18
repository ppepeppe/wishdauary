import { NavLink } from 'react-router-dom';
import styles from './FamilyDiaryNavigation.module.css';

export default function FamilyDiaryNavigation() {
  return <nav className={styles.navigation} aria-label="아이 일기 메뉴">
    <NavLink to="/family-feed" end>아이 일기</NavLink>
    <NavLink to="/shared-feed">공개·그룹 일기</NavLink>
  </nav>;
}
