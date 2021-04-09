# Study_JPA-Utilization2
### 인프런 실전! 스프링 부트와 JPA 활용2 - 웹 애플리케이션 개발 (김영한)
https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-API%EA%B0%9C%EB%B0%9C-%EC%84%B1%EB%8A%A5%EC%B5%9C%EC%A0%81%ED%99%94/dashboard
-----

## [Settings]
#### Project Name
* Study_JPA-Utilization2
#### java
* zulu jdk 11
#### gradle
* IDEA gradle wrapper
#### Spring boot
* 2.4.4
#### H2
* brew install h2
  * 터미널에서 `h2` 명령어로 사용 가능
* JDBC URL : jdbc:h2:~/jpashop
  * 접속 URL : jdbc:h2:tcp://localhost/~/jpashop
* root 경로에 jpashop.mv.db 파일 생성 여부 확인
* 1.4.200 버전에서 MVCC(Multi-Version Concurrency Control, 다중 버전 동시성 제어) 옵션 사용시 에러 발생
  * 1.4.198 버전부터 삭제됨
* 기존에 테이블 존재시 모두 삭제
  * ```drop all objects;```
#### p6spy
* JPA 쿼리 파라미터 로깅 확인 라이브러리
* 1.6.3
* 운영에 사용할 때는 사전 성능 테스트 필수
-----

## [환경 설정]

### IDEA JPA 설정
**설정이 되어 있지 않은 경우**
* `,` + `;` 단축키로 프로젝트 설정 진입
* Project Settings > Modules
  * 패키지의 `main` 경로에 JPA 설정
  * `jpashop` > `main` > JPA 추가, `Provider`를 `Hibernate`로 설정

### Postman 설치
* [Postman](https://www.getpostman.com/)

### 패키지 설정
* 템플릿(화면) 반환 방식과 API의 경우 공통 (처리)기능 등이 다르기 때문에 별도의 패키지로 분할
