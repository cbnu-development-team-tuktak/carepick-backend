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
