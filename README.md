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

## 컬렉션 조회 최적화

### 주문 조회 (Order API) V1
* 현재 `Hibernate5Module`을 사용한 상태라 `orderItems`의 지연 로딩을 강제로 초기화
* 역시나 엔티티를 직접 노출하는 형태이기 때문에 사용 금지

### 주문 조회 (Order API) V2
* `DTO` 안에도 엔티티가 직접 포함되면 안됨
  * 예제에서 `OrderDto` 안에 `OrderItem` 리스트를 `OrderItemDto`로 변환한 리스트를 사용

### 주문 조회 (Order API) V3
* 일대다 연관 관계 쿼리에서는 다(`Many`) 쪽 데이터(`레코드`) 수와 동일하게 데이터 양이 늘어남
  * RDBMS 특성상 조인으로 데이터를 가져올 경우 특정 데이터가 중복될 수 밖에 없음
    * 예제의 경우 `Order` 데이터가 `OrderItem` 데이터로 인해 중복됨
  * 그러나 `JPA(Hibernate)`가 이 데이터의 객체 그래프를 제대로 생성하지 못함
* V2 API와 V3 API는 `JPQL` 차이만 있음
* `fetch join`으로 `SQL`이 한 번만 실행
  * `JPQL`의 `distinct` 키워드 사용하여 중복 데이터 제거
    * SQL`에 `distinct` 키워드를 추가할 뿐 아니라 같은 엔티티인 경우 객체 그래프에서 중복을 제거
  * 단점은 `fetch join`을 사용하면 페이징 처리 불가능
    * `setFirstResult(), setMaxResults()` 등 명령으로 페이징 불가능
    * `firstResult/maxResults specified with collection fetch; applying in memory!` 에러
      * 이 경우 DB에서 페이징 처리를 하지 않고 데이터를 가져온 후 메모리에서 페이징 처리
      * 따라서 OOM(`Out of memory`) 발생할 수 있음
    * `fetch join`을 하면 DB에서는 `Order`가 아닌 `OrderItem`을 기준으로 페이징 처리하게 됨
    * 그래서 일대다의 경우 위 에러를 출력하고 메모리에서 처리
    * 하지만 위험하기 때문에 웬만하면 사용 금지
  * 컬렉션 `fetch join`은 1개만 사용 가능
    * 데이터 정합성 문제로 컬렉션 둘 이상 사용 금지 (예를 들어 1 : N : M 같은 구조)
  
### 주문 조회 (Order API) V3-1 페이징 문제 해결
* 예제에서는 `Order`를 기준으로 페이징 처리 하기를 원함
  * 하지만 `OrderItem`과 일대다 연관 관계 구조에서 조인을 하면 `OrderItem`을 기준으로 페이징 처리

**페이징 처리 확인 사항**
* `*ToOne`(`OneToOne`, `ManyToOne`) 관계는 모두 `fetch join`
  * 데이터 레코드 수를 증가시키지 않아 페이징 쿼리에 영향을 주지 않음
* 컬렉션은 지연 로딩으로 조회
* 지연 로딩 성능 최적화를 위해 `hibernate.default_batch_fetch_size`, `@BatchSize` 적용
  * ~~~
    spring:
      jpa:
        properties:
          hibernate:
            default_batch_fetch_size: 100
    ~~~
  * `hibernate.default_batch_fetch_size`
    * 글로벌 설정 (`application.yml` 파일에 설정)
  * `@BatchSize(size = ?)`
    * 개별 최적화 (특정 엔티티)
    * 일대다 관계인 컬렉션은 인스턴스 필드 위에 애너테이션 태깅
    * 일대일(다대일) 관계는 해당 엔티티 클래스 위에 애너테이션 태깅
  * 이 옵션을 사용하면 컬렉션이나 프록시 객체를 한꺼번에 설정한 사이즈만큼 `IN` 쿼리로 조회
    * `IN` 쿼리에 파라미터 개수 설정
* `offset`의 값을 `0`으로 설정하면 하이버네이트가 `offset` 키워드 구문을 제거

#### V3, V3-1 차이
* V3 방식은 한 번에 데이터를 가져오나 중복 데이터 존재, DB에서 앱으로 모두 전송 (데이터 전송량이 많게됨)
* V3-1 방식은 쿼리를 여러 번으로 나눠 전송되지만 데이터 전송량이 최적화되어 DB에서 앱으로 전송
  * `IN` 쿼리로 필요한 데이터만 찾아옴
* ~~~
  public List<Order> findAllWithMemberDelivery(final int offset, final int limit) {
      return em.createQuery(
          "select o from Order o"
              // default_batch_fetch_size 옵션으로 멤버와 딜리버리 생략해도 IN 쿼리로 가져오게 됨
              // 하지만 DB 쿼리 호출 수 때문에 *ToOne 연관 관계는 join fetch로 가져오는 것이 효율적
              + " join fetch o.member m"
              + " join fetch o.delivery d",
          Order.class)
          .setFirstResult(offset)
          .setMaxResults(limit)
          .getResultList();
  }
  ~~~

#### 페이징 처리 정리
* `ToOne` 관계는 `fetch join`으로 호출 쿼리를 줄이기
* `ToMany` 관계는 `hibernate.default_batch_fetch_size` 옵션을 사용해 최적화
* size는 일반적으로 `100`~`1000` 선택 권장
  * DB 벤더마다 다르나 `1000`이 일반적인 최대치
  * 애플리케이션(WAS) 측면에서는 몇으로 설정하든 원하는 전체 데이터양을 로딩해야 하므로 메모리 사용량이 같음
  * 따라서 DB가 순간적인 부하를 얼마나 견딜 수 있는지를 기준으로 선택
