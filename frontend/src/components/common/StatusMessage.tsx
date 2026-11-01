import styles from './StatusMessage.module.css';

interface StatusMessageProps {
  kind: 'loading' | 'error' | 'empty' | 'success';
  message: string;
}

function StatusMessage({ kind, message }: StatusMessageProps) {
  return <p className={`${styles.message} ${styles[kind]}`}>{message}</p>;
}

export default StatusMessage;
