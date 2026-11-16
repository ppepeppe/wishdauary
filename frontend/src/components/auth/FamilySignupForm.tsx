import { useState } from 'react';
import type { FamilySignupRequest } from '../../types/auth';
import Button from '../common/Button';
import styles from './SignupForm.module.css';

interface FamilySignupFormProps {
  onSubmit: (payload: FamilySignupRequest) => Promise<void>;
  isSubmitting: boolean;
}

function FamilySignupForm({ onSubmit, isSubmitting }: FamilySignupFormProps) {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [relationLabel, setRelationLabel] = useState('');

  const trimmedLoginId = loginId.trim();
  const trimmedPassword = password.trim();
  const trimmedNickname = nickname.trim();
  const trimmedRelationLabel = relationLabel.trim();
  const isSubmitInvalid =
    !trimmedLoginId ||
    !trimmedPassword ||
    !trimmedNickname ||
    !trimmedRelationLabel;

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (isSubmitInvalid) {
      return;
    }

    await onSubmit({
      loginId: trimmedLoginId,
      password: trimmedPassword,
      nickname: trimmedNickname,
      relationLabel: trimmedRelationLabel,
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
          value={nickname}
          required
          disabled={isSubmitting}
          onChange={(event) => setNickname(event.target.value)}
        />
      </label>

      <label className={styles.field}>
        <span>아이와의 관계</span>
        <input
          className={styles.input}
          type="text"
          value={relationLabel}
          maxLength={100}
          required
          disabled={isSubmitting}
          onChange={(event) => setRelationLabel(event.target.value)}
        />
        <p className={styles.helpText}>엄마, 고모, 큰아빠처럼 아이가 부르는 호칭을 적어 주세요.</p>
      </label>

      <Button type="submit" fullWidth disabled={isSubmitting}>
        {isSubmitting ? '가입 준비 중...' : '가입하고 아이 찾기'}
      </Button>
    </form>
  );
}

export default FamilySignupForm;
