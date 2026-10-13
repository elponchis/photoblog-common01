# 공통평가 01 작업 순서

보고서 13개 항목을 순서대로 처리한다. 앞 단계 결과가 뒤 단계의 캡처 자료가 되므로 순서를 지킬 것.

## 0. 폴더 구조 만들기

```
common-eval-01/
├── PhotoBlogServer/        기존 web_blog 내용을 그대로 복사
├── PhotoViewer/            Android Studio 프로젝트
└── 모바일웹서비스 프로젝트 공통평가 01_수행 결과 보고서.docx
```

기존 web_blog는 OneDrive 경로에 있으므로 복사해서 옮긴다. `.git` 폴더는 가져오지 않는다.

```powershell
mkdir C:\AndroidStudioProj\common-eval-01
cd C:\AndroidStudioProj\common-eval-01
Copy-Item -Recurse "C:\Users\1124j\OneDrive\Desktop\26-2\모바일웹서비스프로젝트\과제\web_blog" PhotoBlogServer
Remove-Item -Recurse -Force PhotoBlogServer\.git, PhotoBlogServer\venv -ErrorAction SilentlyContinue
```

→ **항목 1 증빙**: 탐색기 또는 GitHub 폴더 구조 캡처

---

## 1. 서버 준비

```powershell
cd PhotoBlogServer
python -m venv venv
.\venv\Scripts\Activate.ps1
pip install -r requirements.txt
python manage.py migrate
python manage.py createsuperuser
python manage.py drf_create_token <아이디>
python manage.py runserver
```

발급받은 토큰은 메모해 둔다. 클라이언트와 telnet 테스트에 쓴다.

---

## 2. 항목 3 — 포토 블로그 API (강의자료 7p)

`http://127.0.0.1:8000/api_root/Post/` 접속 → 하단 HTML form에 입력

- Author, Title, Text, Created date, Published date, Image 채우고 POST

→ **캡처 1**: 작성 완료된 웹페이지 작성 화면 (POST 버튼 누르기 직전)
→ **캡처 2**: 게시 완료된 페이지 화면 (POST 후 201 응답 또는 목록)

---

## 3. 항목 4 — API 검증 2/3, telnet (강의자료 9p)

Windows는 telnet이 기본 비활성이다. 관리자 PowerShell에서 한 번만:

```powershell
dism /online /Enable-Feature /FeatureName:TelnetClient
```

새 터미널에서:

```
telnet 127.0.0.1 8000
```

연결되면 아래를 입력하고 **마지막에 엔터를 한 번 더** 친다.

```
GET /api_root/Post/ HTTP/1.1
Authorization: Token 발급받은토큰
Host: 127.0.0.1:8000
Connection: Keep-Alive
Accept-Encoding: gzip

```

→ **캡처**: Request Line / HTTP Header / Response / Message Body 가 보이는 터미널 화면

telnet 입력이 번거로우면 PowerShell에서 아래로 대체해도 같은 결과가 나온다.

```powershell
curl.exe -v -H "Authorization: Token 토큰" http://127.0.0.1:8000/api_root/Post/
```

---

## 4. 항목 5 — API 검증 3/3, codebeautify (강의자료 10p)

위에서 받은 JSON Message Body를 복사 → https://codebeautify.org/jsonviewer 에 붙여넣기

→ **캡처**: 좌측 원본 / 우측 Beautify 결과. View를 Tree로 바꾼 화면도 함께 있으면 좋다.

---

## 5. 항목 6 — Socket Server로 Client Request 검증 (강의자료 24p)

이전 과제의 `socket_server.py`를 재사용한다. `PhotoBlogServer`와 포트가 겹치므로 Django를 먼저 끈다.

1. `socket_server.py` 실행 (8000 포트)
2. PhotoViewer 앱 → 설정 → 서버 주소를 소켓 서버로 지정 → **동기화** 누름
3. `request/` 폴더에 `.bin` 파일 생성됨
4. 그 파일을 VSCode로 열기

→ **캡처**: VSCode에서 `.bin` 내용(GET /api_root/Post/ HTTP/1.1, Authorization, User-Agent: Dalvik...)이 보이는 화면

