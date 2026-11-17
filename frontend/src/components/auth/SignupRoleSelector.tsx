import { Link } from 'react-router-dom';
import styles from './SignupRoleSelector.module.css';

const roleOptions = [
  {
    title: '아이로 시작하기',
    description: '일기 작성 · 클로버 모으기 · 선물 요청',
    to: '/signup/child',
  },
  {
    title: '가족으로 시작하기',
    description: '아이 일기 보기 · 댓글 · 선물 관리',
    to: '/signup/family',
  },
];

function SignupRoleSelector() {
  return (
    <div className={styles.grid}>
      {roleOptions.map((option) => (
        <Link key={option.to} to={option.to} className={styles.card}>
          <strong className={styles.title}>{option.title}</strong>
          <p className={styles.description}>{option.description}</p>
          <span className={styles.linkText}>가입하기 →</span>
        </Link>
      ))}
    </div>
  );
}

export default SignupRoleSelector;
