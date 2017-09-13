[Android App] WearlovelyPet
========================
WearlovelyPet is a dog managment application with wearable device. It provides walk(timer/map), location tracing, customized services.
## Image
![WearlovelyPet](https://i.imgur.com/630XtgD.gif)
<br></br>
## Project Info
#### 수행기관
한이음(미래창조과학부)
#### 프로젝트 명
아두이노를 활용한 반려동물 관리 애플리케이션, “ 웨어러블리 펫 ”
#### 프로젝트 소개
바쁜 현대인이 집 밖에서도 반려견을 확인하고 체계적으로 그들의 건강을 챙길 수 있는 반려견 관리 콘텐츠로 웨어러블 기기와 애플리케이션으로 구성되었습니다.
<br></br>
## Development Info
#### 개발 기간 / 개발 인원
2016.09.01 - 2016.12.08 / 5명(본인 포함 안드로이드 2명, 웨어러블 기기 3명)
#### 개발 환경 / 개발 언어
Android Studio, ACROEDIT / Java(中), MySQL(中), php(下)
#### 주요 역할
* 안드로이드 프론트엔드 전체 개발
* 안드로이드 백엔드 일부 기능(메인, 산책, 위치) 개발
* 기획, 디자인 전체
#### 특징
* 소량의 데이터를 저장하는 경우라서 SQLite가 아닌 TextManager.class를 생성하여 txt 형식 문서로 데이터 보관
* php 문을 사용하여 Linux Ubuntu 클라우드 서버와 연동하여 웨어러블 기기로부터 받아온 위치 데이터를 수신
* '산책 기록 타이머' 기능은 자정이 지나면 자동으로 데이터를 저장하고 타이머와 지도를 리셋하도록 기능 설정
* 경로 생성, 반경 거리 원형 표시, 지도 캡처 등과 같은 Google Map API의 다양한 기능 활용
* 외부 그래프 라이브러리(MPAndroidChart)를 활용하여 5일 동안의 산책 시간을 그래프로 제공
* 'MIT 위경도 거리 계산' 알고리즘을 바탕으로 지점 간 거리 계산한 후, 반려견이 사용자가 지정한 반경 거리를 벗어나면 비상 연락망으로 연결하도록 설정
* 외부 이미지 로딩 라이브러리 Glide 사용
* GoogleMap.SnapshotReadyCallback을 통해 Google Map 캡처 기능 설정
