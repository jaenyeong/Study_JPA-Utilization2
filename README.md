# Study_JPA-Utilization2
### 인프런 실전! 스프링 부트와 JPA 활용2 - API 개발과 성능 최적화 (김영한)
https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-API%EA%B0%9C%EB%B0%9C-%EC%84%B1%EB%8A%A5%EC%B5%9C%EC%A0%81%ED%99%94
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

## 지연 로딩과 조회 성능 최적화

### 양방향 연관관계 주의사항
* 예제에서 `Member`와 `Order` 객체는 양방향 참조 상태이기 때문에 둘 중 하나의 데이터를 엔티티 그대로 반환하면 무한 루프에 빠짐
  * 둘 중 한곳은 `@JsonIgnore` 애너테이션을 태깅해야 함
    * `Member`의 `orders` 필드에 `@JsonIgnore` 태깅
    * `OrderItem`의 `order` 필드에 `@JsonIgnore` 태깅
    * `Delivery`의 `order` 필드에 `@JsonIgnore` 태깅
* 지연 로딩일 때 `ByteBuddyInterceptor`를 활용한 Proxy(연관된 엔티티를 상속하는) 객체를 생성해 연관된 엔티티의 인스턴스로 삽입
* 엔티티 직접 반환 시 `Hibernate5Module` 사용 방법
  * 기본적으로 초기화 된 프록시 객체만 노출, 초기화 되지 않은 프록시 객체는 노출 하지 않음
  * `implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'` gradle 설정
  * ~~~
    @Bean
    Hibernate5Module hibernate5Module() {
        final Hibernate5Module hibernate5Module = new Hibernate5Module();
        // 옵션을 직접 설정해서 데이터 생성 시점에 지연 로딩을 실행하여 모든 데이터를 가져옴
        hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
        return hibernate5Module;
    }
    ~~~
  * `hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);`을 사용하지 않고 직접 지연 로딩하는 방법
    * ~~~
      @GetMapping("/api/v1/simple-orders")
      public List<Order> ordersV1() {
          final List<Order> allOrders = orderRepository.findAllByCriteria(new OrderSearch());
          for (Order order : allOrders) {
              // 강제로 지연 로딩을 실행
              // getMember(), getDelivery()까지는 쿼리가 날라가지 않음
              // getName(), getAddress()까지 실행해야 쿼리가 날라감
              order.getMember().getName();
              order.getDelivery().getAddress();
              }
      
              return allOrders;
          }
      ~~~
* 결론적으로 `Hibernate5Module` 사용해 `Entity`를 직접 반환하지말고 DTO 사용할 것
  * 또한 지연 로딩을 피하기 위해 즉시(`EAGER`) 로딩으로 설정한다면 성능 이슈가 발생할 수 있음

### 주문 조회 V1, V2의 공통적인 문제점
* 지연 로딩때문에 DB 쿼리가 너무 많이 호출되어 성능 이슈 발생
  * `N + 1`문제 발생
  * 예제에서 `order` 조회 1번, `order -> member` 지연 로딩 조회 `N`번, `order -> delivery` 지연 로딩 조회 `N`번
    * 지연 로딩도 최초의 영속성 컨텍스트를 확인
    * 영속성 컨텍스트의 동일한 데이터가 없는 최악의 경우 DB 쿼리가 5(`1 + 2 + 2`)번 호출됨

### 주문 조회 V3, V4의 차이
* V3는 fetch join으로 엔티티 자체를 가져와서 다양한 형태로 활용 가능
* V4는 특정 `DTO`의 종속적인 형태로 직접 JPQL을 작성, 가져와서 활용이 어려움
  * 다만 V3보다는 성능 최적화 되어 있음

### 주문 조회 V4 특징
* 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회
* `new` 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
* `SELECT` 절에서 원하는 데이터를 직접 선택하므로 DB 애플리케이션 네트워크 용량 최적화
  * 하지만 생각보다 성능적으로 큰 차이는 없음
* 리포지토리 재사용성 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 들어가는(논리적으로 API에게 종속적) 단점

### 주문 조회 V3, V4는 상황에 따라 사용할 방법을 결정
* V4 방식을 사용할 경우 별도의 `repository`를 생성 (상황에 따라 패키지를 분할)하여 사용할 것을 권장

### 쿼리 방식 선택 권장 순서
1. 우선 엔티티를 `DTO`로 변환하는 방법을 선택
2. 필요하면 `fetch join`으로 성능을 최적화 (이걸로 대부분의 성능 이슈가 해결됨)
3. 그래도 안되면 `DTO`로 직접 조회하는 방법 사용
4. 최후의 방법은 JPA가 제공하는 `Native SQL`이나 `Spring JDBC Template`을 사용해 `SQL`을 직접 사용