User-Agent에 `Dalvik`이 찍혀야 안드로이드에서 온 요청임이 증명된다.

---

## 6. 항목 7·8·9 — Server/Client 연동 테스트 (강의자료 25p)

Django 서버를 다시 켠다. 게시물이 3~4개는 있어야 화면이 보기 좋다.

→ **항목 7 캡처**: 브라우저 `http://127.0.0.1:8000/` 포토 블로그 목록
→ **항목 8 캡처**: 앱 시작 화면 (동기화 전, 목록 비어 있음)
→ **항목 9 캡처**: 동기화 후 이미지 목록이 채워진 화면

실기기에서 돌리면 `10.0.2.2`가 아니라 PC의 실제 IP를 써야 한다. `ipconfig`로 확인한 IPv4 주소를 설정에 입력한다. 이때 Django는 `python manage.py runserver 0.0.0.0:8000`으로 띄우고 `ALLOWED_HOSTS`에 그 IP를 추가한다.

---

## 7. 항목 10 — Upload 구현 (강의자료 19p)

**2) Post에 따른 UI 제공 : 상** 으로 구현되어 있다.

`새로운 이미지 게시` 버튼 → 제목 / 내용 입력 + 이미지 선택 다이얼로그 → 게시
→ multipart/form-data 로 `POST /api_root/Post/` 전송 → 성공 시 자동 재동기화

→ **캡처**: 입력 다이얼로그 화면 + 게시 후 목록에 반영된 화면

---

## 8. 항목 11·12 — PythonAnywhere

PythonAnywhere에도 `tests.py`를 포함한 최신 코드를 올리고 Reload 한다.

서버 쪽에서 토큰을 따로 발급해야 한다(로컬 토큰과 다름).

```bash
python manage.py drf_create_token <아이디>
```

앱 설정 → `클라우드` 프리셋 버튼 → 토큰 교체 → 동기화

→ **항목 11 캡처**: 브라우저로 본 `https://<아이디>.pythonanywhere.com/`
→ **항목 12 캡처**: 클라우드 서버와 동기화된 앱 화면 (설정 화면의 서버 주소가 함께 보이면 더 좋다)

---

## 9. 항목 13 — 편의 기능 (최대 5개)

구현된 5가지. 각각 동작 화면을 캡처한다.

| # | 기능 | 설명 |
|---|---|---|
| 1 | 서버 주소·토큰 설정 | 앱 재빌드 없이 localhost ↔ 클라우드 전환. 항목 11·12 시연에 직접 사용 |
| 2 | 당겨서 새로고침 | 목록을 아래로 당기면 재동기화 |
| 3 | 다운로드 진행률 표시 | 이미지 개수 기준 ProgressBar |
| 4 | 게시물 정보 표시 | 강의자료는 이미지만 보여주나 제목·본문·게시일을 함께 표시 |
| 5 | 이미지 원본 보기 | 목록의 이미지를 누르면 전체 크기로 확인 |

---

## 10. 항목 2 — GitHub push

```powershell
cd C:\AndroidStudioProj\common-eval-01
git init
git add .
git commit -m "feat: 공통평가 01 - PhotoBlogServer, PhotoViewer"
git branch -M main
git remote add origin https://github.com/elponchis/common-eval-01.git
git push -u origin main
```

`.gitignore`에 반드시 포함할 것: `venv/`, `db.sqlite3`, `build/`, `.gradle/`, `.idea/`, `local.properties`, `__pycache__/`

---

## Server API 자동 테스트 (항목 4 관련)

`PhotoBlogServer/blog/tests.py` — 11개 테스트

```powershell
python manage.py test blog -v 2
```

| 클래스 | 테스트 | 내용 |
|---|---|---|
| PostApiTest | 8개 | GET 200, JSON Array 형식, 필수 필드, 허용 메소드, POST 생성, 이미지 저장, 이미지 URL, 제목 누락 시 400 |
| PostListViewTest | 3개 | 목록 페이지 응답, 게시글 노출, 미게시글 비노출 |
