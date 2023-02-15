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

## 2. NamedQuery

<br><Br>

엔티티 클래스에 다음과 같이 NamedQuery를 정의한다.

```java
@NamedQuery(
        name = "Member.findByUsername",
        query = "select m from Member m where m.username = :username"
)
public class Member {
    ...
}
```

<Br><Br>

### 1) JPA로 Named쿼리 호출

```java
public List<Member> findByUsername(String username){
        return em.createNamedQuery("Member.findByUsername", Member.class)
                .setParameter("username", username)
                .getResultList();
    }
```

<Br><Br>

### 스프링 데이터 JPA로 Named쿼리 호출

```java
@Query(name = "Member.findByUsername")
List<Member> findByUsername(@Param("username") String username);
```

<Br>

이 경우, `@Query(name = "Member.findByUsername")`는 생략이 가능하다.

스프링 데이터 JPA는 기본적으로 선언한 "도메인 클래스 + .(점) + 메서드 이름"으로 Named쿼리를 찾아서 실행한다.

만약 Named쿼리가 없다면 1번에서 다뤘던 메서드 이름으로 쿼리 생성 전략을 사용한다.

<br>

- 메서드 이름으로 쿼리를 생성하는 전략은 쿼리 조건이 까다롭지 않을때 사용한다.

<br>
 
- 예를 들어 findByUsernameAndAgeGreaterThan이라는 메서드가 있다고 하자. 그런데 여기서 조건이 더 추가 된다면 메서드 이름이 너무 길어진다. 

- 따라서 메서드 이름으로 쿼리를 생성하는 것은 where절에 1~2개의 조건이 추가되는 정도로만 사용하는 것이 좋다.

<br><Br>

그렇다면 스프링 데이터 JPA에서는 JPA에서 처럼 createQuery하는 기능이 없을까?

JpaRepository는 인터페이스고, 이를 상속받는 MemberRepository도 인터페이스이므로, CreateQuery를 하기 위해서는 구현 클래스를 만들어야 하는데, 이때, JpaRepository 메서드가 모두 인터페이스이기 때문에 해당 기능들을 모두 구현해야 한다.

따라서 스프링 데이터 JPA는 @Query 어노테이션으로 쿼리를 보낼수 있게 설계했다.

<br><Br>

## 3. 스프링 데이터 JPA - @Query

- @Query와 @NamedQuery는 정적 쿼리로, 애플리케이션 실행 시점에 SQL로 해석되어 저장되기 때문에 문법오류를 발견할수 있고 재사용 할 수 있다는 매우 큰 장점이 있다.

- 반면에` em.createQuery() `로 만들어낸 쿼리는 단순 문자열로, 최악의 경우 클라이언트가 직접 클릭했을때 오류가 발생할 수 있는 문제가 있다.

- 동적쿼리는 매우 복잡한데 후에 배울 QueryDSL을 사용한다.

<br><Br>

### 1) 레포지토리 메서드에 쿼리 정의하기

```java
@Query("select m from Member m where m.username = :username and m.age = :age")
List<Member> findUser(@Param("username") String username, @Param("age") int age);
```

- `@org.springframework.data.jpa.repository.Query` 어노테이션을 사용한다.

- 실무에서 메서드 이름 쿼리 생성 기능은 파라미터가 증가하면 메서드 이름이 매우 지저분해진다. 따라서 @Query기능을 많이 사용한다.

<Br><Br>

### 2) 값, DTO 조회하기

<Br><BR>

#### a) 단순한 값 하나 조회

```java
@Query("select m.username from Member m")
List<String> findUsernameList();
```

<br><Br>

#### b) DTO로 직접 조회

```java
@Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
List<MemberDto> findMemberDto();
```

- DTO 직접 조회이기 때문에 join fetch를 사용하지 않았다.

- new를 사용하여야 하고 생성자가 맞는 DTO가 있어야한다.

<br><Br>

## 4. 파라미터 바인딩

<br><br>

### 1) 위치, 이름 기반

```java
select m from Member m where m.username = ?0 //위치 기반
select m from Member m where m.username = :name //이름 기반
```

<br><Br>

### 2) 파라미터 바인딩

```java
@Query("select m from Member m where m.username = :name")
Member findMembers(@Param("name") String username);
```

- @Param을 사용하여 파라미터 username을 name 변수에 바인딩 한다.

<br><Br>

### 3) 컬렉션 파라미터 바인딩

```java
@Query("select m from Member m where m.username in :names")
List<Member> findByNames(@Param("names") List<String> names);
```

- in절을 사용하여 컬렉션 파라미터를 바인딩 할 수 있다.

<br><Br>

## 5. 반환 타입

```java
List<Member> findListByUsername(String name); //컬렉션

Member findMemberByUsername(String name); //단건

Optional<Member> findObtionalByUsername(String name); //단건 Optional
```

- 컬렉션
    - 결과 없음: 빈 컬렉션 반환

- 단건조회
    - 결과 없음: `null` 반환
    - 2건 이상: `javax.persistence.NonUniqueResultException` 예외 발생


<br><Br>

> __참고__ <Br>
단건 메서드를 호출하면 스프링 데이터 JPA는 내부에서 JPQL의 getSingleResult()를 호출한다. 이 메서드를 호출 했을때 조회 결과가 없으면 
`javax.persistence.NoResultException` 예외가 발생하는데 개발자 입장에서 다루기가 상당히 불편하다. 스프링 데이터 JPA는 단건을 조회할 때 이 예외가 발생하면 예외를 무시하고 대신에 null 을 반환한다.

<br><BR>

## 6. 페이징과 정렬

### 1) 순수 JPA

### 2) 스프링 데이터 JPA















