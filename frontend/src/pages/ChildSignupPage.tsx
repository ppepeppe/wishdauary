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
      title="아이 가입"
      description="이메일과 닉네임으로 시작해요. 가족은 가입 후 연결할 수 있어요."
    >
      {isSubmitting ? <StatusMessage kind="loading" message="계정을 만들고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <ChildSignupForm onSubmit={handleSubmit} isSubmitting={isSubmitting} />
      <p>
        이미 계정이 있다면 <Link to="/login">로그인</Link>으로 이어서 시작할 수 있어요.
      </p>
    </PageSection>
  );
}

export default ChildSignupPage;
