import { useId, type KeyboardEvent, type ReactNode } from 'react';
import styles from './Tabs.module.css';

interface TabsProps {
  label: string;
  options: { value: string; label: string; count?: number }[];
  value: string;
  onChange: (value: string) => void;
  children: ReactNode;
}

export default function Tabs({ label, options, value, onChange, children }: TabsProps) {
  const id = useId();
  function handleKey(event: KeyboardEvent<HTMLDivElement>) {
    if ((event.target as HTMLElement).closest('[role="tablist"]') !== event.currentTarget) return;
    const index = options.findIndex(option => option.value === value);
    const targets: Record<string, number> = { ArrowRight: (index + 1) % options.length,
      ArrowLeft: (index + options.length - 1) % options.length, Home: 0, End: options.length - 1 };
    const next = targets[event.key];
    if (next === undefined) return;
    event.preventDefault(); onChange(options[next].value);
    event.currentTarget.querySelectorAll<HTMLButtonElement>('[role="tab"]')[next]?.focus();
  }
  return <div className={styles.root}>
    <div role="tablist" aria-label={label} className={styles.tabs} onKeyDown={handleKey}>
      {options.map(option => <button key={option.value} id={`${id}-${option.value}`} type="button" role="tab"
        aria-selected={value === option.value} aria-controls={`${id}-panel`} tabIndex={value === option.value ? 0 : -1}
        onClick={() => onChange(option.value)}>{option.label}{option.count !== undefined ? <span className={styles.count} aria-label={`${option.count}개 대기`}>{option.count}</span> : null}</button>)}
    </div>
    <div id={`${id}-panel`} role="tabpanel" aria-labelledby={`${id}-${value}`} className={styles.panel}>{children}</div>
  </div>;
}
