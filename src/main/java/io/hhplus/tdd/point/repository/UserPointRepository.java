package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserPointRepository {
    UserPointTable userPointTable;
    PointHistoryTable pointHistoryTable;

    public UserPointRepository(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }


    public UserPoint selectById(long id) {
        return userPointTable.selectById(id);
    }

    public List<PointHistory> selectAllByUserId(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint updateUserPoint(long id, long amount) {
        return userPointTable.insertOrUpdate(id, amount);
    }

    public PointHistory insertPointHistoryofCharge(long id, long amount) {
        return pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
    }

    public PointHistory insertPointHistoryofUse(long id, long amount) {
        return pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
    }
}
