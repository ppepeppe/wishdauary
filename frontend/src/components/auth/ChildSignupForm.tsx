import { useState } from 'react';
import type { ChildSignupRequest } from '../../types/auth';
import Button from '../common/Button';
import styles from './SignupForm.module.css';

interface ChildSignupFormProps {
  onSubmit: (payload: ChildSignupRequest) => Promise<void>;
  isSubmitting: boolean;
}

function ChildSignupForm({ onSubmit, isSubmitting }: ChildSignupFormProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');

  const trimmedEmail = email.trim();
  const trimmedPassword = password.trim();
  const trimmedNickname = nickname.trim();
  const isSubmitInvalid = !trimmedEmail || !trimmedPassword || !trimmedNickname;

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (isSubmitInvalid) {
      return;
    }

    await onSubmit({
      email: trimmedEmail,
      password: trimmedPassword,
      nickname: trimmedNickname,
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

      <label className={styles.field}>
        <span>닉네임</span>
        <input
          className={styles.input}
          type="text"
          value={nickname}
          required
          disabled={isSubmitting}
          placeholder="친구들이 부르기 편한 이름"
          onChange={(event) => setNickname(event.target.value)}
        />
      </label>

      <p className={styles.helpText}>아이 가입은 바로 새 그룹을 만드는 흐름으로 시작돼요. 가족은 나중에 연결할 수 있어요.</p>

      <Button type="submit" fullWidth disabled={isSubmitting}>
        {isSubmitting ? '가입 준비 중...' : '아이로 시작하기'}
      </Button>
    </form>
  );
}

export default ChildSignupForm;
