package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPointService {
    UserPointRepository userPointRepository;

    long maximumPoint = 10000000;

    public UserPointService(UserPointRepository userPointRepository) {
        this.userPointRepository = userPointRepository;
    }

    public UserPoint getPointAmount(long id) {
        return userPointRepository.selectById(id);
    }

    //유저가 가진 전체 포인트 내역을 조회한다.
    public List<PointHistory> getPointHistoryByUserId(long id) {
        return userPointRepository.selectAllByUserId(id);
    }

    public UserPoint chargeUserPoint(long id, long amount) throws BadRequestException{
        if(amount < 0 || amount > maximumPoint) { //todo(나경) 이거는 객체의 책임으로 가져갈수 있을 것 같음. 더 고민해 보기
            throw new BadRequestException("적립할 포인트 금액이 유효하지 않습니다.");
        }

        UserPoint savedUserPoint = getPointAmount(id);
        UserPoint updatedUserPoint = userPointRepository.updateUserPoint(id, savedUserPoint.point() + amount); //tood 조회 포인트 객체와 인서트 포인트 객체 따로 두는게 나을듯

        userPointRepository.insertPointHistoryofCharge(id, amount);

        if(updatedUserPoint.point() > maximumPoint) {
           throw new BadRequestException("최대 보유 가능한 포인트 잔액을 초과합니다.");
        }
        return updatedUserPoint;
    }

    public UserPoint useUserPoint(long id, long amount) throws BadRequestException{
        if(amount < 0 || amount > maximumPoint) { //todo(나경) 이거는 객체의 책임으로 가져갈수 있을 것 같음. 더 고민해 보기
            throw new BadRequestException("사용할 포인트 금액이 유효하지 않습니다.");
        }

        UserPoint savedUserPoint = getPointAmount(id);
        UserPoint updatedUserPoint = userPointRepository.updateUserPoint(id, savedUserPoint.point() - amount); //tood 조회 포인트 객체와 인서트 포인트 객체 따로 두는게 나을듯

        userPointRepository.insertPointHistoryofUse(id, amount);

        if(updatedUserPoint.point() < 0) {
            throw new BadRequestException("보유한 포인트 금액을 초과하여 사용할 수 없습니다.");
        }

        return updatedUserPoint;
    }
}