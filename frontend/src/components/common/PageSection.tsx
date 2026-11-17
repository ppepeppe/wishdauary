import type { ReactNode } from 'react';
import styles from './PageSection.module.css';

interface PageSectionProps {
  title: string;
  description?: string;
  variant?: 'default' | 'form';
  actions?: ReactNode;
  children: ReactNode;
}

function PageSection({ title, description, variant = 'default', actions, children }: PageSectionProps) {
  return (
    <section className={`${styles.section} ${variant === 'form' ? styles.form : ''}`}>
      <div className={styles.heading}>
        <div className={styles.headingCopy}>
          <h1>{title}</h1>
          {description ? <p>{description}</p> : null}
        </div>
        {actions}
      </div>
      {children}
    </section>
  );
}

export default PageSection;
