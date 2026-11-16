# WishDauary · 위시다유어리

아이의 일기와 가족의 응원, 클로버로 부탁하는 선물을 함께하는 가족용 앱입니다.

## 구성

- `frontend/`: React · TypeScript · Vite 웹 화면과 Capacitor Android/iPhone 프로젝트
- `backend/`: Java 21 · Spring Boot · PostgreSQL API
- `frontend/MOBILE.md`: 모바일 앱 빌드와 로컬 기기 연결 안내
- `compose.yaml`, `deploy/`: EC2 한 대에서 웹·API·PostgreSQL 실행, HTTPS·백업 안내

EC2 배포는 [서버 배포 안내](deploy/README.md)를 따릅니다. `sh deploy/init-env.sh`로 비밀값을 생성하고 접속 주소를 설정한 뒤 Docker Compose로 실행합니다. 기존 로컬 DB를 유지하려면 안내의 백업·복원 단계도 진행합니다.

## 주요 기능

- 이메일 인증 없이 아이디·비밀번호로 가입·로그인
- 아이의 일기 작성, 공개 범위와 공유 그룹 관리
- 어른의 아이 검색·연결 요청과 아이의 승인·거절
- 어른용 홈, 아이 일기 열람·댓글·하트
- 아이의 희망 상품 등록, 가족의 선물 등록·클로버 설정과 수정
- 선물 요청 승인·거절과 클로버 사용·환불
- 프로필과 가족 연결 관리
- 관리자·일반 사용자 권한 저장과 관리자 공통 상점 상품 등록

## 계정 로그인

아이·가족 모두 이메일 주소 대신 공백 없는 아이디와 비밀번호로 가입합니다. 한글 아이디도 사용할 수 있으며 아이디는 최대 255자입니다. 아이디는 아이·가족 전체에서 중복될 수 없습니다. 기존 계정은 이전 이메일 주소를 아이디 칸에 입력하면 같은 비밀번호로 로그인할 수 있습니다.

인증 API의 기본 필드는 `loginId`이며 구버전 앱의 `email` 요청 필드도 호환합니다. 프로필은 `loginId`와 구버전 호환용 `email`을 함께 반환합니다. 기존 DB의 `users.email` 컬럼을 로그인 아이디에 매핑해 사용하므로 데이터 변환이나 계정 재생성은 필요 없습니다.

가족 계정은 마이페이지의 **아이와의 관계**를 수정할 수 있습니다. 저장한 호칭은 연결된 아이의 가족 목록·선물 요청 대상 등에 반영됩니다. 관계를 보내지 않는 구버전 프로필 수정 요청은 기존 호칭을 유지하며, 이전 선물 요청에 저장된 당시 호칭은 변경하지 않습니다.

가입 시 닉네임과 아이와의 관계의 앞뒤 공백을 제거하고, 각각 1~20자와 1~100자로 제한합니다. 가족의 아이 검색은 한 글자 닉네임을 정확히 일치하는 경우에만 찾고, 두 글자 이상은 부분 검색합니다. 동명이인은 아이 마이페이지의 사용자 번호로 구분할 수 있습니다.

## 공통 상점 관리

아이·가족 구분(`app_role`)과 별도로 `users.user_role`에 `ADMIN` 또는 `USER`를 저장합니다. 기존 계정과 신규 가입은 기본 `USER`입니다. 관리자 지정은 운영 DB에서 해당 계정만 변경합니다.

```sql
UPDATE users SET user_role = 'ADMIN' WHERE email = '관리자로 지정할 이메일';
```

관리자 계정의 마이페이지에 **공통 상점 상품 관리**가 표시됩니다. `/admin/shop`에서 상품명, 설명, 사진, 클로버, 선택 구매가·쇼핑몰 링크를 입력하면 모든 아이의 공통 상점에 즉시 표시됩니다. 사진은 기존 상품 사진과 동일하게 PostgreSQL에 저장됩니다. 개인 선물과 가족별 요청에는 추가되지 않습니다.

`GET /api/v1/admin/shop/products`, `POST /api/v1/admin/shop/products`는 기존 로그인 확인만 적용합니다. 요청에 따라 `ADMIN` 권한 검사와 관리자 전용 접근 차단은 아직 적용하지 않았습니다. 메뉴 표시는 권한에 따라 구분하지만, 로그인한 일반 사용자도 직접 주소나 API로 접근할 수 있는 현재 단계의 의도된 동작입니다. 프로필 수정·가입 API로 `user_role`을 변경할 수는 없습니다.

## 로컬 실행

Node.js 22 이상, Java 21, PostgreSQL이 필요합니다.

백엔드는 기본적으로 `localhost:5432/wishdiary` 데이터베이스를 사용합니다. 기본 사용자·비밀번호는 로컬 개발용 `postgres`이며, 환경에 맞게 `WISHDIARY_DB_URL`, `WISHDIARY_DB_USERNAME`, `WISHDIARY_DB_PASSWORD`로 지정할 수 있습니다. 외부 배포 시에는 `WISHDIARY_AUTH_TOKEN_SECRET`도 별도로 설정합니다.

선물 사진은 기기에서 선택하면 긴 변 1,000px 이하로 줄여 전송합니다. 서버에서 실제 이미지인지 확인하고 메타데이터를 제거한 JPEG를 `shop_products.image_data` (`BYTEA`)에 저장합니다. 글과 사진은 함께 저장되며, 등록을 취소하면 업로드되지 않습니다. 모은 클로버로 선물을 요청할 때는 당시 사진도 `gift_request_notifications.product_image_data`에 보관합니다. 사진은 권한이 있는 선물 조회 응답에 포함되고, 별도의 공개 파일 주소는 만들지 않습니다. 사진을 포함한 백업은 PostgreSQL 전체 백업으로 할 수 있습니다.

```sh
cd backend
./gradlew bootRun
```

다른 터미널에서 프론트엔드를 실행합니다.

```sh
cd frontend
npm ci
npm run dev
```

화면은 `http://localhost:5187`, API는 `http://localhost:8080`에서 실행됩니다. 로컬 화면의 API 요청은 개발 서버를 통해 전달됩니다. 필요한 경우 `frontend/.env.example`을 참고해 로컬 환경 파일을 작성합니다.

## 검증

```sh
cd backend
./gradlew test
```

백엔드 테스트는 별도 H2 데이터베이스를 사용합니다.

```sh
cd frontend
npm test
npm run build
```

Android APK와 iPhone 프로젝트 준비 방법은 [모바일 실행 안내](frontend/MOBILE.md)를 참고하세요. iPhone 앱 컴파일과 서명에는 Xcode가 필요합니다.
