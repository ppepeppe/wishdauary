# 일기 공개 그룹

## 사용 흐름

1. 아이 계정으로 **내 일기 → 공개 그룹 관리**에서 그룹을 만든다.
2. 그룹 이름을 정하고 서비스 사용자 닉네임을 검색해 구성원을 추가한다. 같은 닉네임은 사용자 번호로 구분한다.
3. 일기 작성·수정 화면에서 **나만 보기 / 전체공개 / 그룹 선택 / 연결된 가족 공개** 중 공개 범위를 고른다.
4. 그룹 공개는 여러 그룹을 선택할 수 있다. 선택된 그룹 중 하나에 속하면 열람할 수 있다.
5. **공개·그룹 피드**에서 전체공개 일기와 자신에게 공유된 그룹 일기를 읽는다. 연결된 가족의 공개 일기는 가족 피드에도 나타난다.

전체공개는 로그인한 서비스 사용자 전체를 뜻한다. 비로그인 접근은 기존 인증 규칙에 따라 차단한다. 기존 가족 공개 일기는 자동으로 전체공개로 전환하지 않는다.

## 그룹 규칙

- 아이마다 활성 그룹 최대 5개. 동시 생성도 소유자 행 잠금과 데이터베이스 슬롯 제약으로 제한한다.
- 그룹 이름은 1~30자. 검색 결과는 최대 20명이며 닉네임·사용자 번호·계정 역할만 반환한다.
- 그룹 생성·수정·삭제와 사용자 검색은 아이만 할 수 있다. 그룹은 만든 아이만 관리한다.
- 구성원이 없는 그룹도 미리 만들 수 있다. 이 그룹에만 공개한 일기는 작성자만 볼 수 있다.
- 구성원 변경은 해당 그룹으로 공유된 이전 일기에도 적용된다.
- 그룹 삭제는 일기를 삭제하지 않는다. 삭제된 그룹은 권한을 부여하지 않으며, 새 그룹에 이전 일기의 권한이 이어지지 않는다.
- 삭제된 그룹을 포함한 일기를 수정할 때는 선택에서 해당 그룹을 제거하거나 다른 공개 범위를 선택한다.
- 일기 작성자는 항상 자기 일기와 기존 댓글을 읽을 수 있다. 다른 사용자는 공개 범위 검사에 통과해야 한다.
- 하트·댓글 작성은 기존 가족 계정 전용 정책을 유지한다. 공개·그룹 범위에 포함된 가족 계정만 반응할 수 있다.

## 변경 위치

- 화면: `frontend/src/pages/SharingGroupsPage.tsx`, `SharedDiaryFeedPage.tsx`
- 공통 공개 선택: `frontend/src/components/sharing/DiaryAudiencePicker.tsx`
- 그룹 유스케이스·포트: `backend/src/main/java/com/wishdiary/application/sharing/`
- 일기 접근 정책: `backend/src/main/java/com/wishdiary/application/diary/DiaryAudiencePolicy.java`
- 스키마: `backend/src/main/resources/db/migration/V9__add_diary_sharing_groups.sql`

API는 `/api/v1/sharing-groups`의 목록·생성·수정·삭제, `/api/v1/sharing-groups/users?query=...`의 사용자 검색, `/api/v1/diaries/shared-feed?page=0`의 피드를 추가했다. 피드는 페이지당 30개이며 최신 생성 순이다. 일기 생성·수정 요청과 작성자 조회 응답에 `sharingGroupIds` 배열을 추가했다.

## 검증 기록 — 2026-09-27

- 프런트엔드 `npm test`: 16개 통과.
- 프런트엔드 `npm run build`: TypeScript·정적 CSS 참조 검사·프로덕션 빌드 통과.
- `git diff --check`: 통과.
- 백엔드: Java 21과 프로젝트에 이미 저장된 의존성으로 전체 main/test 소스를 컴파일하고 JUnit Launcher로 101개 테스트 실행, 모두 통과.
- 실행 범위: domain/application 단위 테스트, `DiarySharingIntegrationTest`, `FlywaySchemaValidationTest`. 새 통합 테스트는 MockMvc와 H2를 사용하며 서버 포트를 열지 않는다.
- 검증 내용: 전체공개·비공개·기존 가족 공개 구분, 복수 그룹 선택, 다른 그룹 소유권 차단, 구성원 추가·제거, 그룹 삭제·재생성, 공개 범위 수정, 일기 삭제, 가족 피드 접근 제한, 댓글·하트 접근 제한, 사용자 검색 반환 정보, 6개 동시 생성 시 5개 제한, V9 마이그레이션과 JPA 스키마 검증.
- 로컬 실행 도구와 로그는 `backend/build/local-check/`에 보관했다. 이번 실행은 Gradle 전체 테스트 작업을 실행한 결과가 아니다. 기존 서버 포트를 사용하는 `AuthAndShopFlowIntegrationTest`는 실행 범위에 포함하지 않았다.
- 실제 PostgreSQL 데이터 변경, 실행 중인 앱 재시작, 브라우저 종단 간 검증, 외부 배포는 수행하지 않았다. 실행 환경에서 새 백엔드를 시작할 때 V9 마이그레이션 적용이 필요하다.
