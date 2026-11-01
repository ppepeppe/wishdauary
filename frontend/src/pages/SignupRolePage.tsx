import { Link } from 'react-router-dom';
import PageSection from '../components/common/PageSection';
import SignupRoleSelector from '../components/auth/SignupRoleSelector';

function SignupRolePage() {
  return (
    <PageSection
      title="어떤 역할로 시작할까요?"
      description="처음부터 아이와 가족을 나눠두면, 각자에게 맞는 화면과 말투로 더 편하게 시작할 수 있어요."
    >
      <SignupRoleSelector />
      <p>
        이미 계정이 있다면 <Link to="/login">로그인</Link>으로 바로 들어갈 수 있어요.
      </p>
    </PageSection>
  );
}

export default SignupRolePage;
