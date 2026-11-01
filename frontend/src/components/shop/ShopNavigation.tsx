import { NavLink } from 'react-router-dom';
import styles from './ShopNavigation.module.css';

function ShopNavigation() {
  return (
    <nav className={styles.navigation} aria-label="선물 메뉴">
      <NavLink to="/shop" end className={({ isActive }) => isActive ? styles.active : styles.link}>
        내 선물
      </NavLink>
      <NavLink to="/shop/requests" className={({ isActive }) => isActive ? styles.active : styles.link}>
        요청 내역
      </NavLink>
    </nav>
  );
}

export default ShopNavigation;
