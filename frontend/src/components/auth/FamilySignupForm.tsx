import { useState } from 'react';
import { FAMILY_RELATION_OPTIONS, getDefaultRelationLabel } from '../../constants/signup';
import type { FamilyRelation, FamilySignupRequest } from '../../types/auth';
import Button from '../common/Button';
import styles from './SignupForm.module.css';

interface FamilySignupFormProps {
  onSubmit: (payload: FamilySignupRequest) => Promise<void>;
  isSubmitting: boolean;
}

function FamilySignupForm({ onSubmit, isSubmitting }: FamilySignupFormProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [familyRelation, setFamilyRelation] = useState<FamilyRelation>('mother');
  const [relationLabel, setRelationLabel] = useState(getDefaultRelationLabel('mother'));

  const trimmedEmail = email.trim();
  const trimmedPassword = password.trim();
  const trimmedNickname = nickname.trim();
  const trimmedRelationLabel = relationLabel.trim();
  const isSubmitInvalid =
    !trimmedEmail ||
    !trimmedPassword ||
    !trimmedNickname ||
    !trimmedRelationLabel;

  function handleRelationChange(nextRelation: FamilyRelation) {
    setFamilyRelation(nextRelation);
    setRelationLabel(getDefaultRelationLabel(nextRelation));
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (isSubmitInvalid) {
      return;
    }

    await onSubmit({
      email: trimmedEmail,
      password: trimmedPassword,
      nickname: trimmedNickname,
      familyRelation,
      relationLabel: trimmedRelationLabel,
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
          placeholder="가족 안에서 부를 이름"
          onChange={(event) => setNickname(event.target.value)}
        />
      </label>

      <div className={styles.field}>
        <span>아이와의 관계</span>
        <div className={styles.optionGrid}>
          {FAMILY_RELATION_OPTIONS.map((option) => (
            <button
              key={option.value}
              type="button"
              className={`${styles.optionCard} ${familyRelation === option.value ? styles.optionCardSelected : ''}`}
              disabled={isSubmitting}
              onClick={() => handleRelationChange(option.value)}
            >
              <span className={styles.optionTitle}>{option.label}</span>
            </button>
          ))}
        </div>
      </div>

      <label className={styles.field}>
        <span>표시 이름</span>
        <input
          className={styles.input}
          type="text"
          value={relationLabel}
          required
          disabled={isSubmitting}
          placeholder="아이에게 보일 관계 이름"
          onChange={(event) => setRelationLabel(event.target.value)}
        />
        <p className={styles.helpText}>기본값은 자동으로 들어가고, 필요하면 더 편한 호칭으로 바꿀 수 있어요.</p>
      </label>

      <Button type="submit" fullWidth disabled={isSubmitting}>
        {isSubmitting ? '가입 준비 중...' : '가입하고 아이 찾기'}
      </Button>
    </form>
  );
}

export default FamilySignupForm;
