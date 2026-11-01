import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../components/common/Button';
import PageSection from '../components/common/PageSection';
import ProfileAvatar from '../components/common/ProfileAvatar';
import StatusMessage from '../components/common/StatusMessage';
import { AVATAR_PRESETS } from '../constants/avatar';
import { fetchMyProfile, updateMyProfile } from '../services/api/userApi';
import { clearAuthSession, getAuthSession, getDefaultRouteForRole, patchAuthSession } from '../services/session/authSession';
import { compressImageFile } from '../utils/image';
import styles from './MyPage.module.css';

function MyPage() {
  const navigate = useNavigate();
  const authSession = getAuthSession();
  const [nickname, setNickname] = useState(authSession?.nickname?.trim() || '');
  const [avatarPreset, setAvatarPreset] = useState('peach');
  const [avatarImage, setAvatarImage] = useState<string | null>(null);
  const [email, setEmail] = useState('');
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
        setAvatarImage(profile.avatarImage);
        setEmail(profile.email);
        patchAuthSession({ nickname: profile.nickname });
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
    if (!trimmedNickname) {
      setError('닉네임을 적어 주세요.');
      return;
    }

    try {
      setIsSaving(true);
      setError(null);
      setSuccessMessage(null);
      const profile = await updateMyProfile({
        nickname: trimmedNickname,
        avatarPreset,
        avatarImage,
      });
      setNickname(profile.nickname);
      setAvatarPreset(profile.avatarPreset);
      setAvatarImage(profile.avatarImage);
      patchAuthSession({ nickname: profile.nickname });
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
        <ProfileAvatar preset={avatarPreset} imageUrl={avatarImage} size="lg" alt="" />
        <div>
          <p className={styles.previewName}>{nickname.trim() || '이름 없음'}</p>
          {email ? <p className={styles.previewEmail}>{email}</p> : null}
        </div>
      </div>

      {authSession ? (
        <nav className={styles.managementNav} aria-label="내 활동 관리">
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

        <fieldset className={styles.fieldset}>
          <legend>기본 프로필색</legend>
          <div className={styles.presetGrid}>
            {AVATAR_PRESETS.map((preset) => (
              <button
                key={preset.key}
                type="button"
                className={avatarPreset === preset.key ? styles.presetButtonActive : styles.presetButton}
                disabled={isSaving || isLoading}
                onClick={() => setAvatarPreset(preset.key)}
              >
                <ProfileAvatar preset={preset.key} size="sm" />
                <span>{preset.label}</span>
              </button>
            ))}
          </div>
        </fieldset>

        <label className={styles.photoField}>
          <span>프로필 이미지 올리기</span>
          <input
            type="file"
            accept="image/*"
            disabled={isSaving || isLoading}
            onChange={handlePhotoChange}
          />
        </label>

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
