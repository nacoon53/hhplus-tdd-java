# 요구사항 분석
## 주어진 요구 사항

- PATCH  `/point/{id}/charge` : 포인트를 충전한다.
- PATCH `/point/{id}/use` : 포인트를 사용한다.
- *GET `/point/{id}` : 포인트를 조회한다.*
- *GET `/point/{id}/histories` : 포인트 내역을 조회한다.*
- *잔고가 부족할 경우, 포인트 사용은 실패하여야 합니다.*
- *동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.*

<br>

## 요구 사항 분석 및 구체화

### 기능

1. **포인트 충전**
    - 유효한 회원에게 입력 받은 금액을 회원의 포인트 잔고에 추가한다.
    - 입력 받은 금액의 범위는 0원 이상 10,000,000원 이하로 설정된다.
2. **포인트 사용**
    - 유효한 회원이 가진 포인트 잔고에서 입력 받은 금액만큼 차감한다.
    - 입력 받은 금액의 범위는 0원 이상 10,000,000원 이하로 설정된다.
3. **포인트 조회**
    - 유효한 회원의 포인트 잔고를 조회하여 보여준다.
    - 만약 유효한 회원이 포인트 잔고를 보유하지 않으면, '0' 을 표시한다.
4. **포인트 내역 조회**
    - 유효한 회원의 포인트 내역 테이블에서 내역을 조회한다.

### 기타

1. **포인트 충전/사용 처리 순서**
    - 여러 건의 포인트 충전 또는 사용 요청은 순차적으로 처리되어야 한다.
    - 충전 및 사용은 하나의 유저 내에서 순차적으로 이루어진다.
  

<br>
<hr>
<br>

# 동시성 제어 기능을 추가하면서…
## 1. 동시성 제어란?

  동시성 제어란 여러 명의 사용자(쓰레드)가 하나의 자원을 접근해야 하는 환경에서 데이터의 일관성을 유지하고, 충돌을 방지하기 위해 필요한 매커니즘 입니다.  

  예를 들어, 두 개의 쓰레드가 동일한 계정에 접근하여 포인트 사용 기능을 호출할 때, 동시성 제어가 되어 있지 않으면 각각 호출 시점에 가져온 포인트 잔액에서 사용 처리 하게 되어 최종 결과로는 음수가 발생할 수 있습니다. 만약, 동시성 제어가 되어 있다면 포인트 잔액이라는 하나의 자원에 대해 한 번의 접근만 허용하기 때문에 이러한 문제를 해결할 수 있습니다.

<br>

## 2. 동시성 제어를 하기 위해 생각한 방법

  이번 프로젝트에서는 사용자의 포인트 충전과 사용에 동시성 제어 기능을 추가하기로 하였습니다.

### 2.1 synchronized

### **synchronized란**

  Java에서 동기화를 지원하는 키워드로, 간단하게 특정 코드 블록이나 메서드에 대해 하나의 쓰레드만 접근하도록 제어하는 기능입니다.

### **synchronized를 사용하면 안 되는 이유**

  synchronized를 사용하게 되면 회원의 아이디 별로 동시성 제어가 생기는 것이 아니라 기능(메서드) 자체에 동시성 제어가 생기게 됩니다. 만약, A라는 유저가 포인트 사용을 할 때, B라는 유저가 동시에 포인트 사용을 하지 못하게 되는 현상이 발생되며, 이런 경우 회원들의 불만을 초래할 수 있습니다. 해당 프로젝트에서는 회원의 ID별로 락을 거는 방법이 필요해서 synchronized는 적합하지 않았습니다.

### 2.2 ReentrantLock

### **ReentrantLock이란**

  ReentrantLock은 synchronized와 유사하게 동기화를 제공하지만, 더 세밀하고 유연한 제어가 가능합니다. 이름 그대로 "재진입 가능한" 특성을 가지며, 특정 조건에서 Lock을 해제한 후 다시 획득하여 이후 작업을 이어나갈 수 있도록 지원합니다.

<br>

## 3. 실제 소스에 적용

### 3.1 예제 코드

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class UserPointLockManager {
    private static final Map<Long, Lock> userPointLocks = new ConcurrentHashMap<>();

    private static Lock getLock(long userId) {
        return userPointLocks.computeIfAbsent(userId, k -> new ReentrantLock(true));
    }

    public static void lock(long userId) {
        Lock lock = getLock(userId);
        lock.lock();
    }

    public static void unlock(long userId) {
        Lock lock = getLock(userId);
        
        if(ObjectUtils.isEmpty(lock)) {
            throw new RuntimeException("존재하지 않는 Lock 객체 입니다.");
        }

        lock.unlock();
    }
}
```

### 3.2 ID에 대한 Lock 필요성

  앞에서 언급한 대로 포인트 사용/충전 시 A 유저의 행동에 B 유저에게 영향이 가면 안되므로, ID별로 개별적인 락 구현이 필요했습니다. 이를 해결하기 위해 `ConcurrentHashMap` 을 도입하였고, 각 ID 별로 개별적인 락을 관리하도록 구현하였습니다.

### 3.3 LockManager 클래스 추가

  처음엔 Lock에 대한 내용을 Service 코드에 직접 넣었습니다. Lock 관리에 대한 책임을 별도의 클래스에 분산시키면 더 좋을 것 같다고 판단되어 LockManager 클래스를 추가하였습니다. LockManager에게 책임을 분산시키면서 서비스 코드에는 기능 구현에 더 중점을 두었습니다.

<br>

## 4. 결론

  동시성 제어 방식으로 synchronized와 ReentrantLock을 비교한 결과, 세밀한 동작 제어가 가능한 ReentrantLock을 사용하는 것이 적합하다는 결론에 도달했습니다. ConcurrentHashMap을 함께 활용하여 ID별 동시성 제어를 구현하였으며, LockManager 클래스를 추가하여 락 관리의 책임을 분리함으로써 코드의 가독성과 유지보수성을 크게 향상시킬 수 있었습니다.
    - 여러 건의 포인트 충전 또는 사용 요청은 순차적으로 처리되어야 한다.
    - 충전 및 사용은 하나의 유저 내에서 순차적으로 이루어진다.
