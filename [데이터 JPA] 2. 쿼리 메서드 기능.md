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
JPA에서 다음 조건으로 페이징과 정렬을 해보자.

- 검색 조건: 10살

- 정렬 조건: 이름으로 내림

- 페이징 조건: 첫 번째 페이지, 페이지 당 데이터 3건

<Br>

```java
public List<Member> findByPage(int age, int offset, int limit){
        return em.createQuery("select m from Member m where m.age = :age order by m.username desc", Member.class)
                .setParameter("age", age)
                .setFirstResult(0)
                .setMaxResults(limit)
                .getResultList();
    }
```

<br> 

페이징을 할때 데이터의 총 개수를 보통 반환하는데 해당 메서드도 만들어주자.

```java
    public long totalCount(int age){
        return em.createQuery("select count(m) from Member m where m.age = :age", Long.class)
                .setParameter("age", age)
                .getSingleResult();
    }
```

<br>

어렵진 않지만 매 페이징 시마다 쿼리를 짜야한다.

스프링 데이터 JPA는 어떻게 페이징을 할까?

<br><Br>


### 2) 스프링 데이터 JPA

스프링 데이터 JPA는 모든 데이터베이스의 페이징과 정렬을 공통으로 관리한다.

- `org.springframework.data.domain.Sort` : 정렬 기능

- `org.springframework.data.domain.Pageable` : 페이징 기능 (내부에 Sort 포함)


<br><Br>

#### a) 반환 타입

- `org.springframework.data.domain.Page` : 추가 count 쿼리 결과를 포함하는 페이징

- org`.springframework.data.domain.Slice` : 추가 count 쿼리 없이 다음 페이지만 확인 가능 (내부적으로 limit + 1조회)

- `List (자바 컬렉션)`: 추가 count 쿼리 없이 결과만 반환

<br><Br>

#### b) 예제

```java
Page<Member> findByUsername(String name, Pageable pageable); 
//count 쿼리 사용

Slice<Member> findByUsername(String name, Pageable pageable); 
//count 쿼리 사용 안함

List<Member> findByUsername(String name, Pageable pageable); 
//count 쿼리 사용 안함

List<Member> findByUsername(String name, Sort sort);
```

<br><Br>

스프링 데이터 JPA에서 다음 조건으로 페이징과 정렬을 해보자.

- 검색 조건: 10살

- 정렬 조건: 이름으로 내림

- 페이징 조건: 첫 번째 페이지, 페이지 당 데이터 3건

<br>

- Repository

```java
Page<Member> findByAge(int age, Pageable pageable);
```

- TestCode

```java
    @Test
    public void paging(){
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));


        //then
        List<Member> content = page.getContent(); //조회된 데이터
        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
        assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
    }
```

- 두 번째 파라미터인 Pageable은 인터페이스이다. 따라서 해당 인터페이스를 구현한 PageRequest 객체를 사용해야 한다.

- PageRequest 생성자의 첫 번째 파라미터에는 현재 페이지를, 두 번째 파라미터에는 조회할 데이터 수를 입력한다. 추가로 정렬 정보도 파라미터로 넘길 수 있다.

- 페이지는 0부터 시작이다.

<br><Br>

#### c) Page 인터페이스

```java
public interface Page<T> extends Slice<T> {
    int getTotalPages(); //전체 페이지 수
    long getTotalElements(); //전체 데이터 수
    <U> Page<U> map(Function<? super T, ? extends U> converter); //변환기
}
```

<br><Br>

#### d) Slice 인터페이스

```java
public interface Slice<T> extends Streamable<T> {
    int getNumber(); //현재 페이지
    int getSize(); //페이지 크기
    int getNumberOfElements(); //현재 페이지에 나올 데이터 수
    List<T> getContent(); //조회된 데이터
    boolean hasContent(); //조회된 데이터 존재 여부
    Sort getSort(); //정렬 정보
    boolean isFirst(); //현재 페이지가 첫 페이지 인지 여부
    boolean isLast(); //현재 페이지가 마지막 페이지 인지 여부
    boolean hasNext(); //다음 페이지 여부
    boolean hasPrevious(); //이전 페이지 여부
    Pageable getPageable(); //페이지 요청 정보
    Pageable nextPageable(); //다음 페이지 객체
    Pageable previousPageable();//이전 페이지 객체
    <U> Slice<U> map(Function<? super T, ? extends U> converter); //변환기
}
```

