# Carepick Backend

> 증상 기반 질병/진료과 예측 및 위치 기반 병원·의사 탐색 플랫폼 백엔드

## 프로젝트 개요

- **주제**: 사용자의 자연어 증상 입력을 기반으로 질병 및 진료과를 예측하고, 위치 기반으로 적합한 병원과 의사를 추천하는 의료 정보 서비스
- **기간**: 2024.09.02 ~ 2025.12.11
- **역할**: 프로젝트 기획, DB 모델링, 백엔드 전체 구현 (단독)

## 기술 스택

| 영역 | 기술 |
|------|------|
| Language | Kotlin |
| Framework | Spring Boot 3.4 |
| ORM | Spring Data JPA + Hibernate Spatial |
| 비동기 통신 | Spring WebFlux + WebClient |
| 공간 질의 | JTS (Java Topology Suite) |
| 크롤링 | Selenium + Jsoup |
| 외부 API | Naver Geolocation API, OpenAI GPT-4o |
| 인증 | JWT + OAuth 2.0 (Google, Kakao, Naver, Apple) |
| DB | MySQL |
| 테스트 | JUnit 5, H2 |

## 프로젝트 구조

```
src/main/kotlin/com/callrapport/
├── application/          # Spring Boot 진입점
├── config/               # Naver API 설정, WebSocket 설정
├── component/
│   ├── calculator/       # 의사 학력 점수 계산 엔진
│   ├── client/           # OAuth 클라이언트 (Google, Kakao, Naver, Apple)
│   ├── crawler/          # 병원/의사/대학 순위 크롤러 (Selenium + Jsoup)
│   ├── extractor/        # HTML에서 병원/의사 정보 추출
│   ├── jwt/              # JWT 토큰 발급 및 검증
│   ├── log/              # WebSocket 기반 실시간 로그 브로드캐스터
│   └── map/              # 주소 → 위도/경도 변환 (Naver Geolocation)
├── controller/           # REST API 엔드포인트
├── service/              # 비즈니스 로직
├── repository/           # JPA 레포지토리
├── model/                # 도메인 엔티티
└── dto/                  # 요청/응답 객체
```

## 주요 기능

### 1. 자가진단 기반 질병/진료과 예측

사용자의 자연어 증상 입력을 받아 Flask(PyTorch) 예측 서버와 GPT-4o를 결합한 이중 파이프라인으로 질병 및 진료과를 예측합니다.

```
사용자 증상 입력
      ↓
Flask 서버 (PyTorch 모델) → Top-K 예측 + score
      ↓
score ≥ 0.4 → DB 질병-진료과 조회 → 결과 반환
score < 0.4 → GPT-4o Fallback → 질병명 추출 → 진료과 추출 → 결과 반환
```

- Flask 서버 응답의 신뢰 점수(score)가 0.4 미만이면 GPT-4o로 재진단
- DB에 없는 질병이거나 진료과 정보가 없는 경우에도 GPT-4o로 보완
- GPT-4o 실패 시 Flask 모델 결과로 Fallback 처리

### 2. 위치 기반 병원 탐색

JTS + Hibernate Spatial을 활용하여 사용자 위치 기준 거리 계산 및 복합 필터 검색을 지원합니다.

**지원 필터**

| 필터 | 설명 |
|------|------|
| keyword | 병원명 부분 검색 |
| location + maxDistanceKm | 위도/경도 기준 반경 내 병원 |
| specialties | 진료과 필터 |
| selectedDays | 요일 필터 (예: 월, 토) |
| startTime / endTime | 운영 시간 필터 |
| sortBy | distance 또는 name 정렬 |

### 3. 의사 신뢰도 점수 산출

주관적 리뷰 점수 대신 학력 기반 객관적 신뢰도 지표를 설계했습니다.

**점수 산출 공식**

```
최종 점수 = 기본 점수 × 상태 가중치 × (1000 / 대학 순위)
```

**학력 유형 분류**

