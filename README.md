## 프로젝트 소개 만화
![title](https://private-user-images.githubusercontent.com/74519471/415958525-46f09526-4eda-44e1-b193-a8b1118d61db.png?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NDAyODY2NjcsIm5iZiI6MTc0MDI4NjM2NywicGF0aCI6Ii83NDUxOTQ3MS80MTU5NTg1MjUtNDZmMDk1MjYtNGVkYS00NGUxLWIxOTMtYThiMTExOGQ2MWRiLnBuZz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTAyMjMlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUwMjIzVDA0NTI0N1omWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPTA3OGU3NWRlNTk3ZjkzMGUwOTI2ZTk3NjEwNzdjZDdkNjZhMjI5NGI2ZTY4ZWZmMjVlMDY1MzFkNjliYTQ3MGYmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.bSrm3mOWyxHvsxa0HxkioziCOZ1bYfQ6-eoWUHFkN4w)

## 프로젝트 개요
(2025.02.23. 기준) <br/>
백엔드는 질병관리청 국가건강정보포털에서 제공하는 질병 정보를 크롤링하여 가공하고, 이를 데이터베이스에 저장하는 기능을 수행합니다.

추후 하이닥에서 제공하는 의사·병원 정보를 크롤링하는 기능도 구현 예정입니다. 
1. 질병 정보 크롤링
    - 병명과 해당 질병의 상세 페이지 URL을 매핑
    - 각 질병의 상세 정보를 크롤링
2. 데이터 가공 및 정제
    - 개요, 정의, 증상, 원인 등의 정보를 정리
    - 증상 목록을 리스트 형식으로 변환
    - 중복되거나 불필요한 내용 필터링
3. 데이터베이스 저장 (향후 지원 예정)

## 사용 기술  
- Kotlin, Spring Boot
 -  크롤링 라이브러리
    - Selenium (동적 페이지 조작)
    - Jsoup (HTML 파싱 및 데이터 추출)
- 데이터 저장: CSV(테스트 및 가공 전) → MySQL (최종 저장)
- 로그 관리: println()을 통한 기본 로깅 (추후 로깅 프레임워크 적용 가능)

## 질병 크롤링 방식
### 질병 링크 크롤링
1. Selenium WebDriver를 사용하여 국가건강정보포털에 접속
2. 건강문제 버튼을 통해 질병 관련 데이터만을 필터링
3. 각 질병의 고유 ID를 포함한 JavaScript 링크만 추출 <br/>
(예: javascript:fn_goView('6549', '만성피로증후군');
4. 정규 표현식을 통해 해당 ID를 파싱하고, 최종적으로 질병 링크를 생성 <br/> (예: https://health.kdca.go.kr/healthinfo/.../gnrlzHealthInfoView.do?cntntns_sn=6549)
### 질병 상세 정보 크롤링
1. Jsoup을 이용해 각 질병 상세 페이지 HRML을 파싱
2. 주요 정보를 추출
    - name (질병명)
    - overview (개요)
    - definition (정의)
    - type (질병 종류)
    - cause (원인)
    - symptoms (증상)
    - FAQ (자주 묻는 질문)
    - url (출처)
      
## 데이터 정제 및 저장
1. 질병명, 개요, 정의, 증상, 원인, FAQ, URL 컬럼으로 CSV를 저장
2. 데이터 검증 및 2차 수정
    - 개요(overview): "~은 ...한 질병입니다." 형식으로 통일
        - 이때, 개요 문장이 비정상적인 경우, 2차 수정 완료 컬럼을 추가해 O/X로 체크
    - 증상(symptoms): 숫자 리스트 형태로 정리 <br/>
(예: 1. 피로감, 2. 근육통, 3. 기억력 저하)

## 실행 방법
1. 크롤러 실행 (Spring Boot)
```js
./gradlew bootRun
```
2. API 엔드포인트 (REST API)

| 엔드포인트 | 설명 |
| --- | --- |
| /api/crawl/disease/links | 크롤링한 질병 목록을 반환 |
| /api/crawl/disease/infos | 질병 상세 정보를 반환 |
| /api/crawl/disease/csv | CSV 파일로 저장 후 다운로드 |

## 데이터베이스 확인 방법 (CMD)

### 1. MySQL 클라이언트로 접속하기

다음 명령어를 통해 MySQL DB 접속 가능
```bash
mysql -h callrapport.cxu4scag8p2u.ap-northeast-2.rds.amazonaws.com -P 3306 -u admin -p
```
비밀번호는 project의 application.properties를 확인
```
Enter password: *********
```

### 2. 데이터베이스 선택
MySQL에 접속한 후, callrapport 데이터베이스 선택
```sql
USE callrapport;
```

### 3. 테이블 확인
현재 데이터베이스에 존재하는 테이블 확인
```sql
SHOW TABLES;
```
```sql
+---------------------------+
| Tables_in_callrapport     |
+---------------------------+
| additional_info           |
| doctor_education_licenses |
| doctor_hospitals          |
| doctors                   |
| hospital_additional_info  |
| hospital_doctors          |
| hospital_specialties      |
| hospitals                 |
| specialties               |
+---------------------------+
```

### 4. 데이터 조회
특정 테이블에서 데이터를 조회하기
```sql
SELECT * FROM hospitals;
```
```sql
+-------------+-------------------+-------------------+-------------------+-------------+--------------+-------------------+
| 병원 ID     | 주소              | 홈페이지           | 병원명            | 운영시간    | 전화번호     | URL               |
+-------------+-------------------+-------------------+-------------------+-------------+--------------+-------------------+
| H0000152568 | ...               | ...               | ...               | ...         | ...          | ...               |
| H0000155388 | ...               | ...               | ...               | ...         | ...          | ...               |
| H0000160628 | ...               | ...               | ...               | ...         | ...          | ...               |
| H0000218720 | ...               | ...               | ...               | ...         | ...          | ...               |
| H0000238449 | ...               | ...               | ...               | ...         | ...          | ...               |
+-------------+-------------------+-------------------+-------------------+-------------+--------------+-------------------+
```  
