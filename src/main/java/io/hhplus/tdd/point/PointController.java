package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.UserPointRequestDTO;
import io.hhplus.tdd.point.service.UserPointService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private final UserPointService userPointService;

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    /* 특정 유저의 포인트를 조회하는 기능 */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        //포인트 조회
        UserPoint userPoint = userPointService.getPointAmount(id);

        return userPoint;
    }

    /* 특정 유저의 포인트 충전/이용 내역을 조회하는 기능 */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        //포인트 내역 조회
        List<PointHistory> list = userPointService.getPointHistoryByUserId(id);

        return list;
    }

    /* 특정 유저의 포인트를 충전하는 기능 */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody UserPointRequestDTO requestDTO
            ) {
        return userPointService.chargeUserPoint(id, requestDTO.getAmount());
    }

    /* 특정 유저의 포인트를 사용하는 기능 */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody UserPointRequestDTO requestDTO
    ) {
        return userPointService.useUserPoint(id, requestDTO.getAmount());
    }
}
