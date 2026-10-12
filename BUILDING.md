# 수정 및 재빌드 안내

이 저장소의 최초 릴리스에는 원본 프로젝트 소스가 포함되어 있지 않았습니다. `recovered-source`에는 배포 JAR에서 복원하고 v1.0.2 변경 사항을 반영한 핵심 소스 세 개가 들어 있습니다.

- `Main.java`: 메인 화면과 버전 UI
- `UpdateService.java`: GitHub 업데이트 확인 및 설치
- `RouletteOverlay.java`: 룰렛 오버레이와 효과음

컴파일에는 Java 17과 기존 `SOOP-Collector.jar`가 필요합니다. 수정한 클래스를 컴파일한 뒤 JAR의 `app/soopcollector` 경로에 갱신합니다.

현재 버전은 저장소 루트의 `build.ps1`로 재현할 수 있습니다. 이 스크립트는 v1.0.2 포터블 ZIP을 기준으로 복원 소스와 인증 지원용 soop4j 패치를 Java 17로 컴파일하고, 보안 스모크 테스트를 실행한 뒤 `dist/Kanashi-Collector-v1.1.0-Portable.zip`을 생성합니다.

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

SOOP 로그인 비밀번호는 저장하지 않습니다. 로그인 성공 후 받은 쿠키는 `%USERPROFILE%\.soop-collector\session.dat`에 Windows DPAPI CurrentUser 범위로 암호화해 저장합니다. 이 파일은 빌드 입력이나 ZIP에 포함되지 않습니다.

Windows 설치기는 `jpackage`와 WiX Toolset 3.14로 생성합니다. `installer/main.wxs`에는 설치 완료 직후 `Kanashi Collector.exe`를 실행하는 사용자 정의 동작이 포함되어 있습니다. 설치 옵션은 사용자별 설치, 바탕화면 바로가기, 시작 메뉴 바로가기이며 설치 경로 선택은 비활성화되어 있습니다.

포터블 ZIP에는 Java 런타임이 함께 포함되어 있으므로 압축을 푼 뒤 `Kanashi Collector.exe`를 바로 실행할 수 있습니다.
