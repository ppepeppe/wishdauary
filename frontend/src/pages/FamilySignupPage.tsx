import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import FamilySignupForm from '../components/auth/FamilySignupForm';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { signupFamily } from '../services/api/authApi';
import { saveSignupSession } from '../services/session/authSession';

const DEFAULT_FAMILY_SIGNUP_ERROR_MESSAGE = '가입하지 못했어요. 입력한 내용을 확인해 주세요.';

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
      variant="form"
      title="가족 가입"
      description="아이의 승인 후 가족으로 연결됩니다."
    >
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <FamilySignupForm
        onSubmit={handleSubmit}
        isSubmitting={isSubmitting}
      />
      <p>
        이미 가입했나요? <Link to="/login">로그인</Link>
      </p>
    </PageSection>
  );
}

export default FamilySignupPage;
