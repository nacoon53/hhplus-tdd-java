package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPointService {
    private final UserPointRepository userPointRepository;

    private final long MAXIMUM_POINT = 10_000_000;

    public UserPoint getPointAmount(long id) {
        return userPointRepository.selectById(id);
    }

    //유저가 가진 전체 포인트 내역을 조회한다.
    public List<PointHistory> getPointHistoryByUserId(long id) {
        return userPointRepository.selectAllByUserId(id);
    }

    public UserPoint chargeUserPoint(long id, long amount) throws IllegalArgumentException {
        if(amount < 0 || amount > MAXIMUM_POINT) { //todo(나경) 이거는 객체의 책임으로 가져갈수 있을 것 같음. 더 고민해 보기
            throw new IllegalArgumentException("적립할 포인트 금액이 유효하지 않습니다.");
        }

        UserPoint savedUserPoint = getPointAmount(id);

        long willUpdatePointAmount = savedUserPoint.point() + amount;
        if (willUpdatePointAmount > MAXIMUM_POINT) {
            throw new IllegalArgumentException("최대 보유 가능한 포인트 잔액을 초과합니다.");
        }

        UserPoint updatedUserPoint = userPointRepository.updateUserPoint(id, willUpdatePointAmount);

        userPointRepository.insertPointHistoryofCharge(id, amount);

        return updatedUserPoint;
    }

    public UserPoint useUserPoint(long id, long amount) throws IllegalArgumentException{
        if(amount < 0 || amount > MAXIMUM_POINT) { //todo(나경) 이거는 객체의 책임으로 가져갈수 있을 것 같음. 더 고민해 보기
            throw new IllegalArgumentException("사용할 포인트 금액이 유효하지 않습니다.");
        }

        UserPoint savedUserPoint = getPointAmount(id);

        long willUpdatePointAmount = savedUserPoint.point() - amount;
        if (willUpdatePointAmount < 0) {
            throw new IllegalArgumentException("보유한 포인트 금액을 초과하여 사용할 수 없습니다.");
        }

        UserPoint updatedUserPoint = userPointRepository.updateUserPoint(id, willUpdatePointAmount);

        userPointRepository.insertPointHistoryofUse(id, amount);

        return updatedUserPoint;
    }
}
