package io.hhplus.tdd.point.lock;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Map;
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
        //userPointLocks.remove(userId); //TODO remove 안 하면 Map에 계속 데이터가 쌓일텐데 이 부분에 대한 처리는 어떻게 할 지에 대해 생각하기
    }
}
