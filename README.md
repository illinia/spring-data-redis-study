## redis 공식문서 공부

### Redis 데이터 유형 튜토리얼

#### Key
* 매우 긴 키는 좋지 않다. 메모리 측면과 키를 조회하는데 비용이 많이 든다.
* 매우 짧은 키는 좋지 않다.
* 스키마를 지켜라. 점이나 콜론을 사용하여 여러 단어로 된 필드를 사용한다.

```shell
set myKey someValue
get myKey
```

set 시 key 가 이미 존재하는 경우 저장된 기존 값들 대체한다.

값은 모든 종류의 문자열(바이너리 데이터 포함)이 될 수 있다. 예를 들어 값 안에 jpeg 이미지를 저장할 수 있다.

명령 set 에 추가 인수로 제공되는 옵션이 있다.

문자열이 Redis 의 기본 값이더라도 문자열을 사용하여 할 수 있는 작업들이 있다. 예로 원자 증가이다.

```shell
set counter 100
incr counter
incr counter
incrby counter 50
```

INCR 명령은 문자열 값을 정수로 분석하고 1씩 증가시킨 후 새값으로 설정한다.
원자적이라는 의미는 경합 상태에 빠지지 않는다.

단일 명령으로 여러 키의 값을 설정하거나 검색하는 기능은 대기 시간 단축에도 유용하다.
```shell
mset a 10 b 20 c 30
mget a b c
```

mget 은 값의 배열을 반환한다.

#### 키 변경, 쿼리
모든 유형의 키와 함께 사용할 수 있다.
exists 명령은 키가 데이터베이스에 존재하는지 여부를 알리기 위해 1 또는 0 을 반환하고
del 명령은 값이 무엇이든 키, 관련 값을 삭제한다.
```shell
set myKey hello
exists myKey
del myKey
exists myKey
```

type 명령어는 키에 저장된 종류의 값을 반환한다.
```redis
set myKey x
type myKey
del myKey
type myKey
```

#### 키 만료

키 만료 사용시 수명또는 TTL 이라하는 키의 제한 시간을 설정할 수 있다.
* 초, 밀리초를 사용하여 설정 가능
* 만료 시간 확인은 항상 1밀리초이다.
* 만료에 대한 정보는 디스크에 저장되고 Redis 종료 상태로 유지되는 시간은 경과된다.(키가 만료되는 날짜를 지정함.)

```redis
set key some-value
expire key 5
get key
```

```redis
set key 100 ex 10
ttl key
```

### Lists

Redis 리스트는 Linked List 로 구현된다. 리스트의 헤드, 테일에 새 요소를 추가하는 것은 같은 시간에 수행된다.

### Redis Lists

lpush 명령은 리스트의 왼쪽(헤드)에 새 요소를 추가하고 rpush 명령은 리스트의 오른쪽(테일)에 새 요소를 추가한다.
lrange 명령은 목록에서 요소 범위를 추출한다.

```redis
rpush mylist A
rpush mylist B
lpush mylist first
lrange mylist 0 -1
```
```redis
rpush mylist 1 2 3 4 5 "foo bar"
lrange mylist 0 -1
```

요소 팝은 리스트에서 요소를 검색하는 동시에 목록에서 제거하는 기능을 한다.
```redis
rpush mylist a b c
rpop mylist
```

#### 목록의 일반적인 사용 사례
* 소셜 네트워크에 게시한 최신 업데이트 기억하기
* 생산자가 항목을 리스트에 푸시하고 소비자가 해당 항목을 소비하고 작업을 실행하는
소비자-생산자 패턴을 사용하는 프로세스간의 통신이다.

#### 제한된 리스트
최신 항목을 저장하기 위해 리스트를 사용하기를 원한다.
제한 컬렉션을 사용하여 최신 N 항목만 기억하고 가장 오래된 항목은 버린다.
ltrim 명령은 lrange 와 유사하지만 지정된 요소 범위를 표시하는 대신 이 범위를 새 목록 값으로 설정한다.

