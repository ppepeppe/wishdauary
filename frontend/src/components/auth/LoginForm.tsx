import { useState } from 'react';
import type { LoginRequest } from '../../types/auth';
import Button from '../common/Button';
import styles from './SignupForm.module.css';

interface LoginFormProps {
  onSubmit: (payload: LoginRequest) => Promise<void>;
  isSubmitting: boolean;
}

function LoginForm({ onSubmit, isSubmitting }: LoginFormProps) {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const trimmedLoginId = loginId.trim();
  const trimmedPassword = password.trim();

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!trimmedLoginId || !trimmedPassword) {
      return;
    }

    await onSubmit({
      loginId: trimmedLoginId,
      password: trimmedPassword,
    });
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <label className={styles.field}>
        <span>아이디</span>
        <input
          className={styles.input}
          type="text"
          value={loginId}
          autoComplete="username"
          autoCapitalize="none"
          spellCheck={false}
          maxLength={255}
          required
          disabled={isSubmitting}
          onChange={(event) => setLoginId(event.target.value)}
        />
      </label>

      <label className={styles.field}>
        <span>비밀번호</span>
        <input
          className={styles.input}
          type="password"
          autoComplete="current-password"
          value={password}
          required
          disabled={isSubmitting}
          onChange={(event) => setPassword(event.target.value)}
        />
      </label>

      <Button type="submit" fullWidth disabled={isSubmitting}>
        {isSubmitting ? '로그인 중...' : '로그인'}
      </Button>
    </form>
  );
}

export default LoginForm;
