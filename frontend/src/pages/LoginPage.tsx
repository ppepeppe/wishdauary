import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import LoginForm from '../components/auth/LoginForm';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { login } from '../services/api/authApi';
import { getDefaultRouteForRole, saveLoginSession } from '../services/session/authSession';

const DEFAULT_LOGIN_ERROR_MESSAGE = '로그인을 이어가다 잠시 멈췄어요. 입력한 내용을 한 번만 다시 확인해 주세요.';

function LoginPage() {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(payload: Parameters<typeof login>[0]) {
    try {
      setIsSubmitting(true);
      setError(null);
      const loginResult = await login(payload);
      saveLoginSession(loginResult);
      navigate(getDefaultRouteForRole(loginResult.appRole));
    } catch (caughtError) {
      const message =
        caughtError instanceof Error && caughtError.message.trim()
          ? caughtError.message
          : DEFAULT_LOGIN_ERROR_MESSAGE;
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <PageSection
      title="로그인"
      description="이전에 만든 계정으로 바로 이어서 시작할 수 있어요."
    >
      {isSubmitting ? <StatusMessage kind="loading" message="입력한 계정을 확인하고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <LoginForm onSubmit={handleSubmit} isSubmitting={isSubmitting} />
      <p>
        아직 계정이 없다면 <Link to="/signup">회원가입</Link>으로 시작할 수 있어요.
      </p>
    </PageSection>
  );
}

export default LoginPage;
