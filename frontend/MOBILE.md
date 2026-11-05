# 위시다유어리 Android · iPhone 앱

기존 React 화면을 Capacitor 8 앱 안에 포함합니다. 앱은 프론트 웹 서버 없이 설치된 화면 코드를 실행하고, 일기·로그인·선물 요청은 별도 Spring Boot 백엔드에 보냅니다. 기본 이미지와 Pretendard 글꼴도 앱에 포함됩니다. 서버 통신이 필요한 기능은 인터넷 연결이 필요하며 오프라인 저장은 구현하지 않았습니다.

- 앱 이름: 위시다유어리
- Android application ID / iOS bundle ID: `com.wishdiary.app`
- Android 프로젝트: `android/`
- iOS 프로젝트: `ios/App/App.xcodeproj` (Swift Package Manager)
- 앱에 포함할 화면 빌드: `dist-native/`
- 기존 웹 배포 빌드: `dist/`

## 준비

Node.js 22 이상을 사용합니다. 모든 npm 명령은 이 파일이 있는 `frontend/`에서 실행합니다.

```sh
npm ci
```

로컬 백엔드와 PostgreSQL이 실행 중이어야 합니다. 기존 개발 DB를 사용하는 경우 저장한 일기와 선물 요청도 그 DB에 반영됩니다. 자동 테스트는 별도 H2 DB를 사용합니다.

## 안드로이드

