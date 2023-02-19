# Hint & Lock
 
쿼리 힌트 기능은 JPA가 제공하는 것이 아니라 하이버네이트가 제공하는 기능이다.

우리는 지금까지 DB에 쿼리를 날리게 되면 2개의 중복된 데이터를 보관하고 있었다.

그 이유는 update 쿼리가 발생 할 수 있으므로, 원본 데이터를 보관해야 하기 때문!


<br><Br>

즉 데이터의 변동이 생기면 영속성 컨텍스트에서 원본데이터와 비교한 후 update쿼리가 생성되는 것이다.

반대로 말하면 수정하지 않는 데이터는 원본데이터(스냅샷)을 저장하고 있을 필요가 없다. 

이 경우 하이버네이트에서는 JPA구현체에게 힌트를 제공하여 중복 데이터 문제를 해결 할 수 있다.

<br><Br>


## 1. 쿼리 힌트

```java
@QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);
```

위와 같이 쿼리 힌트를 readOnly로 하게 되면, 스냅샷을 저장하고 있지 않는다.

대신, 해당 쿼리로 조회한 데이터는 당연하게도 수정할 수 없다.
강제로 수정 코드를 넣는다고 해도 JPA가 무시해 버린다.

@QueryHint에서 볼수 있듯이, name 속성도 String이고, Value 속성도 String인것을 볼 수 있다.

즉, 해당 기능 말고도 여러가지 힌트를 JPA에게 제공할 수 있음을 의미한다.

<br><Br>

## 2. Lock

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
List<Member> findByUsername(String name);
```

Lock은 해당 데이터를 수정하기위해 조회를 할때 사용한다.

여러 사용자가 해당 데이터에 동시에 접근했을때 데이터의 정합성에 문제가 발생 할 수 있기 때문에, 해당 데이터에 Lock을 걸어 동시성 제어를 해주는 것이다.

락에 대한 더 자세한 내용은 JPA책 16.1 트랜잭션과 락 절을 참고하자.


