import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import LoginForm from '../components/auth/LoginForm';
import PageSection from '../components/common/PageSection';
import StatusMessage from '../components/common/StatusMessage';
import { login } from '../services/api/authApi';
import { getDefaultRouteForRole, saveLoginSession } from '../services/session/authSession';

const DEFAULT_LOGIN_ERROR_MESSAGE = '로그인하지 못했어요. 아이디와 비밀번호를 확인해 주세요.';

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
      variant="form"
      title="로그인"
    >
      {error ? <StatusMessage kind="error" message={error} /> : null}
      <LoginForm onSubmit={handleSubmit} isSubmitting={isSubmitting} />
      <p>
        계정이 없나요? <Link to="/signup">회원가입</Link>
      </p>
    </PageSection>
  );
}

export default LoginPage;
