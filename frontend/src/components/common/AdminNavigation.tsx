import { NavLink } from 'react-router-dom';
import styles from './AdminNavigation.module.css';

export default function AdminNavigation() {
  return <nav className={styles.navigation} aria-label="관리자 메뉴">
    <NavLink to="/admin/shop">상점 상품</NavLink>
    <NavLink to="/admin/english">영어 콘텐츠</NavLink>
  </nav>;
}
