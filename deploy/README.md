# EC2 한 대에 다유어리 배포

Ubuntu 24.04 LTS · x86_64 · t3.small(2GB) · gp3 30GiB 기준입니다. Docker Engine과 Compose 플러그인이 필요하며 Node.js, Java, PostgreSQL을 호스트에 별도로 설치하지 않습니다.

```text
브라우저 → EC2 80/443 → Caddy ┬ /api/* → Spring Boot → PostgreSQL
                              └ 나머지 → Nginx / React
```

외부 포트는 Caddy의 80·443만 게시합니다. 백엔드 8080과 DB 5432는 Compose 네트워크 안에서만 접근합니다. PostgreSQL 데이터와 HTTPS 인증서는 이름 있는 Docker 볼륨에 저장합니다. 컨테이너는 서버 재부팅 후 자동으로 시작하며 로그 용량도 제한합니다.

## 1. EC2 준비

- Ubuntu Server 24.04 LTS, Canonical 제공, x86_64, t3.small, gp3 30GiB 암호화.
- 기본 VPC의 인터넷 연결 가능한 서브넷, 퍼블릭 IPv4 활성화.
- 보안 그룹: SSH 22는 내 IP, HTTP 80·HTTPS 443은 인터넷 허용.
- CPU 크레딧 Standard를 사용하면 추가 CPU 크레딧 비용 없이, 크레딧 소진 시 CPU 성능이 제한됩니다.
- 무료 플랜도 서버·디스크·공인 IP 사용량만큼 크레딧이 차감됩니다.

Mac에서 접속합니다. `서버IP`는 EC2의 퍼블릭 IPv4로 바꿉니다.

```sh
chmod 400 ~/Downloads/wishdauary-key.pem
ssh -i ~/Downloads/wishdauary-key.pem ubuntu@서버IP
```

## 2. Docker 설치

