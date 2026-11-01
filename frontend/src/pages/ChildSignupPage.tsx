import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ChildSignupForm from '../components/auth/ChildSignupForm';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { signupChild } from '../services/api/authApi';
import { getDefaultRouteForRole, saveSignupSession } from '../services/session/authSession';

const DEFAULT_CHILD_SIGNUP_ERROR_MESSAGE = '아이 가입을 이어가다 잠시 멈췄어요. 입력한 내용을 한 번만 다시 확인해 주세요.';

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
      description="복잡하게 정하지 않아도 괜찮아요. 자주 쓰는 이름으로 바로 시작하고, 가족은 나중에 연결할 수 있어요."
    >
      {isSubmitting ? <StatusMessage kind="loading" message="아이 계정을 차분히 만들고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <ChildSignupForm onSubmit={handleSubmit} isSubmitting={isSubmitting} />
      <p>
        이미 계정이 있다면 <Link to="/login">로그인</Link>으로 이어서 시작할 수 있어요.
      </p>
    </PageSection>
  );
}

export default ChildSignupPage;
