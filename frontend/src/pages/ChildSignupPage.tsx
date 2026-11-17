import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ChildSignupForm from '../components/auth/ChildSignupForm';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { signupChild } from '../services/api/authApi';
import { getDefaultRouteForRole, saveSignupSession } from '../services/session/authSession';

const DEFAULT_CHILD_SIGNUP_ERROR_MESSAGE = '가입하지 못했어요. 입력한 내용을 확인해 주세요.';

function ChildSignupPage() {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(payload: Parameters<typeof signupChild>[0]) {
    try {
      setIsSubmitting(true);
      setError(null);
      const signupResult = await signupChild(payload);
      saveSignupSession(signupResult);
      navigate(getDefaultRouteForRole(signupResult.appRole));
    } catch (caughtError) {
      const message =
        caughtError instanceof Error && caughtError.message.trim()
          ? caughtError.message
          : DEFAULT_CHILD_SIGNUP_ERROR_MESSAGE;
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <PageSection
      variant="form"
      title="아이 가입"
    >
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <ChildSignupForm onSubmit={handleSubmit} isSubmitting={isSubmitting} />
      <p>
        이미 가입했나요? <Link to="/login">로그인</Link>
      </p>
    </PageSection>
  );
}

export default ChildSignupPage;