아래 명령은 새 Ubuntu 서버에서 실행합니다. 기존 Docker가 설치돼 있다면 [공식 설치 안내](https://docs.docker.com/engine/install/ubuntu/)에서 충돌 패키지 여부부터 확인합니다.

```sh
sudo apt-get update
sudo apt-get install -y ca-certificates curl git openssl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

sudo tee /etc/apt/sources.list.d/docker.sources > /dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "$VERSION_CODENAME")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker
sudo docker compose version
```

2GB 서버에서는 빌드 중 메모리 부족을 줄이기 위해 swap 2GB를 권장합니다. 먼저 `swapon --show`로 확인하고, swap이 없는 새 서버에서만 다음을 한 번 실행합니다.

```sh
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
printf '/swapfile none swap sw 0 0\n' | sudo tee -a /etc/fstab
```

swap은 메모리가 부족할 때 디스크를 사용하므로 메모리 증설과 같은 성능은 아닙니다. 컨테이너별 메모리 상한과 Java 힙 상한은 `compose.yaml`에 지정했습니다. 빌드는 아래처럼 하나씩 실행합니다.

## 3. 코드와 환경 파일 준비

배포할 변경 내용이 GitHub에 반영된 커밋을 사용합니다. 로컬에서만 수정한 파일은 `git clone`으로 가져오지 못합니다. 이미 체크아웃한 작업 폴더가 있다면 clone 대신 그 폴더로 이동합니다.

```sh
git clone https://github.com/ppepeppe/wishdauary.git
cd wishdauary
sh deploy/init-env.sh
nano .env
```

`init-env.sh`는 DB 비밀번호와 로그인 토큰 비밀키를 각각 무작위로 생성하고 `.env`를 소유자만 읽고 쓸 수 있게 만듭니다. 기존 `.env`는 덮어쓰지 않습니다. 실제 `.env`는 Git에서 제외됩니다.

도메인이 아직 없으면 초기 연결 확인용으로 다음 두 값을 사용합니다. `YOUR_EC2_PUBLIC_IP`를 실제 IP로 바꿉니다.

```dotenv
WISHDIARY_PUBLIC_ORIGIN=http://YOUR_EC2_PUBLIC_IP
WISHDIARY_SITE_ADDRESS=:80
```

`PUBLIC_ORIGIN`에는 경로나 마지막 `/` 없이 브라우저에서 접속할 정확한 주소를 넣습니다. HTTP 접속은 설치 확인용이며 실제 가족 계정과 일기는 HTTPS 연결 후 사용합니다.

기본 EC2 공인 IP는 중지 후 다시 시작하면 바뀔 수 있습니다. 주소가 바뀌면 `.env`와 도메인 DNS도 갱신합니다. 고정 주소가 필요하면 Elastic IP를 연결하고, 할당해 둔 동안 별도 IP 요금이 발생함을 확인합니다.

## 4. 빌드와 실행

프로젝트 루트에서 실행합니다. `config --quiet`는 비밀값을 화면에 출력하지 않고 설정만 검사합니다.

```sh
sudo docker compose config --quiet
sudo docker compose build backend
sudo docker compose build frontend
sudo docker compose up -d --wait --wait-timeout 180
sudo docker compose ps
sudo docker compose logs --tail=100 backend
```

첫 빌드는 이미지·Gradle·npm 의존성 다운로드 때문에 몇 분 이상 걸릴 수 있습니다. PostgreSQL 준비를 기다린 뒤 백엔드가 시작하고 Flyway가 테이블을 생성합니다. 백엔드는 로그인 없는 `/api/v1/users/me` 요청이 예상대로 `401`을 반환하는지 확인하고, 준비된 뒤 프론트·프록시를 시작합니다. `--wait`는 DB·백엔드·프론트의 healthcheck를 확인합니다.

EC2 안에서 실행합니다. 시작 직후에는 Spring Boot 준비에 시간이 걸릴 수 있습니다.

```sh
curl -I http://127.0.0.1/
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1/api/v1/users/me
```

첫 요청은 `200`, 두 번째는 로그인 토큰이 없으므로 `401`이면 정상입니다. `502`라면 백엔드 로그에서 시작 완료·DB 연결 오류를 확인합니다. 브라우저에서 `http://서버IP`와 `/login`을 열어 화면도 확인합니다. 80 포트를 변경했다면 curl 주소에 변경한 포트를 붙입니다.

새 볼륨은 빈 DB로 시작합니다. 기존 로컬 계정·일기·사진은 자동으로 옮겨지지 않습니다. 이전하려면 아래 복원 절차를 사용합니다. 새로 가입한 계정은 기본 일반 사용자이며 관리자 지정은 [루트 README](../README.md#공통-상점-관리)의 SQL을 사용합니다.

## 5. 도메인과 HTTPS

사용할 공개 도메인의 A 레코드를 EC2 공인 IPv4로 지정합니다. AAAA 레코드는 실제 IPv6 연결을 구성한 경우에만 지정합니다. DNS가 반영되고 외부에서 80·443 포트에 접근 가능해야 합니다.

`.env`의 두 값을 함께 바꿉니다. `SITE_ADDRESS`에는 프로토콜이나 경로 없이 호스트명만 넣습니다.

```dotenv
WISHDIARY_PUBLIC_ORIGIN=https://diary.example.com
WISHDIARY_SITE_ADDRESS=diary.example.com
```

```sh
sudo docker compose up -d
sudo docker compose logs --tail=100 proxy
curl -I https://diary.example.com/
```

Caddy가 공개 인증서를 자동 발급·갱신하고 HTTP를 HTTPS로 전환합니다. 인증서 데이터는 `caddy_data` 볼륨에 보관합니다. IP나 `localhost`만 넣는 것은 신뢰되는 공개 도메인 HTTPS 설정과 다릅니다. [Caddy HTTPS 조건](https://caddyserver.com/docs/automatic-https)

현재 요청에 따라 관리자 상품 API에는 로그인 확인만 있고 ADMIN 인가는 유예된 상태입니다. 일반 로그인 계정도 직접 API를 호출하면 공통 상품을 등록할 수 있습니다. 외부 사용자에게 가입을 개방하기 전 이 정책을 보완해야 합니다.

## 6. 업데이트·정지

DB 백업을 먼저 만든 뒤 배포할 커밋을 받습니다. 현재 변경되지 않은 서비스를 포함해 컨테이너를 다시 만들더라도 DB 볼륨은 유지됩니다.

```sh
sudo sh deploy/backup-db.sh
git pull --ff-only
sudo docker compose build backend
sudo docker compose build frontend
sudo docker compose up -d --wait --wait-timeout 180
```

일시 정지는 `sudo docker compose stop`, 재시작은 `sudo docker compose up -d`입니다. `sudo docker compose down`도 볼륨은 유지합니다. **`down -v`와 볼륨 prune은 저장된 DB·인증서를 지우므로 실행하지 않습니다.** 서버를 중지해도 EBS와 할당된 고정 IP 등은 비용이 계속 발생할 수 있습니다.

## 7. DB 백업과 이전

다음 명령은 일기·계정·선물·사진을 포함한 전체 DB를 `backups/` 아래 custom dump로 저장합니다. 실패한 백업은 최종 파일로 남기지 않습니다.

```sh
sudo sh deploy/backup-db.sh
```

서버 내부 백업만으로는 디스크 손실에 대비할 수 없으므로 생성된 `.dump`와 `.env`를 접근이 제한된 별도 장소에도 보관합니다. dump에는 가족 개인정보가 포함되며 Git에 추가하지 않습니다. `.env`를 분실하면 기존 DB 비밀번호와 토큰 서명키를 복구할 수 없습니다. 기존 DB를 만든 후 `.env`의 DB 비밀번호만 바꾸면 DB 자체 비밀번호는 바뀌지 않아 연결에 실패합니다.

기존 로컬 Docker PostgreSQL에서 이전용 백업을 만들려면 로컬 컴퓨터에서 실행합니다.

```sh
umask 077
docker exec wishdiary-postgres pg_dump -U postgres -d wishdiary -Fc --no-owner --no-acl > wishdauary-local.dump
scp -i ~/Downloads/wishdauary-key.pem wishdauary-local.dump ubuntu@서버IP:~/
```

복원은 **새 배포용 DB에 기존 로컬 데이터를 넣는 절차**입니다. 기존 서버 DB에 실행하면 같은 테이블을 교체하므로 먼저 백업합니다. 애플리케이션을 멈춘 상태에서 실행합니다. 이전 DB도 현재 앱이 사용하는 마이그레이션 버전과 호환돼야 합니다.

```sh
sudo docker compose stop proxy frontend backend
sudo docker compose up -d --wait postgres
sudo docker compose exec -T postgres pg_restore \
  -U wishdauary -d wishdauary --clean --if-exists --no-owner --no-acl --exit-on-error \
  < ~/wishdauary-local.dump
sudo docker compose up -d --wait --wait-timeout 180
```

마이그레이션 기록도 dump에 포함됩니다. 사진이 BYTEA로 저장되므로 별도 업로드 폴더 이전은 필요 없습니다. 로컬 테스트 계정과 관리자 지정도 그대로 복원되므로 운영 전 확인합니다.

## 로컬에서 배포 구성만 확인

기존 개발 서버·로컬 PostgreSQL과 분리하려면 별도 작업 폴더에서 `.env`를 만들고 다음 값을 사용합니다. 프로젝트 이름을 바꿔도 동일한 `.env`를 사용하면 DB·서명키는 공유되므로 테스트 전용 `.env`를 만듭니다.

```dotenv
WISHDIARY_PUBLIC_ORIGIN=http://localhost:18087
WISHDIARY_SITE_ADDRESS=:80
WISHDIARY_BIND_ADDRESS=127.0.0.1
WISHDIARY_HTTP_PORT=18087
WISHDIARY_HTTPS_PORT=18443
```

`docker compose -p wishdauary-review ...`처럼 별도 프로젝트 이름으로 실행하면 볼륨·네트워크가 분리됩니다. Caddy가 게시한 `http://localhost:18087`로 확인합니다. 이미지는 현재 호스트 아키텍처로 빌드되므로 Mac ARM 이미지를 그대로 EC2 x86에 복사하지 말고 서버에서 빌드합니다.
