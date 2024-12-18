package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserPointServiceTest {

    @InjectMocks
    UserPointService userPointService;

    @Mock
    UserPointRepository userPointRepository;

    @Test
    public void 포인트_충전을_성공한다() {
        //given
        long id = 0L;
        long amount = 500;
        given(userPointRepository.selectById(anyLong())).willReturn(UserPoint.empty(id));
        given(userPointRepository.updateUserPoint(anyLong(), anyLong())).willReturn(new UserPoint(id, 500, System.currentTimeMillis()));
        given(userPointRepository.insertPointHistoryofCharge(anyLong(), anyLong())).willReturn(null);

        //when
        try {
            UserPoint userPoint = userPointService.chargeUserPoint(id, amount);

            //then
            assertThat(userPoint.point()).isEqualTo(500);

            verify(userPointRepository).updateUserPoint(eq(id), eq(amount));
            verify(userPointRepository).insertPointHistoryofCharge(eq(id), eq(amount));
        }catch(Exception e) {
            fail();
        }
    }

    @Test
    public void 포인트가_음수로_입력된다면_포인트_충전을_실패한다() {
        //given
        long id = 0L;
        long amount = -500;

        //when
        Exception exception = assertThrows(BadRequestException.class, () -> {
                    userPointService.chargeUserPoint(id, amount);
                });

        //then
        assertThat(exception.getMessage()).isEqualTo("적립할 포인트 금액이 유효하지 않습니다.");
    }

    @Test
    public void 충전_후_포인트_잔액이_최대_보유_가능한_잔액을_넘는다면_실패한다() {
        //given
        long id = 0L;
        long amount = 10_000_000;

        UserPoint givenUserPoint = new UserPoint(id, 1000, System.currentTimeMillis());

        given(userPointRepository.selectById(anyLong())).willReturn(givenUserPoint);
        given(userPointRepository.updateUserPoint(anyLong(), anyLong())).willReturn(new UserPoint(id, 10_001_000, System.currentTimeMillis()));


        //when
        Exception exception = assertThrows(BadRequestException.class, () -> {
            userPointService.chargeUserPoint(id, amount);
        });

        //then
        assertThat(exception.getMessage()).isEqualTo("최대 보유 가능한 포인트 잔액을 초과합니다.");
    }

    @Test
    public void 포인트_사용에_성공한다() {
        //given
        long id = 0L;
        long amount = 1000;

        UserPoint givenUserPoint = new UserPoint(id, 1000, System.currentTimeMillis());

        given(userPointRepository.selectById(anyLong())).willReturn(givenUserPoint);
        given(userPointRepository.updateUserPoint(anyLong(), anyLong())).willReturn(new UserPoint(id, 0, System.currentTimeMillis()));
        given(userPointRepository.insertPointHistoryofUse(anyLong(), anyLong())).willReturn(null);


        //when
        try {
            UserPoint userPoint = userPointService.useUserPoint(id, amount);

            //then
            assertThat(userPoint.point()).isEqualTo(0);

            verify(userPointRepository).updateUserPoint(eq(id), eq(0L));
            verify(userPointRepository).insertPointHistoryofUse(eq(id), eq(amount));
        } catch(Exception e) {
            fail();
        }
    }

    @Test
    public void 포인트가_음수로_입력된다면_포인트_사용에_실패한다() {
        //given
        long id = 0L;
        long amount = -100;

        UserPoint givenUserPoint = new UserPoint(id, 1000, System.currentTimeMillis());

        //when
        Exception exception = assertThrows(BadRequestException.class, () -> {
            userPointService.useUserPoint(id, amount);
        });

        //then
        assertThat(exception.getMessage()).isEqualTo("사용할 포인트 금액이 유효하지 않습니다.");
    }

    @Test
    public void 사용_후_포인트_잔액이_0_미만일_경우_실패한다() {
        //given
        long id = 0L;
        long amount = 100;

        given(userPointRepository.selectById(anyLong())).willReturn(UserPoint.empty(id));
        given(userPointRepository.updateUserPoint(anyLong(), anyLong())).willReturn(new UserPoint(id, -100, System.currentTimeMillis()));

        //when
        Exception exception = assertThrows(BadRequestException.class, () -> {
            userPointService.useUserPoint(id, amount);
        });

        //then
        assertThat(exception.getMessage()).isEqualTo("보유한 포인트 금액을 초과하여 사용할 수 없습니다.");
    }
}
