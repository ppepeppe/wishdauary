import type { useSignupAvailability } from '../../hooks/useSignupAvailability';
import styles from './SignupForm.module.css';

export default function SignupAvailabilityMessage({ check }: { check: ReturnType<typeof useSignupAvailability> }) {
  return <div className={styles.availability}>
    <p id={check.id} role="status" className={check.status === 'available' ? styles.available : styles.helpText}>{check.message}</p>
    {check.status === 'error' ? <button type="button" onClick={check.retry}>다시 확인</button> : null}
  </div>;
}
