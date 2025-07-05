# Carepick Backend - 병원/의사 추천 애플리케이션 

## 프로젝트 소개
Carepick은 사용자에게 병원 및 의사를 추천하는 의료 정보 제공 서비스입니다. <br>
이 repository는 해당 서비스의 백엔드 서로, Spring Boot를 기반으로 병원/의사 데이터 조회, 질병 및 진료과 예측 결과 수신 등의 역할을 수행합니다.

---

## 🛠️ 기술 스택
### 📌 주요 프레임워크 및 언어
- **Spring Boot 3.4 (Kotlin)**: REST API 서버 구현
- **Spring Data JPA + Hibernate Spatial**: 병원/의사 DB 처리 및 공간 정보 질의
- **Spring WebFlux + WebClient**: Flask 서버와의 비동기 통신
- **Kotlin**: 타입 안정성과 코루틴 기반 비동기 지원
  
### 🧠 데이터 처리 및 분석
- **JTS + Hibernate Spatial**: 병원 위치 기반 거리 계산 및 공간 질의
  
### 🔌 외부 통신 및 유틸리티
- **Flask 서버 연동**: 진료과/질병 예측 결과 수신
- **Jackson (Kotlin Module)**: JSON 직렬화/역직렬화
- **Jsoup / Selenium**: 병원 데이터 수집을 위한 크롤링 로직

### 🧪 테스트 및 개발
- **Junit 5 + kotlin-test**: 단위 테스트
- **H2**: 테스트용 인메모리 데이터베이스
- **Spring Boot Devtools**: 자동 재시작 및 개발 편의성 강화
---

## 주요 기능

## 내 역할

---

## 실행 방법
```bash
# 1. 프로젝트 클론
git clone https://github.com/your-id/carepick-backend.git
cd carepick-backend

# 2. application.yml 설정
# DB 정보, 외부 Flask 서버 주소 등 환경 설정

# 3. 실행
./gradlew bootRun
```
