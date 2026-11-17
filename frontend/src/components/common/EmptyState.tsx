import type { ReactNode } from 'react';
import styles from './EmptyState.module.css';

interface EmptyStateProps {
  title: string;
  description?: string;
  children?: ReactNode;
}

export default function EmptyState({ title, description, children }: EmptyStateProps) {
  return <div className={styles.empty}>
    <h3>{title}</h3>
    {description ? <p>{description}</p> : null}
    {children ? <div className={styles.actions}>{children}</div> : null}
  </div>;
}
