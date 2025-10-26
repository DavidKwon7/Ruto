# Ruto — 루틴/알림 앱 (Android + Supabase + FCM)

#### 루틴 생성/수정/삭제, 푸시 알림, 월간 통계를 제공하는 Android App

Android: Jetpack Compose / Room / WorkManager / Ktor / Hilt, 

백엔드: Supabase (Postgres + Edge Functions + Storage) / FCM 

### 주요 기능
루틴 CRUD: 생성/수정/삭제, 주기(D/W/M/Y), 알림 시간, 태그

푸시 알림: Edge Function + Cron + FCM

오프라인/낙관적 UI:

- Room 캐시로 즉시 표시

- WorkManager로 “루틴 완료”를 백그라운드 업로드

- 계정/게스트 별 ownerKey를 통해 데이터 분리

월간 히트맵: 날짜별 완료율 + 루틴별 일자 완료 상태

게스트/로그인 동시 지원: Authorization(로그인) 또는 X-Guest-Id(게스트)

### TODO 
- [ ] UI 테마 / 접근성 개선
- [ ] 다국어 / 현지화
- [ ] 주기 / 요일 세분화
- [ ] 프로필 생성
- [ ] 루틴 소셜 기능 추가
