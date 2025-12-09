# Ruto – 나만의 루틴 관리 & 일상 코칭 App ⏰

> Android / Kotlin / Jetpack Compose 기반의 **루틴 관리 & 알림 App** 입니다.  
> 매일 반복되는 루틴을 설정하고, 알림/통계를 통해 지속적인 실천을 돕습니다.


<br>

##  주요 기능 (Features)

- **루틴 관리**
  - 요일/시간별 루틴 생성 및 수정
  - 하루 여러 개의 루틴 등록 가능
- **알림 (푸시)**
  - 지정한 시간에 FCM 기반 알림 발송
  - 루틴 완료 여부를 빠르게 체크
- **통계 / 기록 보기**
  - 한 달 단위 루틴 완료 현황 확인
  - 출석/완료 비율을 통해 습관 유지 상황 파악
- **프로필 관리**
  - 닉네임 설정 (기본값: `미설정 닉네임`)
  - Supabase Storage + Coil 기반 **프로필 이미지 업로드**
- **게스트 & 소셜 로그인**
  - Google / Kakao 소셜 로그인
  - 게스트 로그인 모드 지원 (프로필 수정 제한)
- **다크 모드 / 테마 설정**
  - 시스템 설정 / 라이트 / 다크 모드 선택 가능


<br>

##  스크린샷 (Screenshots)


| 메인 루틴 목록 | 루틴 통계 | 설정 / 프로필 |
| ------------- | --------- | ------------- |
| <img src="https://github.com/user-attachments/assets/3a137c20-ec60-46b0-bdd9-f9445469af15" width="260" /> | <img src="https://github.com/user-attachments/assets/cdcc381d-e87f-4b23-bd29-cda4275f0f11" width="260" /> | <img src="https://github.com/user-attachments/assets/19b87c2c-f0c0-405e-aac5-d96c4e8f3765" width="260" /> |




<br>

## 🏗 기술 스택 (Tech Stack)

**Client (Android)**

- Language: **Kotlin**
- UI: **Jetpack Compose**, Material 3
- Architecture:
  - MVVM, Clean-ish Architecture
  - `ViewModel` + `StateFlow` 기반 상태 관리
- DI: **Hilt**
- 비동기: **Coroutine / Flow**
- 데이터:
  - Room (로컬 DB, 루틴/기록 캐싱)
  - DataStore (테마/설정 값 저장)
- 백엔드 연동:
  - **Supabase**
    - Auth (구글, 카카오 소셜 로그인 / 게스트)
    - PostgREST (루틴/프로필/기록 API)
    - Storage (프로필 이미지)
  - Ktor Client (HTTP 통신)
- 알림:
  - Firebase Cloud Messaging (FCM)
  - WorkManager (백그라운드 작업)
- 이미지:
  - **Coil** (프로필/이미지 로딩 및 캐싱)


<br>

##  아키텍처 개요 (Architecture)

간단히 크게 세 레이어로 나뉩니다.

- **domain**  
  - `routine`, `profile`, `fcm` 등 핵심 도메인 모델(`RoutineModels`, `UserProfile`, `MonthlyCompletionsModels` 등)을 정의합니다.
  - UI / Data 모듈에 의존하지 않는 **순수 Kotlin 레이어**로, 비즈니스 규칙과 타입 정의에 집중합니다.

- **data**  
  - `auth`, `routine`, `profile`, `statistics`, `setting`, `fcm`, `notification`, `sync`, `local` 등 실제 데이터 소스를 다루는 구현 레이어입니다.
  - Room 기반 로컬 DB(`local.*`), 네트워크 API / Supabase 연동(`routine`, `auth`, `profile`), FCM 토큰 관리(`fcm`, `notification`), DataStore 기반 설정 저장(`setting`) 등을 포함합니다.
  - `UserProfileEntity` 같은 **Entity/DTO ↔ Domain 모델 변환**을 담당하며, 상위 레이어에서는 `ProfileRepository`, `RoutineRepository` 등 **Repository 인터페이스**를 통해 접근합니다.

- **ui**  
  - `auth`, `routine`, `statistics`, `setting`, `home` 등 **Jetpack Compose 기반 Screen** 과 `ViewModel` 이 위치하는 레이어입니다.
  - `UiState`, `UiEvent` 를 중심으로 `StateFlow` / `SharedFlow` 를 사용해 **단방향 데이터 흐름(UDF)**을 구성합니다.
  - 테마(`theme`), 공용 컴포넌트/유틸(`ui.util`, `ClickEffect`, `PickerDialog` 등)은 재사용 가능한 UI 레벨 기능으로 분리되어 있습니다.

디렉터리 구조:

```text
app/src/main/java/com/handylab/ruto
├── auth
├── data
│   ├── auth
│   ├── fcm
│   ├── local
│   │   ├── complete
│   │   ├── routine
│   │   └── statistics
│   ├── notification
│   ├── profile
│   ├── routine
│   ├── security
│   ├── setting
│   ├── statistics
│   │   └── model
│   └── sync
├── di
├── domain
│   ├── fcm
│   ├── profile
│   └── routine
├── ui
│   ├── auth
│   ├── event
│   ├── home
│   ├── permission
│   ├── routine
│   │   └── edit
│   ├── setting
│   │   └── profile
│   ├── state
│   ├── statistics
│   ├── theme
│   └── util
├── util
└── workManager
