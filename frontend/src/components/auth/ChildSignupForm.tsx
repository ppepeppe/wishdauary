import { useSignupAvailability } from '../../hooks/useSignupAvailability';
import SignupAvailabilityMessage from './SignupAvailabilityMessage';
import { useState } from 'react';
import type { ChildSignupRequest } from '../../types/auth';
import Button from '../common/Button';
import styles from './SignupForm.module.css';

interface ChildSignupFormProps {
  onSubmit: (payload: ChildSignupRequest) => Promise<void>;
  isSubmitting: boolean;
}

function ChildSignupForm({ onSubmit, isSubmitting }: ChildSignupFormProps) {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');

  const loginCheck = useSignupAvailability('LOGIN_ID', loginId);
  const nicknameCheck = useSignupAvailability('NICKNAME', nickname);

  const trimmedLoginId = loginId.trim();
  const trimmedPassword = password.trim();
  const trimmedNickname = nickname.trim();
  const isSubmitInvalid = !trimmedLoginId || !trimmedPassword || !trimmedNickname || loginCheck.status !== 'available' || nicknameCheck.status !== 'available';

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (isSubmitInvalid) {
      return;
    }

    await onSubmit({
      loginId: trimmedLoginId,
      password: trimmedPassword,
      nickname: trimmedNickname,
    });
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <label className={styles.field}>
        <span>아이디</span>
        <input
          className={styles.input}
          type="text"
          aria-describedby={loginCheck.id}
          aria-invalid={loginCheck.status === 'unavailable'}
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
      <SignupAvailabilityMessage check={loginCheck} />

      <label className={styles.field}>
        <span>비밀번호</span>
        <input
          className={styles.input}
          type="password"
          autoComplete="new-password"
          value={password}
          required
          disabled={isSubmitting}
          onChange={(event) => setPassword(event.target.value)}
        />
      </label>

      <label className={styles.field}>
        <span>닉네임</span>
        <input
          className={styles.input}
          type="text"
          aria-describedby={nicknameCheck.id}
          aria-invalid={nicknameCheck.status === 'unavailable'}
          value={nickname}
          maxLength={20}
          required
          disabled={isSubmitting}
          onChange={(event) => setNickname(event.target.value)}
        />
      </label>
      <SignupAvailabilityMessage check={nicknameCheck} />

      <p className={styles.helpText}>가입하면 바로 일기를 쓸 수 있어요.</p>

      <Button type="submit" fullWidth disabled={isSubmitting || isSubmitInvalid}>
        {isSubmitting ? '가입 준비 중...' : '아이로 시작하기'}
      </Button>
    </form>
  );
}

export default ChildSignupForm;
