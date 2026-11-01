import { Link } from 'react-router-dom';
import styles from './SignupRoleSelector.module.css';

const roleOptions = [
  {
    title: '아이로 시작하기',
    description: '오늘 마음을 가볍게 남기고, 가족 반응을 편하게 받아볼 수 있어요.',
    to: '/signup/child',
  },
  {
    title: '가족으로 시작하기',
    description: '아이의 이야기를 따뜻하게 보고, 관계에 맞는 이름으로 반응할 수 있어요.',
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
          <span className={styles.linkText}>이 역할로 계속하기</span>
        </Link>
      ))}
    </div>
  );
}

export default SignupRoleSelector;
