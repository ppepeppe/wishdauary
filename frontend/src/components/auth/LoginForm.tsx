import { useState } from 'react';
import type { LoginRequest } from '../../types/auth';
import Button from '../common/Button';
import styles from './SignupForm.module.css';

interface LoginFormProps {
  onSubmit: (payload: LoginRequest) => Promise<void>;
  isSubmitting: boolean;
}

function LoginForm({ onSubmit, isSubmitting }: LoginFormProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const trimmedEmail = email.trim();
  const trimmedPassword = password.trim();

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!trimmedEmail || !trimmedPassword) {
      return;
    }

    await onSubmit({
      email: trimmedEmail,
      password: trimmedPassword,
    });
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <label className={styles.field}>
        <span>이메일</span>
        <input
          className={styles.input}
          type="email"
          value={email}
          required
          disabled={isSubmitting}
          placeholder="name@example.com"
          onChange={(event) => setEmail(event.target.value)}
        />
      </label>

      <label className={styles.field}>
        <span>비밀번호</span>
        <input
          className={styles.input}
          type="password"
          value={password}
          required
          disabled={isSubmitting}
          placeholder="비밀번호를 입력해 주세요"
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
