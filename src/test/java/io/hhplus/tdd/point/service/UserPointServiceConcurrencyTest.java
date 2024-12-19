package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
class UserPointServiceConcurrencyTest {

    UserPointService userPointService;

    @BeforeEach
    public void setup() {
        UserPointTable userPointTable = new UserPointTable();
        PointHistoryTable pointHistoryTable = new PointHistoryTable();

        UserPointRepository userPointRepository = new UserPointRepository(userPointTable, pointHistoryTable);
        userPointService = new UserPointService(userPointRepository);
    }

    @Test
    public void 동시에_여러_스레드가_포인트를_충전해도_최종_포인트_값이_일치해야_한다() throws InterruptedException {
        //given
        long id = 1L;
        long chargeAmount = 100L;

        int threadCount = 10;

        //when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //여러 스레드에서 충전 작업 실행
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    userPointService.chargeUserPoint(id, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 작업 완료 대기
        latch.await();
        executorService.shutdown();

        //then
        long expectedPoint = chargeAmount * threadCount;
        UserPoint userPoint = userPointService.getPointAmount(id);

        assertThat(userPoint.point()).isEqualTo(expectedPoint);
    }

    @Test
    public void 동시에_여러_스레드가_포인트를_사용해도_최종_포인트_값이_일치해야_한다() throws InterruptedException {
        //given
        long id = 1L;
        long useAmount = 100L;
        long initialPoint = 1000L;

        int threadCount = 10;

        userPointService.chargeUserPoint(id, initialPoint);

        //when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //여러 스레드에서 충전 작업 실행
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    userPointService.useUserPoint(id, useAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 작업 완료 대기
        latch.await();
        executorService.shutdown();

        //then
        long expectedPoint = 0;
        UserPoint userPoint = userPointService.getPointAmount(id);

        assertThat(userPoint.point()).isEqualTo(expectedPoint);
    }

    @Test
    public void 동시에_여러_스레드가_포인트를_충전하고_사용해도_최종_포인트_값이_일치해야_한다() throws InterruptedException {
        //given
        long id = 1L;
        long amount = 100L;

        int threadCount = 10;


        //when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //여러 스레드에서 충전 작업 실행
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    userPointService.chargeUserPoint(id, amount);

                    userPointService.useUserPoint(id, amount);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 작업 완료 대기
        latch.await();
        executorService.shutdown();

        //then
        long expectedPoint = 0;
        UserPoint userPoint = userPointService.getPointAmount(id);

        assertThat(userPoint.point()).isEqualTo(expectedPoint);
    }
}