```redis
rpush mylist 1 2 3 4 5
ltrim mylist 0 2
lrange mylist 0 -1
```

```redis
lpush mylist <some elements>
ltrim mylist 0 999
```

lrange 는 기술적으로 O(N) 명령이지만 헤드, 테일로 작은 범위로 엑세스하는 것은 상수시간 작업이다.

### 리스트 블로킹 작업

소비자-생산자 패턴으로 리스트를 사용할 때 목록이 비어있고 처리할 것이 없으르모 pop 시 null 을 반환할 수 있다.
이 경우 소비자는 잠시 기다릴 수 있으며 이것을 polling 이라 하며 여러 단점이 있다.
1. Redis 와 클라이언트가 쓸모 없는 명령을 처리하도록 강제한다.
2. 작업자가 null 을 수신한 후 일정 시간 대기하므로 항목 처리가 지연된다.

brpop 은 차단할 수 있는 버전이다. 새 요소가 목록에 추가되거나 지정한 시간 초과에 도달한 경우에만 호출자에게 반환된다.

```redis
brpop tasks 5
```

리스트의 요소를 기다리지만 5초후에 사용 가능한 요소가 없으면 반환한다.
0을 시간 제한으로 사용하여 요소를 영원히 기다릴 수 있고
여러 목록을 기다리고 첫 번째 목록이 요소를 수신할 때 알림을 받기 위해 여러 목록을 지정할 수 있다.

주의사항
1. 클라이언트에게 순서대로 제공된다. 리스트 대기를 차단할 첫 번재 클라이언트는 다른 클라이언트들에 의해 요소가 푸시될 때 먼저 제공된다.
2. 반환 값은 다를 수 있다. 여러 목록에서 요소를 기다릴 수 있기 때문
3. 제한 시간에 도달하면 null 이 반환된다.


* lmove 를 사용하여 안전한 큐 혹 순환 큐를 구축할 수 있다.
```shell
redis> RPUSH mylist "one"
(integer) 1
redis> RPUSH mylist "two"
(integer) 2
redis> RPUSH mylist "three"
(integer) 3
redis> LMOVE mylist myotherlist RIGHT LEFT
"three"
redis> LMOVE mylist myotherlist LEFT RIGHT
"one"
redis> LRANGE mylist 0 -1
1) "two"
redis> LRANGE myotherlist 0 -1
1) "three"
2) "one"
```
* blmove 라는 변형된 차단 명령도 있다.


### 키 자동 생성 및 제거

요소를 푸시하기 전에 빈 목록을 만들거나 내부에 더 이상 요소가 없을 때 빈 목록을 제거할 필요가 없었다.

1. 집계 데이터 유형에 요소를 추가할 때 대상 키가 없으면 요소를 추가하기 전에 빈 집계 데이터 유형이 생성된다.
```redis
del mylist
lpush mylist 1 2 3
```
키가 존재하는 경우 잘못된 유형에 대해 작업을 수행할 수 없다.
```redis
set foo bar
plush foo 1 2 3
type foo
```

2. 집계 데이터 유형에서 요소를 제거할 때 값이 비어 있으면 키가 자동으로 폐기된다.
```redis
lpush mylist 1 2 3
exists mylist
lpop mylist
lpop mylist
lpop mylist
exists mylist
```

3. llen(목록의 길이를 반환하는)와 같은 읽기 전용 명령을 호출하거나 
빈 키를 사용하여 요소를 제거하는 쓰기 명령을 호출하면 키가 명령 유형의 빈 집계 유형을 보유하고 있는 것처럼 동작한다.
```redis
del mylist
llen mylist
lpop mylist
```

### 해시
```redis
hset user:1000 username antirez birthyear 1977 verified 1
hget user:1000 username
hget user:1000 birthyear
hgetall user:1000
```

```redis
hmget user:1000 username birthyear no-such-field
```

```redis
hincrby user:1000 birthyear 10
hincrby user:1000 birthyear 10
```

### set
정렬되지 않은 문자열 모음
```redis
sadd myset 1 2 3
smembers myset
```

