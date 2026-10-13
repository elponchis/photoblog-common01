# 모바일/웹서비스 프로젝트 공통평가 01

경희대 컴퓨터공학과 수업 과제. Django 포토 블로그 서버와 안드로이드 클라이언트를 만들고,
13개 평가 항목을 구현·테스트한 뒤 캡처를 Word 보고서에 정리해 GitHub URL을 제출한다.

## 폴더 구조 (고정, 변경 금지)

```
ROOT
├── PhotoBlogServer/     Django Server
├── PhotoViewer/         Android Client
└── 모바일웹서비스 프로젝트 공통평가 01_수행 결과 보고서.docx
```

## 환경

- Windows 11, PowerShell
- Python 가상환경은 `PhotoBlogServer/venv`
- Android Studio, Java (Kotlin 아님), Empty Views Activity
- 안드로이드 패키지명: `com.example.photoviewer`
- GitHub 계정: elponchis
- PythonAnywhere 계정: elponchis

## 서버 구조 (kiokahn/web_blog 기반, 이미 검증됨)

| 항목 | 값 |
|---|---|
| API 경로 | `/api_root/Post/` |
| ViewSet | `blog/views.py` 의 `IntruderImage` |
| 시리얼라이저 필드 | author, title, text, created_date, published_date, image |
| 인증 | `rest_framework.authtoken`, 헤더 `Authorization: Token <40자리>` |
| 권한 | `DEFAULT_PERMISSION_CLASSES` 비어 있음 (토큰 없이도 동작) |
| 페이지네이션 | `PAGE_SIZE`만 있고 클래스 미지정 → 응답이 순수 JSON Array |
| 목록 페이지 `/` | `published_date__lte=now` 인 글만 노출 |
| ALLOWED_HOSTS | `['127.0.0.1', '10.0.2.2', '.pythonanywhere.com']` |
| 이미지 저장 | `media/images/%Y/%m/%d/` |

`python manage.py check` 실행 시 나오는 `rest_framework.W001` 경고는 정상이다.
`DEFAULT_PAGINATION_CLASS`가 없어서 뜨는 것이고, 덕분에 응답이 JSON Array라
안드로이드에서 `JSONArray`로 바로 파싱된다. 고치지 말 것.

## 클라이언트 구조

강의자료(ch05) 14~23페이지 구조를 그대로 따른다. 클래스명·메소드명을 바꾸지 않는다.

- `MainActivity.java` — `onClickDownload`, `onClickUpload`, `onClickSettings`,
  내부 클래스 `CLoadImage`(AsyncTask, 다운로드), `UploadPost`(AsyncTask, 멀티파트 업로드)
- `ImageAdapter.java` — RecyclerView 어댑터
- `PostItem.java` — 게시물 모델 (title, text, publishedDate, bitmap)
- `res/layout/activity_main.xml` — 동기화 / 새로운 이미지 게시 / 설정 버튼, textView, RecyclerView
- `res/layout/item_image.xml` — ImageView + 제목/본문/날짜
- `res/layout/dialog_upload.xml` — 업로드 입력 폼
- `res/layout/dialog_settings.xml` — 서버 주소/토큰/작성자 ID
- `res/xml/network_security_config.xml` — cleartext 허용

`AsyncTask`는 deprecated이지만 강의자료 기준이므로 유지한다. 경고는 무시.

서버 주소·토큰·작성자 ID는 SharedPreferences에 저장되고 앱 내 **설정** 버튼에서 바꾼다.
localhost ↔ PythonAnywhere 전환 때 코드를 고치거나 재빌드하지 않는다.

## 13개 평가 항목

| # | 항목 | 증빙 |
|---|---|---|
| 1 | 폴더 구조 | 탐색기 또는 GitHub 폴더 구조 캡처 |
| 2 | GitHub push | URL |
| 3 | 포토 블로그 API (7p) | 작성 화면 + 게시 완료 화면 |
| 4 | API 검증 2/3 (9p) | telnet 터미널 화면 |
| 5 | API 검증 3/3 (10p) | codebeautify JSON Viewer 화면 |
| 6 | Socket Server 요청 메시지 (24p) | 수신 `.bin` 파일 VSCode 편집 화면 |
| 7 | 연동 테스트 - Server 브라우저 | localhost 블로그 목록 |
| 8 | 연동 테스트 - Client 시작 화면 | 동기화 전 빈 화면 |
| 9 | 연동 테스트 - Client 동기화 완료 | 이미지 목록 채워진 화면 |
| 10 | MainActivity Upload (19p) | **2) Post에 따른 UI 제공(상)** 으로 구현 |
| 11 | PythonAnywhere - 서버 | 클라우드 브라우저 화면 |
| 12 | PythonAnywhere - 클라이언트 | 클라우드 연동된 앱 화면 |
| 13 | 편의 기능 (최대 5개) | 기능 정의 + 동작 캡처 |

## 편의 기능 5개 (항목 13)

1. 서버 주소·토큰 설정 — 재빌드 없이 localhost ↔ 클라우드 전환
2. 당겨서 새로고침 — SwipeRefreshLayout
3. 다운로드 진행률 표시 — 이미지 개수 기준 ProgressBar
4. 게시물 정보 표시 — 강의자료는 이미지만, 여기선 제목·본문·게시일 함께
5. 이미지 원본 보기 — 목록 이미지 탭하면 전체 크기 다이얼로그

## 자동 테스트 (항목 4 관련)

`PhotoBlogServer/blog/tests.py` 에 11개. 실행:

```powershell
python manage.py test blog -v 2
```

## 작업 규칙

- 주석은 꼭 필요한 곳에만. 코드만 봐도 아는 내용은 쓰지 않는다.
- 이모지, 뱃지, 과장된 머리말을 쓰지 않는다. 문서는 담백하게.
- 강의자료에 나온 클래스명·메소드명·레이아웃 ID를 바꾸지 않는다. 채점 기준이다.
- 커밋 메시지는 한 줄, 한국어 또는 conventional commits 형식.
- `.gitignore`: `venv/`, `db.sqlite3`, `__pycache__/`, `build/`, `.gradle/`,
  `.idea/`, `local.properties`, `*.apk`
- `media/` 는 커밋한다. 업로드된 이미지가 증빙이 된다.

## 자주 막히는 지점

- **실기기 사용 시** `10.0.2.2`는 에뮬레이터 전용. `ipconfig`로 PC의 IPv4를 확인해
  앱 설정에 넣고, 서버는 `runserver 0.0.0.0:8000`, `ALLOWED_HOSTS`에 그 IP 추가.
- **telnet** Windows 기본 비활성. 관리자 PowerShell에서
  `dism /online /Enable-Feature /FeatureName:TelnetClient` 한 번 실행.
- **PowerShell의 `curl`** 은 `Invoke-WebRequest` 별칭이라 `-F` 가 안 먹는다.
  반드시 `curl.exe` 로 호출.
- **PythonAnywhere** Python 3.13 가상환경을 쓴다. 3.10이면 Django 6.x 설치 실패.
- **토큰은 로컬과 클라우드가 다르다.** 각각 `drf_create_token` 으로 발급.

## 이 폴더의 문서

- `WORKFLOW.md` — 13개 항목 작업 순서와 캡처 목록
- `PROMPTS.md` — Claude Code용 단계별 프롬프트
- `PhotoViewer/SETUP.md` — 안드로이드 프로젝트 생성·의존성 설정

- 커밋 메시지에 Co-Authored-By 나 Generated with Claude Code 같은 줄을 절대 넣지 않는다.
