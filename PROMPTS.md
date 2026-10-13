# Claude Code 시작 프롬프트

## 준비

1. zip을 풀어 `C:\AndroidStudioProj\common-eval-01` 에 둔다.
2. Android Studio로 그 폴더를 연다.
3. 하단 Terminal(Alt+F12)에서 `claude` 실행.

`CLAUDE.md`가 같은 폴더에 있으면 Claude Code가 자동으로 읽는다. 과제 배경을 다시 설명할 필요 없다.

---

## 1단계 — 서버 세팅

```
PhotoBlogServer를 준비해줘.

1. 기존 web_blog를 복사한다:
   "C:\Users\1124j\OneDrive\Desktop\26-2\모바일웹서비스프로젝트\과제\web_blog"
   → PhotoBlogServer/
   .git, venv, db.sqlite3 은 제외

2. 지금 PhotoBlogServer/blog/tests.py 에 있는 테스트 파일은 그대로 두고
   복사 과정에서 덮어쓰지 않게 주의해줘.

3. 가상환경 만들고 패키지 설치, migrate, 슈퍼유저 생성, 토큰 발급까지 해줘.
   슈퍼유저는 elponchis / 1124jisoo@gmail.com
   createsuperuser는 대화형 입력에서 인코딩 에러가 나니까
   manage.py shell -c 로 비대화형 생성할 것.

4. python manage.py test blog -v 2 로 11개 테스트가 통과하는지 확인해줘.

5. 발급된 토큰을 알려줘.
```

---

## 2단계 — 안드로이드 프로젝트 생성

이건 GUI 작업이라 직접 해야 한다.

Android Studio → New Project → **Empty Views Activity**

- Name `PhotoViewer`
- Package name `com.example.photoviewer`
- Language **Java**
- Minimum SDK API 24
- Save location `C:\AndroidStudioProj\common-eval-01\PhotoViewer`

만든 뒤:

```
PhotoViewer 안드로이드 프로젝트에 준비된 소스를 적용해줘.

zip에 들어있던 PhotoViewer/app/src/main/ 아래 파일들을
새로 만든 프로젝트의 같은 경로에 배치하면 된다.
res/xml 폴더가 없으면 만들어줘.

그리고:
- build.gradle에 recyclerview, swiperefreshlayout 의존성 추가
- AndroidManifest의 테마 이름을 프로젝트 실제 테마와 맞추기
- MainActivity의 CLOUD_URL을 https://elponchis.pythonanywhere.com 으로

끝나면 ./gradlew assembleDebug 로 빌드되는지 확인해줘.
```

---

## 3단계 — 연동 확인

폰을 USB로 연결하거나 에뮬레이터를 켠 상태에서:

```
adb devices로 기기를 확인하고 앱을 설치해줘.
그리고 Django 서버를 띄워줘.

실기기면 ipconfig로 내 PC IPv4를 알려주고,
runserver 0.0.0.0:8000 으로 띄우고 ALLOWED_HOSTS에 그 IP를 추가해줘.
```

앱에서 **설정** → 서버 주소·토큰·작성자 ID 입력 → **동기화**

---

## 4단계 — 항목별 캡처

항목 3~9, 11~13 순서는 `WORKFLOW.md`에 정리돼 있다. 막히면:

```
WORKFLOW.md의 항목 N을 진행하려고 해. 필요한 명령 알려줘.
```

소켓 서버(항목 6)가 필요할 때:

```
이전 과제의 socket_server.py를 PhotoBlogServer 옆에 복사하고 실행해줘.
Django는 포트가 겹치니 먼저 꺼야 한다.
```

---

## 5단계 — 보고서와 push

캡처를 모두 모은 뒤:

```
캡처 이미지를 모두 모았어. 보고서 docx의 각 항목에 넣고,
.gitignore 만들고 GitHub에 push해줘.
레포는 https://github.com/elponchis/common-eval-01
```

Word 편집은 Claude Code가 python-docx로 처리할 수 있지만, 이미지 위치를 눈으로
확인하는 게 빠르니 직접 넣는 편이 나을 수도 있다.

---

## 팁

- 긴 작업 전에 `/clear` 로 컨텍스트를 비우면 응답이 빨라진다.
- Claude Code가 파일을 고치기 전에 보여주길 원하면 프롬프트에 "수정 전에 계획부터 알려줘" 를 붙인다.
- 빌드 에러는 그대로 복사해 붙이면 된다. 로그를 직접 읽고 고친다.
