import { Link, useLocation } from 'react-router-dom';
import styles from './FamilyShopNavigation.module.css';

export default function FamilyShopNavigation() {
  const location = useLocation();
  const category = new URLSearchParams(location.search).get('category');
  const selected = location.pathname === '/admin/shop' ? 'manage' : category === 'custom' || category === 'family' ? category : 'catalog';
  const options = [
    { value: 'catalog', label: '공통 상점', to: '/family-shop' },
    { value: 'custom', label: '아이의 신청', to: '/family-shop?category=custom' },
    { value: 'family', label: '가족의 제안', to: '/family-shop?category=family' },
    { value: 'manage', label: '상품 관리', to: '/admin/shop' },
  ];
  return <nav className={styles.navigation} aria-label="상점 메뉴">
    {options.map(option => <Link key={option.value} to={option.to}
      aria-current={selected === option.value ? 'page' : undefined}>{option.label}</Link>)}
  </nav>;
}