<Br><Br>

여러 테이블을 조인하여 Page로 반환하게 되면 Count 쿼리가 추가로 날아갈 때, 
필요없는 테이블과 Join이 발생한다. 이러한 성능 이슈를 해결하기 위해서 Count쿼리를 다음과 같이 분리 할 수 있다.

#### e) Count쿼리 분리

```java
@Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m.username) from Member m")
Page<Member> findMemberAllCountBy(Pageable pageable);
```

<br><Br>

페이지 인터페이스에는 map(변환) 인터페이스가 있다.

Repository에서 반환타입을 

`Page<Member>`와 같이 설정하면 안된다.

여러번 설명했듯이 반환타입에 엔티티를 노출하면 안되기 때문이다.

따라서 map을 이용하여 `Page<Member>`를 `Page<MemberDto>`로 변경해야 한다.

<br>

#### f) 엔티티를 DTO로 변경

```java
Page<Member> page = memberRepository.findByAge(10, pageRequest);
Page<MemberDto> dtoPage = page.map(m -> new MemberDto());
```

<br><Br>

## 7. 벌크성 수정 쿼리

<br>

기존 JPA 기본편에서 벌크 쿼리에 대해서 배웠을 것이다.

벌크 쿼리한 하나의 데이터가 아닌 여러 데이터를 동시에 수정하는 쿼리를 말한다.

JPA에서는 .executeUpdate()를 통해서 쿼리를 날리고, 변경된 데이터의 수 int로 반환해주었다.

스프링 데이터 JPA에서 벌크성 쿼리를 알아보자.

```java
@Modifying
@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
int bulkAgePlus(@Param("age") int age);
```

- 벌크성 수정, 삭제 쿼리는 @Modifying 어노테이션을 추가해 주어야 한다. 만약 하지 않으면 `QueryExecutionRequestException` 이 발생한다.

- 해당 어노테이션이 JPA의 .executeUpdate()기능을 수행한다.

- JPA 기본에서도 설명 했듯이 벌크 연산 후에는 영속성 컨텍스트를 clear 해주어야 한다. 벌크 연산은 영속성 컨텍스트를 거치지 않고, DB에 직접 쿼리를 날린다. 따라서, 영속성 컨텍스트에서 관리하는 엔티티를 업데이트 하게 되면, 업데이트 되지 않은 엔티티가 영속성 컨텍스트에 존재하게 된다. 벌크연산을 한뒤 조회를 하게 되면 업데이트 되지 않은 엔티티가 영속성 컨텍스트에서 반환되므로, 벌크 연산을 수행한 뒤에는 반드시 영속성 컨텍스트를 clear 해주어야 한다.

스프링 데이터 JPA에서는 해당 기능을 @Modifying 어노테이션으로 제공한다.

`@Modifying(clearAutomatically = true)`로 설정하게 되면 clear를 해주지 않아도, 업데이트 쿼리가 나간뒤 자동으로 영속성 컨텍스트를 clear 해준다.

<br>

> __참고__ <br>
영속성 컨텍스트를 clear하기 전에 flush를 수행하지 않는 이유는 JPA를 잘 아는 분이라면 이미 알고 있을 것이다. JPA에서는 JPQL쿼리가 실행되기 전에 flush가 자동으로 이루어 진다. 따라서 flush를 하지않고 clear만 해주면 되는 것이다.

<br><Br>

## 8. EntityGraph

<Br>

JPA에서는 연관된 엔티티들을 join fetch를 이용하여 한번에 조회하였다.

물론 스프링 데이터 JPA에서도 @Query를 사용하여 fetch join을 사용할 수 있다.

스프링 데이터 JPA는 JPQL없이 페치 조인 없이 해당 기능을 사용 할 수 있다.

<br><Br>

```java

//공통 메서드 오버라이드
@Override
@EntityGraph(attributePaths = {"team"})
List<Member> findAll();

//JPQL + 엔티티 그래프
@EntityGraph(attributePaths = {"team"})
@Query("select m from Member m")
List<Member> findMemberEntityGraph();

//메서드 이름으로 쿼리에서 특히 편리하다.
@EntityGraph(attributePaths = {"team"})
List<Member> findByUsername(String username)

```

- findAll()는 인터페이스에서 제공하는 메서드이기 때문에 오버라이드 어노테이션을 해주어야 한다.

- 엔티티 그래프 어노테이션은 LEFT OUTER JOIN을 사용한다.


