| 유형 | 키워드 | 기본 점수 |
|------|------|------|
| 학위 | 석박사(5), 박사(3), 석사(2), 학사(1) | 졸업 1.0 / 수료 0.6 / 과정 0.5 |
| 수련 | 전공의(4), 레지던트(3), 수련의(2), 인턴(1) | 과정 0.5 / 그 외 1.0 |
| 직책 | 임상 부교수(5), 조교수(4), 전임의(3), 임상강사(2) | 항상 1.0 |

- Times Higher Education 대학 순위를 크롤링하여 한글/영문명 매칭
- 매칭 실패 시 최하위 순위로 처리

### 4. 병원/의사 데이터 수집 (크롤링)

HiDoc 웹사이트를 대상으로 Selenium + Jsoup 기반 크롤링 파이프라인을 구축했습니다.

```
HiDoc 병원 목록 페이지 (Selenium)
      ↓
병원별 링크 수집 (페이지네이션 자동 처리)
      ↓
병원 상세 페이지 크롤링
→ 병원명, 전화번호, 주소, 진료과, 운영시간, 부가정보
      ↓
병원 내 의사 URL 추출 → 의사 상세 정보 크롤링
→ 의사명, 진료과, 경력, 학력/자격면허
      ↓
Naver Geolocation API → 주소 → 위도/경도 변환 → DB 저장
```

### 5. 인증 시스템

JWT + OAuth 2.0 기반 소셜 로그인을 지원합니다. (Google, Kakao, Naver, Apple)

### 6. 행정구역 기반 탐색

시도 → 시군구 → 읍면동 단위 행정구역 필터링으로 지역별 병원/의사 분포 탐색을 지원합니다.

## DB 모델링

```
Hospital ─── HospitalDoctor ─── Doctor
    │                               │
HospitalSpecialty           DoctorSpecialty
    │                               │
HospitalOperatingHours      DoctorEducationLicense ── EducationLicense
    │                               │
HospitalAdditionalInfo      DoctorCareer ── Career
    │
HospitalImage

Disease ── DiseaseSpecialty ── Specialty
       └── DiseaseCategory ── Category
       └── DiseaseBodySystem ── BodySystem

UniversityRank ── UniversityRankRegion ── Region

User ── UserProfile ── Profile ── ProfileAddress ── Address
     └── UserOauth ── Oauth
     └── UserFavoriteHospital
     └── UserFavoriteDoctor
```

## 핵심 기술적 결정

**1. Flask + GPT-4o 이중 파이프라인**

단일 모델만 사용할 경우 신뢰 점수가 낮은 예측에 대한 대응이 불가합니다. score 임계값(0.4)을 기준으로 Flask 모델과 GPT-4o를 결합하여 예측 정확도와 커버리지를 동시에 확보했습니다.

**2. 주관적 리뷰 대신 학력 기반 신뢰도 지표 설계**

리뷰 점수는 개인 편차가 크고 기준이 불일치합니다. Times Higher Education 대학 순위와 학위/수련/직책 분류 체계를 결합하여 수치화 가능한 객관적 신뢰도 지표를 직접 설계했습니다.

**3. Hibernate Spatial + JTS 공간 질의**

일반적인 위도/경도 계산은 정확도가 낮습니다. SRID 4326 좌표계 기반 Point 타입과 Hibernate Spatial의 공간 인덱스를 활용하여 거리 계산 정확도와 쿼리 성능을 확보했습니다.

**4. HospitalField enum 기반 선택적 크롤링**

전체 필드를 항상 크롤링하면 불필요한 리소스가 소모됩니다. `HospitalField` enum으로 필요한 필드만 선택적으로 추출하여 크롤링 효율을 높였습니다.

## 실행 방법

```bash
# 1. 프로젝트 클론
git clone https://github.com/cbnu-development-team-tuktak/carepick-backend.git
cd carepick-backend

# 2. application.yml 환경 설정
# DB 접속 정보, Naver API 키, OpenAI API 키, Flask 서버 주소 설정

# 3. 실행
./gradlew bootRun
```
