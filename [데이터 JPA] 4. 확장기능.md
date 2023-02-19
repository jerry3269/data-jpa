# 1. 사용자 정의 레포지토리

스프링 데이터 JPA 레포지토리는 인터페이스만 정의하고 구현체는 스피링이 자동 생성해준다.

스프링 데이터가 JPA가 제공하는 인터페이스를 직접 구현하면 구현해야 하는 기능이 너무 많아진다.


인터페이스의 메서드를 구현해야 하는 상황
- JPA 직접 사용
- 스프링 JDBC Template 사용
- 마이 바티스 사용
- 데이터 베이스 커넥션 직접 사용
- Querydsl 사용

<br>

메서드를 구현하기 위해서는 사용자 정의 인터페이스를 새로 만들어야한다.

- ## 1) 인터페이스

```java
public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
```

- ## 2) 구현클래스

```java
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m")
                .getResultList();
    }
}
```

- ## 3) 사용자 정의 인터페이스 상속

```java
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

}
```

<br>

Custom이라는 인터페이스를 만드는 이유는, 인터페이스는 구현 클래스를 상속할 수 없기 때문이다. 

따라서 Custom이라는 인터페이스를 새로 만들고 해당 인터페이스를 구현한 Impl 클래스를 만든다. 이후 스프링 데이터 JPA 인터페이스가 해당 인터페이스를 상속하도록 하면 구현한 메서드를 사용할 수 있다.

물론, 자바가 해당 기능을 제공하진 않고, JPA가 인식하여 구현 클래스를 스프링 빈으로 등록해준다.

따라서 @Repository와 같은 어노테이션을 붙이지 않아도 동작한다.

<br><Br>


## 4) 사용자 정의 구현 클래스 규칙

- 레포지토리 인터페이스 이름 + Impl

- 사용자 정의 인터페이스 이름 + Impl(최신 트랜드)

- MemberRepositoryImpl

- MemberRepositoryCustomImpl -> 해당 기능은 최신 스프링 부트에서 지원하는 기능이다. (최신 트랜드)

<Br><Br>

## 5) 참고

- 실무에서는 주로 Querydsl을 사용할때 사용자 정의 레포지토리를 사용한다.

- 항상 사용자 정의 레포지토리가 필요한 것은 아니다.

<br><Br>

# 2. Auditing

엔티티를 생성, 변경 할때 변경한 사람과 시간을 추적

- 등록일

- 수정일

- 등록자

- 수정자

유지보수에 있어서 등록일, 수정일, 등록자, 수정자는 거의 필수이다. 특히, 등록일, 수정일은 모든 테이블에 있어서 필요한 정보이기 때문에 모든 엔티티에 해당 데이터를 넣어야 한다.

<Br><Br>

JPA 기본에서 배웠듯이, DB에서는 상속이라는 개념이 없기 때문에, @MappedSuperclass 어노테이션을 추가하여 실제로 상속관계는 아니지만 매핑정보만을 제공할 수 있다.

<br>

## 1) 순수 JPA

```java
@MappedSuperclass
@Getter
public class JpaBaseEntity {

    @Column(updatable = false)
    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist(){
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }

    @PreUpdate
    public void preUpdate(){
        updatedDate = LocalDateTime.now();
    }

}
```

이후 Member 엔티티가 해당 클래스를 extends 해야 한다

```java
public class Member extends JpaBaseEntity {}
```

@Column(updatable = false)을 하게 되면 update로 수정되지 않는다. 
따라서 등록일을 처음 등록한 뒤에 수정되지 않는다.

<Br>

### a) 사용 가능한 주요 어노테이션

- @PrePersist, @PostPersist

- @PreUpdate, @PostUpdate

<br><Br>

## 2) 스프링 데이터 JPA

스프링 데이터 JPA로 Auditing을 하기 위해서는 

@EnableJpaAuditing 어노테이션을  스프링 부트 설정 클래스에 적용해야 한다.

엔티티에는 @EntityListeners(AuditingEntityListener.class) 어노테이션을  적용해야 한다.

```java
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;
}
```

스프링 데이터 JPA는 해당 기능들을 어노테이션으로 제공한다. 매우 편리한 기능인것 같다.

여기서 등록일과 수정일은 순수 JPA에서 했던 일들을 자동으로 처리해준다.

<Br><Br>

그런데, createdBy와 lastModified는 어떤 값을 보고 넣어주게 될까?

id? username? 이를 처리해주기 위해서는 등록자, 수정자를 처리해주는 AuditorAware을 스프링 빈으로 등록해주어야 한다.

```java
@Bean
public AuditorAware<String> auditorProvider() {
    return () -> Optional.of(UUID.randomUUID().toString());
}
```

실무에서는 세션정보나, 스프링 시큐리티 로그인 정보에서 ID를 받는다.

<br><Br>


# 3. 웹 - 도메인 클래스 컨버터

## 1) 도메인 클래스 컨버터 사용 전

```java
    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id){
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }
```

<br>

## 2) 도메인 클래스 컨버터 사용 후

```java
@GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member){
        return member.getUsername();
    }
```

- HTTP 요청은 회원 `id`를 받는다. 하지만 도메인 클래스 컨버터가 중간에 동작하여 회원 엔티티 객체를 반환한다.

- 도메인 클래스 컨버터도 인젝션된 레포지토리를 사용해서 엔티티를 찾는다.

<br><Br>

### 주의
도메인 클래스 컨버터로 엔티티를 파라미터로 받으면, 단순 조회용으로 사용해야 한다. 트랜잭션이 없는 범위에서 엔티티를 조회했으므로, 엔티티를 변경해도 DB에 반영되지 않는다.

<Br><Br>

# 4. 웹 - 페이징과 정렬

```java
@GetMapping("/members")
public Page<Member> list(Pageable pageable) {
    Page<Member> page = memberRepository.findAll(pageable);
    return page;
}
```

- 파라미터로 `Pageable`을 받을 수 있다.

- `Pageable`은 인터페이스로 JPA가 PageRequest 구현 객체를 자동으로 생성해준다.

<br><Br>

## 1) 요청 파라미터
- 예) `/members?page=0&size=3&sort=id,desc&sort=username,desc`
- page: 현재 페이지, 0부터 시작한다.
- size: 한 페이지에 노출할 데이터 건수
- sort: 정렬 조건을 정의한다. 예) 정렬 속성,정렬 속성...(ASC | DESC), 정렬 방향을 변경하고 싶으면 sort파라미터 추가 ( asc 생략 가능)

<br><Br>

## 2) 글로벌 설정

- `application.yml`

```yml
spring.data.web.pageable.default-page-size: 20 /# 기본 페이지 사이즈/
spring.data.web.pageable.max-page-size: 2000 /# 최대 페이지 사이즈/
```

<Br><Br>

## 3) 개별 설정

- `@PageableDefault` 어노테이션 사용

```java
@GetMapping("/members/page")
    public Page<Member> list2(@PageableDefault(size = 12, sort = "username", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        return page;
    }
```

<br><Br>

## 4) 페이징 정보 둘 이상이면

- 접두사로 구분

- @Qualifier에 접두사명 추가 

- 예제: `/members?member_page=0&order_page=1`

<br>

```java
public String list(
 @Qualifier("member") Pageable memberPageable,
 @Qualifier("order") Pageable orderPageable, ...)
{
    ...
}
```

<br><Br>

## 5) 주의

- 엔티티를 API로 노출하면 문제 발생

- 반드시 `map`을 이용하여 DTO로 변환하여 반환

- page는 0부터 시작임.


















