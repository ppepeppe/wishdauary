import { NavLink } from 'react-router-dom';
import styles from '../common/AdminNavigation.module.css';

export default function DiaryNavigation() {
  return <nav className={styles.navigation} aria-label="일기 메뉴">
    <NavLink to="/diaries" end>내 일기</NavLink>
    <NavLink to="/shared-feed">그룹 일기</NavLink>
  </nav>;
}
