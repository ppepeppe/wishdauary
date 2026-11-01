import type { ReactNode } from 'react';
import styles from './PageSection.module.css';

interface PageSectionProps {
  title: string;
  description?: string;
  children: ReactNode;
}

function PageSection({ title, description, children }: PageSectionProps) {
  return (
    <section className={styles.section}>
      <div className={styles.heading}>
        <h2>{title}</h2>
        {description ? <p>{description}</p> : null}
      </div>
      {children}
    </section>
  );
}

export default PageSection;