```redis
sismember myset 3
sismember myset 30
```

집합은 객체간에 관계를 표현하는데 좋다. 예를 들어 태그를 구현하기 위해 집합을 쉽게 사용할 수 있다.
```redis
sadd news:1000:tags 1 2 5 77

sadd tag:1:news 1000
sadd tag:2:news 1000
sadd tag:5:news 1000
sadd tag:77:news 1000
```

서로 다른 집합 간의 교집합을 구하는 명령을 사용할 수 있다.
```redis
sinter tag:1:news tag:2:news
```

```redis
sadd deck C1 C2 C3 C4 C5 C6 C7 C8 C9 C10 CJ CQ CK
  D1 D2 D3 D4 D5 D6 D7 D8 D9 D10 DJ DQ DK H1 H2 H3
  H4 H5 H6 H7 H8 H9 H10 HJ HQ HK S1 S2 S3 S4 S5 S6
  S7 S8 S9 S10 SJ SQ SK
  
  sunionstore game:1:deck deck
  
  spop game:1:deck
  spop game:1:deck
  spop game:1:deck
  spop game:1:deck
  spop game:1:deck
  
  scard game:1:deck
```

### sorted set
고유하고 반복되지 않는 문자열 요소로 구성된다.
모든 요소는 score 라고하는 부동 소수점 값에 연결되어 정렬된다.

* A.score > B.score 이면 A > B 이다
* score 가 같으면 A 문자열이 사전식으로 B 문자열보다 크면 A > B 이다.

```redis
zadd hackers 1940 "Alan Kay"
zadd hackers 1957 "Sophie Wilson"
```

score 가 추가 인수로 추가할 요소 앞에 위치한다.
sorted set 은 요소를 추가할 때 마다 O(log(N)) 작업을 수행한다.
하지만 정렬된 요소를 요청할 때는 작업을 수행할 필요가 없다.
```redis
zrange hackers 0 -1
zrange hackers 0 -1 withscores
```

#### 범위로 작동
```redis
zrangebyscore hackers -inf 1950
zremrangebyscore hackers 1940 1960
zrank hackers "Sophie Wilson"
```

#### 사전식 점수
정렬된 집합의 모든 요소가 동일한 점수로 삽입된다고 가정시 사전순으로 범위를 가져올 수 있는 기능이 있다.

```redis
zadd hackers 0 "Alan Kay" 0 "Sophie Wilson" 0 "Richard Stallman" 0
  "Anita Borg" 0 "Yukihiro Matsumoto" 0 "Hedy Lamarr" 0 "Claude Shannon"
  0 "Linus Torvalds" 0 "Alan Turing"
  
zrange hackers 0 -1
zrangebylex hackers [B [P
```

### 점수 업데이트
정렬된 집합의 점수는 언제든 업데이트할 수 있다. 이미 포함된 요소에 대해 업데이트하면 O(log(N)) 이다.
일반적인 사용 사례는 점수 보드이다

### 비트맵
문자열 유형에 정의된 일련의 비트 지향 작업이다.
정보 저장시 공간을 크게 절약할 수 있다.
```redis
setbit key 10 1
getbit key 10
getbit key 11
```

1. bitop 서로 다른 문자열 간에 비트 연산을 수행한다. and or xor not
2. bitcount 카운트를 수행하여 1로 설정된 비트 수를 보고한다.
3. bitpos 지정된 값이 0 또는 1인 첫 번째 비트를 찾는다.
```redis
setbit key 0 1
setbit key 100 1
bitcount key
```

* 모든 종류의 실시간 분석
* 개체 ID 와 관련된 공간 효율적인 고성능 부울 정보 저장


### Redis 보안
1. Redis 가 연결을 수행하는 데 사용하는 포트(6379, 16379, 26379) 가 방화벽으로 설정되어 외부에서 연결할 수 없게하라.
2. 사용중인 네트워크 인터페이스에서만 수신하도록 보장하기 위해 지시문이 설정된 구성 파일을 사용하라.
3. requirepass 명령을 사용하여 인증해야 하도록 추가 보안 계층을 추가하려면 AUTH 옵션을 사용해라.

