import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../components/common/Button';
import PageSection from '../components/common/PageSection';
import ProfileAvatar from '../components/common/ProfileAvatar';
import NavIcon from '../components/common/NavIcon';
import StatusMessage from '../components/common/StatusMessage';
import { AVATAR_CHARACTERS, AVATAR_PRESETS } from '../constants/avatar';
import { fetchMyProfile, updateMyProfile } from '../services/api/userApi';
import { clearAuthSession, getAuthSession, getDefaultRouteForRole, patchAuthSession } from '../services/session/authSession';
import { compressImageFile } from '../utils/image';
import styles from './MyPage.module.css';
import type { UserRole } from '../types/user';

function MyPage() {
  const navigate = useNavigate();
  const authSession = getAuthSession();
  const isFamily = authSession?.appRole === 'family';
  const [nickname, setNickname] = useState(authSession?.nickname?.trim() || '');
  const [avatarPreset, setAvatarPreset] = useState('peach');
  const [avatarCharacter, setAvatarCharacter] = useState('my-melody');
  const [avatarImage, setAvatarImage] = useState<string | null>(null);
  const [loginId, setLoginId] = useState('');
  const [relationLabel, setRelationLabel] = useState(authSession?.relationLabel || '');
  const [userRole, setUserRole] = useState<UserRole>('user');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;

    async function load() {
      try {
        setIsLoading(true);
        setError(null);
        const profile = await fetchMyProfile();
        if (ignore) {
          return;
        }
        setNickname(profile.nickname);
        setAvatarPreset(profile.avatarPreset || 'peach');
        setAvatarCharacter(profile.avatarCharacter);
        setAvatarImage(profile.avatarImage);
        setLoginId(profile.loginId);
        setUserRole(profile.userRole);
        setRelationLabel(profile.relationLabel || '');
        patchAuthSession({ nickname: profile.nickname, relationLabel: profile.relationLabel });
      } catch (caughtError) {
        if (!ignore) {
          setError(caughtError instanceof Error ? caughtError.message : '내 정보를 불러오지 못했어요.');
        }
      } finally {
        if (!ignore) {
          setIsLoading(false);
        }
      }
    }

    void load();
    return () => {
      ignore = true;
    };
  }, []);

  async function handlePhotoChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) {
      return;
    }

    try {
      setError(null);
      setAvatarImage(await compressImageFile(file));
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : '사진을 불러오지 못했어요.');
    }
  }

  async function handleSave(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedNickname = nickname.trim();
    const trimmedRelationLabel = relationLabel.trim();
    if (!trimmedNickname) {
      setError('닉네임을 적어 주세요.');
      return;
    }
    if (isFamily && !trimmedRelationLabel) {
      setError('아이와의 관계를 적어 주세요.');
      return;
    }

    try {
      setIsSaving(true);
      setError(null);
      setSuccessMessage(null);
      const profile = await updateMyProfile({
        nickname: trimmedNickname,
        avatarPreset,
        avatarCharacter,
        avatarImage,
        ...(isFamily ? { relationLabel: trimmedRelationLabel } : {}),
      });
      setNickname(profile.nickname);
      setAvatarPreset(profile.avatarPreset);
      setAvatarCharacter(profile.avatarCharacter);
      setAvatarImage(profile.avatarImage);
      setUserRole(profile.userRole);
      setRelationLabel(profile.relationLabel || '');
      patchAuthSession({ nickname: profile.nickname, relationLabel: profile.relationLabel });
      setSuccessMessage('내 정보를 저장했어요.');
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : '내 정보를 저장하지 못했어요.');
    } finally {
      setIsSaving(false);
    }
  }

  function handleLogout() {
    clearAuthSession();
    navigate('/');
  }

  return (
    <PageSection
      title="마이페이지"
      description={authSession?.appRole === 'child' ? '내 프로필과 공유할 그룹을 관리해요.' : '내 프로필과 아이 연결을 관리해요.'}
    >
      {isLoading ? <StatusMessage kind="loading" message="내 정보를 준비하고 있어요." /> : null}
      {error ? <StatusMessage kind="error" message={error} /> : null}
      {successMessage ? <StatusMessage kind="success" message={successMessage} /> : null}

      <div className={styles.preview}>
        <ProfileAvatar preset={avatarPreset} character={avatarCharacter} imageUrl={avatarImage} size="lg" alt="" />
        <div>
          <p className={styles.previewName}>{nickname.trim() || '이름 없음'}</p>
          {loginId ? <p className={styles.previewEmail}>{loginId}</p> : null}
          {authSession?.appRole === 'child' ? <p className={styles.previewEmail}>사용자 번호 #{authSession.userId} · 가족에게 알려 주세요.</p> : null}
          {!isLoading ? <span className={styles.roleBadge}>{userRole === 'admin' ? '관리자' : '일반 사용자'}</span> : null}
        </div>
      </div>

      {authSession ? (
        <nav className={styles.managementNav} aria-label="내 활동 관리">
          {userRole === 'admin' ? <Link to="/admin/shop" className={styles.managementLink}>
            <span><strong>공통 상점 상품 관리</strong><small>모든 아이가 볼 수 있는 상점 상품을 등록해요.</small></span>
            <span aria-hidden="true">→</span>
          </Link> : null}
          <Link to="/family-connections" className={styles.managementLink}>
            <span><strong>{authSession.appRole === 'child' ? '가족 연결 요청' : '아이와 연결하기'}</strong><small>{authSession.appRole === 'child' ? '가족이 보낸 요청을 확인하고 승인해요.' : '아이를 검색하고 보낸 요청을 확인해요.'}</small></span><span aria-hidden="true">→</span>
          </Link>
          {authSession.appRole === 'child' ? <Link to="/sharing-groups" className={styles.managementLink}>
            <span>
              <strong>그룹 관리</strong>
              <small>일기를 공유할 사람과 선물을 요청할 가족을 관리해요.</small>
            </span>
            <span aria-hidden="true">→</span>
          </Link> : null}
        </nav>
      ) : null}

      <form className={styles.form} onSubmit={handleSave}>
        <label className={styles.field}>
          <span>닉네임</span>
          <input
            value={nickname}
            maxLength={20}
            disabled={isSaving || isLoading}
            placeholder="어떻게 불러줄까요?"
            onChange={(event) => setNickname(event.target.value)}
          />
        </label>

        {isFamily ? <label className={styles.field}>
          <span>아이와의 관계</span>
          <input
            value={relationLabel}
            maxLength={100}
            required
            disabled={isSaving || isLoading}
            onChange={(event) => setRelationLabel(event.target.value)}
          />
          <small className={styles.fieldHint}>입력한 호칭이 가족 목록과 선물 요청 화면에 보여요.</small>
        </label> : null}

        <details className={styles.appearance}>
          <summary><span><strong>프로필 꾸미기</strong><small>캐릭터 · 배경색 · 내 사진</small></span><span className={styles.disclosureArrow} aria-hidden="true">›</span></summary>
          <div className={styles.appearanceContent}>
            <fieldset className={styles.fieldset} disabled={isSaving || isLoading}>
              <legend>기본 프로필 캐릭터</legend>
              <div className={styles.presetGrid}>
                {AVATAR_CHARACTERS.map((character) => (
                  <button key={character.key} type="button"
                    className={avatarCharacter === character.key && !avatarImage ? styles.presetButtonActive : styles.presetButton}
                    aria-pressed={avatarCharacter === character.key && !avatarImage}
                    onClick={() => { setAvatarCharacter(character.key); setAvatarImage(null); }}>
                    <ProfileAvatar preset={avatarPreset} character={character.key} size="md" />
                    <span>{character.label}</span>
                  </button>
                ))}
              </div>
            </fieldset>

            <fieldset className={styles.fieldset}>
              <legend>기본 프로필 배경색</legend>
              <div className={styles.presetGrid}>
                {AVATAR_PRESETS.map((preset) => (
                  <button
                    key={preset.key}
                    type="button"
                    className={avatarPreset === preset.key ? styles.presetButtonActive : styles.presetButton}
                    disabled={isSaving || isLoading}
                    aria-pressed={avatarPreset === preset.key}
                    onClick={() => setAvatarPreset(preset.key)}
                  >
                    <ProfileAvatar preset={preset.key} character={avatarCharacter} size="sm" />
                    <span>{preset.label}</span>
                  </button>
                ))}
              </div>
            </fieldset>

            <div className={styles.photoField}>
              <span>내 사진으로 바꾸기</span>
              <input
                id="profile-photo"
                type="file"
                className={styles.photoInput}
                aria-label="프로필 이미지 올리기"
                accept="image/*"
                disabled={isSaving || isLoading}
                onChange={handlePhotoChange}
              />
              <label htmlFor="profile-photo" className={styles.uploadButton}><NavIcon name="profile" />사진 선택하기</label>
            </div>

            {avatarImage ? (
              <button
                type="button"
                className={styles.clearPhoto}
                disabled={isSaving}
                onClick={() => setAvatarImage(null)}
              >
                프로필 이미지 지우기
              </button>
            ) : null}
          </div>
        </details>

        <Button type="submit" fullWidth disabled={isSaving || isLoading}>
          {isSaving ? '저장하는 중...' : '내 정보 저장하기'}
        </Button>
      </form>

      <div className={styles.logoutBox}>
        <Button type="button" variant="ghost" fullWidth onClick={handleLogout}>
          로그아웃
        </Button>
        <button
          type="button"
          className={styles.backLink}
          onClick={() => navigate(getDefaultRouteForRole(authSession?.appRole))}
        >
          돌아가기
        </button>
      </div>
    </PageSection>
  );
}

export default MyPage;