[Capacitor 환경 안내](https://capacitorjs.com/docs/getting-started/environment-setup)에 맞는 Android Studio, Android SDK 36, JDK 21이 필요합니다. SDK 경로는 Android Studio의 SDK Manager에서 확인해 `ANDROID_HOME` 또는 무시되는 `android/local.properties`에 지정합니다.

```sh
# Android 에뮬레이터 → 컴퓨터의 localhost:8080
npm run mobile:android

# Android Studio 열기 / 에뮬레이터·연결 기기에서 실행
npm run mobile:android -- --open
npm run mobile:android -- --run

# 디버그 APK 생성
npm run mobile:android:apk
```

APK는 `artifacts/wishdauary-android-debug.apk`에 복사되며, 연결 주소는 같은 폴더의 `mobile-build.json`에서 확인할 수 있습니다. 원본은 `android/app/build/outputs/apk/debug/app-debug.apk`입니다. 주소를 지정하지 않고 빌드하면 Android 에뮬레이터용 `http://10.0.2.2:8080`를 사용합니다. 실제 휴대폰에서는 아래의 Wi-Fi 주소 설정으로 빌드하세요.

개발 도구가 CLI만 설치된 경우에도 SDK/JDK 경로를 지정하면 APK를 빌드할 수 있습니다. 개발용 HTTP 연결 허용은 Android Debug manifest에만 있습니다. Release 빌드는 HTTP를 허용하지 않으며 개발용 화면 번들이 들어 있으면 중단됩니다.

## 아이폰

Xcode 26 이상을 설치한 뒤 Xcode를 한 번 열어 필수 구성 요소 설치를 마칩니다. 이 프로젝트는 CocoaPods 없이 Swift Package Manager를 사용합니다.

```sh
# iOS 시뮬레이터 → 컴퓨터의 localhost:8080
npm run mobile:ios
npm run mobile:ios -- --open
```

Xcode에서 App 타깃을 선택하고 시뮬레이터로 실행합니다. 실제 아이폰에 설치하려면 Signing & Capabilities에서 본인의 Apple Team을 선택하고 기기를 연결해야 합니다. Apple 계정 로그인과 서명 설정은 사용자가 직접 진행합니다.

`Info-Debug.plist`만 로컬 HTTP·Wi-Fi 개발 서버 연결을 허용합니다. Release는 `Info.plist`를 사용하고 개발 서버 예외를 포함하지 않습니다. Xcode Archive도 개발용 화면 번들이 들어 있으면 중단됩니다.

## 실제 휴대폰에서 같은 Wi-Fi로 테스트

휴대폰과 컴퓨터를 같은 Wi-Fi에 연결합니다. 컴퓨터의 Wi-Fi IP를 확인한 후 아래 예시 주소를 자신의 IP로 바꿉니다. 백엔드가 8080 포트에서 외부 연결을 받을 수 있어야 합니다. iOS에서는 로컬 네트워크 접근 요청을 허용합니다.

```sh
npm run mobile:android:apk -- --api-url http://192.168.1.12:8080
npm run mobile:ios -- --api-url http://192.168.1.12:8080 --open
```

주소를 계속 유지하려면 `.env.mobile.example`을 `.env.mobile.local`로 복사해 설정합니다. 이 파일은 버전 관리에서 제외됩니다.

```dotenv
MOBILE_API_BASE_URL=http://192.168.1.12:8080
```

우선순위는 `--api-url` → `.env.mobile.local`/환경 변수 → 개발 모드의 시뮬레이터 기본 주소 순입니다. 앱에 들어가는 주소이므로 비밀번호나 비밀 키를 입력하지 않습니다. 주소 변경 후 다시 빌드·동기화해야 합니다.

## 서버 배포 후 가족들에게 사용하게 하기

외부에서 접속 가능한 HTTPS 백엔드와 DB가 준비되면 아래처럼 개발 옵션 없이 각 플랫폼의 앱 번들을 만듭니다.

```sh
npm run mobile:sync -- android --api-url https://api.example.com
npm run mobile:sync -- ios --api-url https://api.example.com
```

`api.example.com`은 예시이므로 실제 서버 주소로 바꿉니다. 이 명령은 화면을 빌드하고 해당 플랫폼에 복사합니다. `server.url`로 웹사이트를 여는 설정은 사용하지 않습니다. 서명한 APK/AAB 또는 iOS Archive 생성과 배포는 각 플랫폼 도구에서 진행합니다. 서명 키·인증서는 저장소에 넣지 않습니다.

백엔드는 기본적으로 아래의 앱 출처와 기존 웹 개발 출처를 허용합니다.

```text
capacitor://localhost       # iOS
https://localhost          # Android
http://localhost:5187      # 웹 개발 (WishDauary 전용 포트)
http://127.0.0.1:5187       # 웹 개발
```

서버에서 `WISHDIARY_ALLOWED_ORIGINS`를 설정하면 이 목록을 대체합니다. 공개 웹 주소를 추가할 때도 필요한 앱 출처를 함께 유지하세요. 이전 개발 주소인 5173도 호환을 위해 허용합니다. CORS 설정은 로그인 인증을 대체하지 않습니다.

아이폰 가족 배포는 Apple 서명·기기 등록 방식 또는 베타 배포 절차를 별도로 정해야 합니다. [Apple 개발자 계정 안내](https://developer.apple.com/help/account/basics/about-your-developer-account), [TestFlight 안내](https://developer.apple.com/help/app-store-connect/test-a-beta-version/testflight-overview/). TestFlight는 베타 테스트용이며 빌드는 90일 후 만료됩니다.

## 변경 후 확인

```sh
npm test
npm run build
npm run mobile:android
npm run mobile:ios
npm run mobile:icons  # 앱 아이콘 원본 assets/app-icon.svg를 수정했을 때
```

안드로이드 뒤로가기는 상세 화면에서 이전 화면으로 돌아가며, 홈·로그인에서는 앱을 백그라운드로 보냅니다. 키보드가 열린 동안 하단 메뉴를 숨기고, 상태 표시줄·홈 표시기 영역은 safe-area 여백으로 처리합니다.

사진 선택, 키보드, 시스템 뒤로가기, 화면 회전은 실제 Android·iPhone에서도 최종 확인해야 합니다. iOS 컴파일·기기 실행·서명은 Xcode가 있어야 검증할 수 있습니다.

## 가족 연결과 역할

어른 로그인 후에는 `/family-home`으로 이동합니다. 어른용 하단 메뉴는 홈 · 아이 일기 · 상점 관리 · 선물 요청 · 마이이며, 아이 계정의 쓰기·클로버 화면과 구분됩니다. 홈에서는 연결된 아이, 최근 공유 일기, 승인할 선물·가격 설정 요청을 확인합니다. 아이 연결 관리는 홈과 마이페이지에서 열 수 있습니다.

가족 가입을 마치면 `/family-connections`에서 아이 닉네임을 검색하고 연결 요청을 보냅니다. 로그인한 가족만 검색할 수 있으며, 아이의 사용자 번호·닉네임만 보여줍니다. 요청 중에는 가족 권한이 없습니다.

아이는 홈의 요청 안내, 마이페이지 → 가족 연결 요청, 그룹 관리의 연결 요청 링크에서 승인·거절합니다. 승인하면 기본 ‘우리 가족’ 그룹에 추가됩니다. 이 기본 그룹은 직접 만드는 5개 그룹 제한에 포함되지 않습니다. 아이가 직접 그룹에 가족을 추가하는 동의 동작도 기존처럼 지원하며, 대기 중인 요청이 있으면 함께 승인 처리합니다.

어른은 일기를 작성할 수 없습니다. 승인된 연결과 일기 공개 범위에 따라 읽기·하트·댓글을 남기고, `/family-shop`에서 연결된 아이를 골라 선물과 클로버 가격을 등록할 수 있습니다. 가족이 등록한 선물은 선택한 아이의 ‘갖고 싶은 선물’에만 표시됩니다. 선물 요청 처리와 아이가 등록한 선물의 가격 설정은 `/family-settings`에서 합니다.

`/family-shop`에는 아이가 올린 상품과 가족이 등록한 선물이 함께 표시됩니다. 가격이 없는 상품은 ‘클로버 정하기’, 가격을 저장한 상품은 ‘클로버 수정’으로 다시 설정할 수 있습니다. 아이의 ‘갖고 싶은 선물’에는 새 가격이 반영되고, 이미 보낸 선물 요청의 가격·차감·환불 수량은 요청 당시 값으로 유지됩니다. 기본 제공 상품은 이 관리 목록에서 제외합니다.

## 아이의 선물 메뉴

- `/shop` — 공통 상점: 기본 제공 상품을 함께 구경합니다. 이전 `/shop/catalog` 주소도 이 화면으로 연결됩니다.
- `/shop/wishlist` — 갖고 싶은 선물: 가족이 클로버 수를 확인한 선물만 보여줍니다. 확인 전 요청은 이 목록에 표시하지 않습니다.
- `/shop/requests` — 요청 내역: 클로버 설정 요청의 대기·확인 상태와, 모은 클로버로 보낸 선물 요청의 승인·거절 상태를 구분해서 확인합니다.

아이는 선물을 적고 연결된 가족 한 명을 직접 선택해서 요청합니다. 요청을 받은 가족만 `/family-settings`나 `/family-shop`에서 클로버를 정할 수 있습니다. 가족이 1개 이상의 클로버를 정하면 아이의 ‘갖고 싶은 선물’에 표시됩니다. 처음 요청을 보내거나 가족이 수량을 정하는 단계에서는 클로버를 차감하지 않습니다.

기존 미확인 상품도 요청 내역에서 볼 수 있습니다. 이전에 등록한 상품 중 요청받은 가족 정보가 없는 건은 연결된 가족이 확인할 수 있도록 유지하며, 새 요청에는 수신 가족 선택이 필수입니다.

어른이 두 아이와 연결되어 있어도 각 아이의 개인 상품·요청·목표는 서로 섞이지 않습니다. 기존 상품과 요청 기록은 그대로 유지합니다.

연결 해제는 아이가 해당 가족을 모든 그룹에서 제외하는 방식입니다. 연결이 해제되면 그 가족의 일기·소식·상점 관리 권한도 갱신됩니다. 기존 가족 공개·그룹 공개·나만 보기의 공개 범위는 유지됩니다.
