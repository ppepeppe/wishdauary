import type { ReactNode } from 'react';
import styles from './PageNavigation.module.css';

export default function PageNavigation({ children }: { children: ReactNode }) {
  return <nav className={styles.navigation} aria-label="관련 메뉴">{children}</nav>;
}
