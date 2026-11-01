import { Link } from 'react-router-dom';
import NavIcon, { type NavIconName } from '../components/common/NavIcon';
import styles from './LandingPage.module.css';

const SHORTCUTS: { label: string; icon: NavIconName; to: string }[] = [
  { label: '일기 쓰기', icon: 'write', to: '/login' },
  { label: '내 일기', icon: 'diary', to: '/login' },
  { label: '선물 둘러보기', icon: 'gift', to: '/login' },
  { label: '가족 연결', icon: 'family', to: '/signup/family' },
];

function LandingPage() {
  return (
    <div className={styles.page}>
      <nav className={styles.navigation} aria-label="서비스 둘러보기">
        <a href="#start" className={styles.current}>홈</a>
        <a href="#diary-preview">일기 쓰는 법</a>
        <a href="#wish-preview">선물</a>
        <a href="#guide">이용 안내</a>
      </nav>

      <div className={styles.content}>
        <section id="start" className={styles.intro}>
          <div className={styles.banner}>
            <span className={styles.bannerLabel}>우리 가족의 일기 서비스</span>
            <h1>오늘의 이야기를 남기고,<br />가족과 함께 읽어요</h1>
            <p>기분과 일상을 기록하고, 클로버로 원하는 선물을 모아보세요.</p>
            <Link to="/signup/child" className={styles.primaryLink}>일기 시작하기 <span aria-hidden="true">›</span></Link>
          </div>
          <aside className={styles.joinPanel}>
            <span className={styles.joinIcon}><NavIcon name="family" /></span>
            <h2>아이의 이야기를 함께 읽어요</h2>
            <p>가족 계정으로 가입한 뒤 그룹으로 연결하고<br />일기에 댓글과 하트를 남겨보세요.</p>
            <Link to="/signup/family">가족 계정으로 가입하기 <span aria-hidden="true">›</span></Link>
            <span className={styles.loginHint}>이미 계정이 있다면 <Link to="/login">로그인</Link></span>
          </aside>
        </section>

        <nav className={styles.shortcuts} aria-label="주요 기능">
          {SHORTCUTS.map((item) => (
            <Link to={item.to} key={item.label}><span className={styles.shortcutIcon}><NavIcon name={item.icon} /></span><span>{item.label}</span></Link>
          ))}
        </nav>

        <section id="diary-preview" className={styles.section}>
          <div className={styles.sectionHeading}><div><h2>이렇게 기록해요</h2><p>아이의 실제 기록은 가입 후 본인과 공개한 가족에게만 보여요.</p></div><Link to="/signup/child">나도 쓰기 <span aria-hidden="true">›</span></Link></div>
          <div className={styles.diaryGrid}>
            <article className={styles.diaryCard}><h3>1. 오늘의 기분 고르기</h3><p>지금 마음과 가까운 표정을 골라요.</p></article>
            <article className={styles.diaryCard}><h3>2. 내 이야기 남기기</h3><p>길게 쓰지 않아도 괜찮아요. 떠오르는 일을 편하게 적어요.</p></article>
            <article className={styles.diaryCard}><h3>3. 가족과 나누기</h3><p>공개할 사람을 직접 고르면 가족이 하트와 댓글로 응원해요.</p></article>
          </div>
        </section>

        <section id="wish-preview" className={styles.section}>
          <div className={styles.sectionHeading}><div><h2>기록하며 모으는 선물</h2><p>갖고 싶은 선물을 목표로 정하고 클로버를 모아요.</p></div><Link to="/login">로그인하고 보기 <span aria-hidden="true">›</span></Link></div>
          <div className={styles.wishGrid}>
            <div className={styles.cloverGuide}><span className={styles.cloverLabel}>나만의 선물 목록</span><h3>갖고 싶은 선물을<br />직접 적어두기</h3><p>이름과 설명을 적으면 선물 놀이터에 저장돼요. 사진과 쇼핑몰 링크는 선택할 수 있어요.</p><Link to="/signup/child">시작하기 <span aria-hidden="true">›</span></Link></div>
            <div className={styles.cloverGuide}><span className={styles.cloverLabel}>클로버 이용 안내</span><h3>매일 쓰는 일기가<br />선물에 한 걸음 더</h3><p>일기를 쓰면 클로버가 쌓여요.<br />목표를 채우면 가족에게 선물을 요청해요.</p><a href="#guide">자세히 알아보기 <span aria-hidden="true">›</span></a></div>
          </div>
        </section>

        <section id="guide" className={styles.section}>
          <div className={styles.sectionHeading}><h2>처음 이용하시나요?</h2></div>
          <div className={styles.guideGrid}>
            <details className={styles.guideItem}><summary>일기는 어떻게 쓰나요?<span aria-hidden="true">+</span></summary><p>아이 계정으로 가입한 뒤 ‘일기 쓰기’를 눌러 주세요. 날짜와 기분을 고르고 글로 이야기를 남길 수 있어요.</p></details>
            <details className={styles.guideItem}><summary>가족은 어떻게 연결하나요?<span aria-hidden="true">+</span></summary><p>가족 계정으로 가입한 뒤 아이의 닉네임을 검색해 연결 요청을 보내요. 아이가 승인한 경우에만 가족으로 연결돼요.</p></details>
            <details className={styles.guideItem}><summary>누가 일기를 볼 수 있나요?<span aria-hidden="true">+</span></summary><p>선택한 그룹에 공개한 일기는 그 그룹 구성원이 볼 수 있어요. 연결된 가족 공개를 선택하면 내 그룹에 추가한 가족 계정 모두가 볼 수 있어요.</p></details>
            <details className={styles.guideItem}><summary>클로버는 어떻게 쓰나요?<span aria-hidden="true">+</span></summary><p>일기를 쓰며 모은 클로버로 선물 목표를 채워요. 목표를 달성하면 가족에게 선물을 요청할 수 있어요.</p></details>
          </div>
        </section>
      </div>
      <footer className={styles.footer}><div><strong>위시다유어리</strong><p>아이와 가족이 함께하는 일상 기록</p></div><Link to="/login">로그인</Link><Link to="/signup">회원가입</Link></footer>
    </div>
  );
}

export default LandingPage;
