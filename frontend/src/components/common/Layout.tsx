import { useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import { getAuthSession } from '../../services/session/authSession';
import { fetchProductPricingRequests, fetchReceivedGiftRequests, fetchSentGiftRequests } from '../../services/api/shopApi';
import NavIcon, { type NavIconName } from './NavIcon';
import { GIFT_REQUESTS_CHANGED_EVENT } from '../../constants/events';
import styles from './Layout.module.css';

const childNavItems: { to: string; label: string; icon: NavIconName; end: boolean; isPrimary?: boolean }[] = [
  { to: '/home', label: '홈', icon: 'home', end: true },
  { to: '/diaries', label: '일기', icon: 'diary', end: true },
  { to: '/diaries/write', label: '쓰기', icon: 'write', end: true, isPrimary: true },
  { to: '/shop', label: '선물', icon: 'gift', end: false },
  { to: '/feed', label: '소식', icon: 'feed', end: true },
];

const familyNavItems: typeof childNavItems = [
  { to: '/family-home', label: '홈', icon: 'home', end: true },
  { to: '/family-feed', label: '아이 일기', icon: 'diary', end: true },
  { to: '/family-shop', label: '상점 관리', icon: 'store', end: true },
  { to: '/family-settings', label: '선물 요청', icon: 'gift', end: true },
  { to: '/me', label: '마이', icon: 'profile', end: true },
];

function Layout() {
  const location = useLocation();
  const authSession = getAuthSession();
  const navItems = authSession?.appRole === 'family' ? familyNavItems : childNavItems;
  const isPublicPage = !authSession;
  const isLandingPage = isPublicPage && location.pathname === '/';
  const [giftBadgeCount, setGiftBadgeCount] = useState(0);

  useEffect(() => {
    let ignore = false;
    let version = 0;

    async function loadGiftBadgeCount() {
      const current = ++version;
      if (!authSession) {
        setGiftBadgeCount(0);
        return;
      }

      try {
        if (authSession.appRole === 'family') {
          const [requests, pricing] = await Promise.all([fetchReceivedGiftRequests(), fetchProductPricingRequests()]);
          if (!ignore && current === version) {
            setGiftBadgeCount(requests.filter((request) => request.status === 'pending').length + pricing.length);
          }
          return;
        }

        const requests = await fetchSentGiftRequests();
        if (!ignore && current === version) {
          setGiftBadgeCount(requests.filter((request) => request.status === 'approved' || request.status === 'rejected').length);
        }
      } catch {
        if (!ignore && current === version) {
          setGiftBadgeCount(0);
        }
      }
    }

    void loadGiftBadgeCount();
    const refresh = () => void loadGiftBadgeCount();
    window.addEventListener('focus', refresh);
    window.addEventListener(GIFT_REQUESTS_CHANGED_EVENT, refresh);

    return () => {
      ignore = true;
      window.removeEventListener('focus', refresh);
      window.removeEventListener(GIFT_REQUESTS_CHANGED_EVENT, refresh);
    };
  }, [authSession?.appRole, authSession?.userId, location.pathname]);

  if (authSession) {
    return (
      <div className={styles.mobileShell}>
        <div className={styles.mobileFrame}>
          <main className={styles.mobileContent}>
            <Outlet />
          </main>
          <nav className={styles.bottomNav} aria-label="주요 메뉴">
            <Link to={authSession.appRole === 'family' ? '/family-home' : '/home'} className={styles.desktopBrand}>
              <span className={styles.desktopMark}>W</span>
              <span>wish 다유어리<small>{authSession.appRole === 'family' ? '함께 읽는 아이의 하루' : '나의 하루를 차곡차곡'}</small></span>
            </Link>
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  [
                    (isActive || (item.to === '/family-shop' && location.pathname === '/admin/shop')) ? styles.bottomNavItemActive : styles.bottomNavItem,
                    item.isPrimary ? styles.bottomNavPrimary : '',
                  ]
                    .filter(Boolean)
                    .join(' ')
                }
              >
                <span className={styles.bottomNavIcon} aria-hidden="true">
                  <NavIcon name={item.icon} />
                  {(item.to === '/shop' || item.to === '/family-settings') && giftBadgeCount > 0 ? (
                    <span className={styles.bottomNavBadge}>{giftBadgeCount}</span>
                  ) : null}
                </span>
                <span className={styles.bottomNavLabel}>{item.label}</span>
              </NavLink>
            ))}
            <Link to="/me" className={styles.desktopAccount} aria-label="내 계정">
              <NavIcon name="profile" />
              <span>{authSession.nickname || '내 계정'}<small>프로필과 가족 관리</small></span>
              <span aria-hidden="true">›</span>
            </Link>
          </nav>
        </div>
      </div>
    );
  }

  return (
    <div className={isLandingPage ? styles.landingShell : styles.appShell}>
      <header className={isLandingPage ? styles.landingHeader : styles.header}>
        <div className={styles.topRow}>
          <Link to="/" className={isLandingPage ? styles.landingBrand : styles.brand}>
            <span className={isLandingPage ? styles.landingBrandMark : styles.brandMark} aria-hidden="true">W</span>
            <span className={styles.brandCopy}>
              <span className={isLandingPage ? styles.landingWordmark : styles.eyebrow}>wish 다유어리</span>
              <span className={isLandingPage ? styles.landingTagline : styles.title}>마음을 잇는 가족 다유어리</span>
            </span>
          </Link>
          <div className={styles.publicActions}>
            {location.pathname !== '/login' ? <Link to="/login" className={styles.publicLink}>로그인</Link> : null}
            {location.pathname !== '/signup' ? (
              <Link to="/signup" className={isLandingPage ? styles.landingSignup : styles.publicLink}>회원가입</Link>
            ) : null}
          </div>
        </div>
      </header>
      <main className={isLandingPage ? styles.landingContent : styles.content}><Outlet /></main>
    </div>
  );
}

export default Layout;
