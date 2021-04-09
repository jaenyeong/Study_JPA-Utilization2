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

## 엔티티 설계

### API 스펙을 위한 별도의 DTO 클래스 생성
* 유효성 검사 기준 등이 다른 두 방식(`Presentation(화면 템플릿)`, `API`)이 혼재하면 엔티티가 지저분해짐
* 같은 API 방식이라도 엔티티를 사용할 때 유효성 검사 등의 기준이 다를 수 있음
  * `@JsonIgnore`, `@NotEmpty` 등과 같은 조건들이 API마다 다르면 구현이 어려워짐
  * 또한 컬렉션을 그대로 반환하게 되면 추후 API 스펙 확장이 어려움
* 엔티티를 계층간 데이터 전송시에 사용하지 말고 전송을 위한 DTO를 생성해 사용할 것

### CQS (Command and Query Separation)
* 수정한 엔티티를 반환하면 커맨드에 이어 쿼리를 한 것과 동일
  * 따라서 변경 작업과 조회를 별도로 수행하기도 함
