# WishDauary · 위시다유어리

아이의 일기와 가족의 응원, 클로버로 부탁하는 선물을 함께하는 가족용 앱입니다.

## 구성

- `frontend/`: React · TypeScript · Vite 웹 화면과 Capacitor Android/iPhone 프로젝트
- `backend/`: Java 21 · Spring Boot · PostgreSQL API
- `frontend/MOBILE.md`: 모바일 앱 빌드와 로컬 기기 연결 안내

## 주요 기능

- 아이의 일기 작성, 공개 범위와 공유 그룹 관리
- 어른의 아이 검색·연결 요청과 아이의 승인·거절
- 어른용 홈, 아이 일기 열람·댓글·하트
- 아이의 희망 상품 등록, 가족의 선물 등록·클로버 설정과 수정
- 선물 요청 승인·거절과 클로버 사용·환불
- 프로필과 가족 연결 관리

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
