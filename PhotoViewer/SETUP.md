# PhotoViewer 프로젝트 설정

## 프로젝트 생성

Android Studio → New Project → **Empty Views Activity**

- Name: `PhotoViewer`
- Package name: `com.example.photoviewer`
- Language: **Java**
- Minimum SDK: API 24
- Save location: `C:\AndroidStudioProj\common-eval-01\PhotoViewer`

Kotlin이 아니라 Java다. 강의자료가 Java 기준이다.

## build.gradle.kts (Module :app)

`dependencies` 블록에 아래 두 줄이 없으면 추가한다.

```kotlin
dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}
```

groovy 방식(`build.gradle`)이면 따옴표 없이:

```groovy
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
```

추가 후 상단에 뜨는 **Sync Now** 클릭.

## 파일 배치

```
app/src/main/
├── AndroidManifest.xml                          덮어쓰기
├── java/com/example/photoviewer/
│   ├── MainActivity.java                        덮어쓰기
│   ├── ImageAdapter.java                        새로 추가
│   └── PostItem.java                            새로 추가
└── res/
    ├── layout/
    │   ├── activity_main.xml                    덮어쓰기
    │   ├── item_image.xml                       새로 추가
    │   ├── dialog_upload.xml                    새로 추가
    │   └── dialog_settings.xml                  새로 추가
    └── xml/
        └── network_security_config.xml          새로 추가
```

`res/xml` 폴더가 없으면 `res`에서 우클릭 → New → Android Resource Directory → Resource type: `xml`

## MainActivity 안에서 바꿀 값

```java
private static final String CLOUD_URL = "https://elponchis.pythonanywhere.com";
```

본인 PythonAnywhere 주소로. 다른 설정값(서버 주소, 토큰, 작성자 ID)은 앱 실행 후 **설정** 버튼에서 바꾸므로 코드를 고칠 필요 없다.

## 테마 이름

`AndroidManifest.xml`의 `android:theme="@style/Theme.PhotoViewer"` 는 프로젝트 생성 시 만들어진 테마 이름과 같아야 한다. `res/values/themes.xml`을 열어 확인하고 다르면 맞춘다.

## 첫 실행 순서

1. Django 서버 실행 후 토큰 발급
2. 앱 실행 → **설정** → 서버 주소 `http://10.0.2.2:8000` (에뮬레이터) 또는 PC 실제 IP (실기기), 토큰 입력, 작성자 ID 입력
3. **동기화** 누름
