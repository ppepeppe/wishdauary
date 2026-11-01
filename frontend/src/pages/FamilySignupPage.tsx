import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import FamilySignupForm from '../components/auth/FamilySignupForm';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { signupFamily } from '../services/api/authApi';
import { saveSignupSession } from '../services/session/authSession';

const DEFAULT_FAMILY_SIGNUP_ERROR_MESSAGE = '가족 가입을 이어가다 잠시 멈췄어요. 입력한 내용을 한 번만 다시 확인해 주세요.';

function FamilySignupPage() {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(payload: Parameters<typeof signupFamily>[0]) {
    try {
      setIsSubmitting(true);
      setError(null);
      const signupResult = await signupFamily(payload);
      saveSignupSession(signupResult);
      navigate('/family-connections', { replace: true });
    } catch (caughtError) {
      const message =
        caughtError instanceof Error && caughtError.message.trim()
          ? caughtError.message
          : DEFAULT_FAMILY_SIGNUP_ERROR_MESSAGE;
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <PageSection
      title="가족 가입"
      description="가입 후 아이의 닉네임을 검색해 연결을 요청해요. 아이가 승인하면 가족으로 연결돼요."
    >
      {isSubmitting ? <StatusMessage kind="loading" message="가족 계정을 차분히 만들고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <FamilySignupForm
        onSubmit={handleSubmit}
        isSubmitting={isSubmitting}
      />
      <p>
        이미 계정이 있다면 <Link to="/login">로그인</Link>으로 바로 들어갈 수 있어요.
      </p>
    </PageSection>
  );
}

export default FamilySignupPage;
