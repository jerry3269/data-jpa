# 쿼리 메서드 기능

지금부터는 더 복잡한 JPA 메서드들을 구현해보고 스프링 데이터 JPA로 바꿔보겠다.


<br><Br>


## 1. 메서드 이름으로 쿼리 생성

이름과 나이를 기준으로 회원을 조회

### 1) 순수 JPA

```java
public List<Member> findByUsernameAndAgeGreaterThan(String username, int age){

        return em.createQuery("select m from Member m where m.username = :username and m.age > :age")
                .setParameter("username", username)
                .setParameter("age", age)
                .getResultList();
    }
```

순수 JPA를 사용하면 이러한 메서드를 작성해야 한다.

하지만 데이터 JPA를 사용해보자.

<br><Br>

### 2) 스프링 데이터 JPA

```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
}
```
끝이다.

구현도 하지 않았는데 이게 어떻게 가능할까?

<Br>

이유는 스프링 데이터 JPA는 조건에 맞춰서 메서드이름을 작성하면 메서드의 이름을 분석하여 JPQL을 생성하고 실행하는 기능을 제공해준다.

<Br>

#### a) 쿼리 메서드 필터 조건

다음 문서 참고

(https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation)

<br>


<br>

#### b) 스프링 데이터 JPA가 제공하는 쿼리 메서드 기능
- 조회: `find...By`,` read...By`, `query...By`,` get...By`
    - findHelloBy 처럼 ...에 식별하기 위한 내용이 들어간다.(아무내용이나 들어가도 됨)

- COUNT: `count...By` : 반환타입 `long`

- EXISTS: `exists...By` : 반환타입 `boolean`

- 삭제: `delete...By`, `remove...By` : 반환타입 `long`

- DISTINCT: `findDistinct`, `findMemberDistinctBy` : distinct조건 추가

- LIMIT: `findFirst3`, `findFirst`, `findTop`, `findTop3` : limit조건 추가

<br><br>


더 알고 싶다면 다음 주소를 참고하자.

(https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result)

<br><br>

> __참고__ <br>
엔티티의 필드명이 변경되면 메서드 이름도 변경해야 한다. 그렇지 않으면 애플리케이션 실행 시점에 오류가 발생한다.
이렇게 애플리케이션 로딩 시점에 오류를 인지할 수 있는것은 스프링 데이터 JPA의 매우 큰 장점이다.

<br><Br>














