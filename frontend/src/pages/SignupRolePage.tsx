import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import SignupRoleSelector from '../components/auth/SignupRoleSelector';

function SignupRolePage() {
  return (
    <PageSection
      variant="form"
      title="어떤 역할로 시작할까요?"
    >
      <SignupRoleSelector />
      <p>
        이미 가입했나요? <Link to="/login">로그인</Link>
      </p>
    </PageSection>
  );
}

export default SignupRolePage;
