import { NavLink } from 'react-router-dom';
import styles from '../common/AdminNavigation.module.css';

export default function FamilyShopNavigation() {
  return <nav className={styles.navigation} aria-label="상점 메뉴">
    <NavLink to="/family-shop" end>상점</NavLink>
    <NavLink to="/admin/shop">상품 관리</NavLink>
  </nav>;
}