### 레디스 지속성
기본 구성 시작시 데이터를 자발적으로 저장한다.
데이터베이스를 유지하고 다시 시작한 후 다시 로드하려면 데이터 스냅샷을 강제 적용할 때마다 save 명령을 수동으로 호출해야 한다.
그렇지 않으면 shutdown 명령을 사용하여 종료해야 한다. 그러면 종료전에 데이터를 디스크에 저장한다.


## spring-data-redis 공식문서 공부
### 10.1 시작하기
build.gradle 에 의존성 추가
```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

```shell
brew install redis

docker image pull redis:6.2-alpine
docker run --name redis-local -d -p 6379:6379 redis:6.2-alpine
docker exec -it redis-local sh

redis-cli ping
```

#### 10.4.2 Lettuce 커넥터 구성

#### 10.4.4 마스터에 쓰기, 복제본에서 읽기
자동 장애 조치가 없는 마스터/복제본 설정을 통해 데이터를 더 많은 노드에 안전하게 저장할 수 있다.
Lettuce 를 사용해 마스터에 쓰기를 푸시하는 동안 복제본에서 데이터를 읽을 수 있다.

propertySource 로도 RedisSentinelConfiguration 을 정의할 수 있다.

센티넬중 하나와 직접적인 상호작용이 필요하다.
RedisConnectionFactory.getSentinelConnection() 나
RedisConnection.getSentinelCommands() 를 사용하면 첫 번째 활성 Sentinel 에 대한 엑세스 권한을 제공한다.


### 10.6 RedisTemplate 을 통한 개체 작업

### 10.7 문자열 중심 편의 클래스
저장된 키와 값이 매우 일반적이기 때문에 문자열 작업을 위한 각각 구현에 대한 RedisConnection, RedisTemplate 을 제공한다.

다른 스프링 템플릿과 마찬가지로 RedisTemplate, StringRedisTemplate 은 RedisCallback 인터페이스를 통해 대화할 수 있다.
이 기능은 RedisConnection 과 대화하는 것 처럼 완벽한 제어권을 준다.

### 10.8 직렬 변환기
1. RedisSerializer 에 기반한 양방향 직렬 변환기
2. RedisElementReader 와 RedisElementWriter 를 사용하는 요소 reader, writer

가장 큰 차이점은 RedisSerializer 는 byte[] 로 직렬화하고 readers, writers 는 ByteBuffer 를 사용한다.

### 10.9 해시 매핑
Redis 내에서 다양한 데이터 구조를 사용하여 데이터를 저장할 수 있다.
JSON 형식의 개체를 Jackson2JsonRedisSerializer 로 변환할 수 있다.
1. HashOperations, Serializer 를 사용한 직접 매핑
2. Redis Repositories 사용
3. HashMapper, HashOperations 사용

#### 10.9.1 해시 매퍼
해시 매퍼는 map 객체를 Map<K, V> 로 양방향 변환할 수 있다.
1. BeanUtilsHashMapper
2. ObjectHashMapper
3. Jackson2HashMapper

#### 10.9.2 Jackson2HashMapper

일반 매핑
* name: Jon
* surname: Snow
* address: {"city" : "Castle Black", "country" : "The North"}
* date: 123123
* localDateTime: 2018-01-02T12:34:56

플랫 매핑
* name: Jon
* surname: Snow
* address.city: Castle Black
* address.country: The North

평면화는 JSON 경로를 방해하지 않는 모든 속성의 이름이 사용될 수 있다.
맵 키 또는 속성 이름으로 점, 괄호를 사용하는 것은 지원되지 않는다.
결과 해시는 객체에 다시 매핑할 수 없다.

### 10.12 레디스 트랜잭션
레디스는 multi, exec, discard 명령어를 통해 트랜잭션을 제공한다. RedisTemplate 에서 가능하다.
하지만 RedisTemplate 은 트랜잭션에서 모든 연산을 같은 커넥션에서 보장하지 않는다.