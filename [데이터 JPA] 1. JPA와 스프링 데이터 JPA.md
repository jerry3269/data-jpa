# JPA와 스프링 데이터 JPA

- 스프링 데이터 JPA는 JPA를 더 효율적으로 사용하기 위한 기술이다.

- 데이터 JPA는 매우 유용한 기능들을 많이 제공해 주지만, JPA를 활용하는
기술이기 때문에 JPA 동작원리를 알고 있어야 한다.

- 데이터 JPA가 만능인것은 아니다. 

- 동적 쿼리는 QueryDSL을 이용해야함.

<br><Br>

## 1. JPA 레포지토리

<br>

다음은 JPA를 이용한 Member레포지토리이다.

```java
@Repository
public class MemberJpaRepository {

    @PersistenceContext
    private EntityManager em;

    public Member save(Member member){
        em.persist(member);
        return member;
    }

    public void delete(Member member){
        em.remove(member);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public Optional<Member> findById(Long id){
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public long count(){
        return em.createQuery("select count(m) from Member m", Long.class)
                .getSingleResult();
    }
```

JPA를 이용하여 회원 저장, 삭제, 모두 조회, 회원ID로 조회, count 를 반환하는 메서드를 구현하였다.

- Optional<T> : Optional로 클래스를 감싸게 되면, null이 들어와도 NullPointException이 발생하지 않는다. 

<br><Br>

## 2. 스프링 데이터 JPA

해당 JPA레포지토리를 스프링 데이터 JPA 레포지토리로 변경해 보겠다.

```java
public interface MemberRepository extends JpaRepository<Member, Long> {

}
```

끝이다. ... ????

스프링 데이터 JPA는 매우 강력한 기능을 제공해준다. 
단순 조회, 등록, 삭제와 같은 기능은 기본적으로 인터페이스로 제공해준다.

<br>

스프링 부트를 사용하면 기본적으로 애플리케이션 코드 패키지와 하위 패키지에서 JpaRepository를 상속한 인터페이스를 찾는다. 이후 구현클래스를 자동으로 생성한다.


이때 구현 클래스를 찍어보면 프록시 객체가 나온다. 이것이 JPA가 자동으로 생성해주는 구현 클래스 이다.


정말 엄청나다.

<br><Br>

뿐만 아니라, @Repository 어노테이션이 없는 것을 볼 수 있다.
스프링 데이터 JPA는 컴포넌트 스캔도 자동으로 처리해준다.

또한, JPA예외를 스프링 예외로 변환해주는 작업도 자동으로 처리해 준다.


<br><Br>

참고로 JpaRepository<Member, Long>에서 Member는 엔티티 타입, Long은 엔티티의 식별자 타입(pk)를 적어주면 된다.


JpaRepository는 부모로 `PagingAndSortingRepository`, `CrudRepository`, `Repository`를 갖는다. 

`PagingAndSortingRepository`, `CrudRepository`, `Repository`는 스프링 데이터 레포지토리 기능으로, 데이터 JPA, 몽고DB등이 공통적으로 사용할 수 있는 기능을 제공해준다.

JpaRepository는 JPA에 특화된 기능을 제공해준다.





