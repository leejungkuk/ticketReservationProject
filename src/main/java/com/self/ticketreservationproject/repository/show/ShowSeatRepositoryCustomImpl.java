package com.self.ticketreservationproject.repository.show;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.self.ticketreservationproject.domain.show.QShowSeat;
import com.self.ticketreservationproject.domain.show.SeatStatus;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ShowSeatRepositoryCustomImpl implements ShowSeatRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QShowSeat seat = QShowSeat.showSeat;

  @Override
  @Transactional
  public long holdSeat(long seatId, long userId, LocalDateTime now) {
    return queryFactory
        .update(seat)
        .set(seat.status, SeatStatus.HOLD)
        .set(seat.holdUserId, userId)
        .set(seat.holdStartTime, now)
        .where(seat.id.eq(seatId).and(seat.status.eq(SeatStatus.AVAILABLE)))
        .execute();
  }

  @Override
  @Transactional
  public long confirmSeat(long seatId, long userId) {
    return queryFactory
        .update(seat)
        .set(seat.status, SeatStatus.RESERVED)
        .set(seat.updatedAt, LocalDateTime.now())
        .where(seat.id.eq(seatId).and(seat.status.eq(SeatStatus.HOLD)))
        .execute();
  }

  @Override
  @Transactional
  public long releaseExpiredHolds(LocalDateTime expireTime) {
    return queryFactory
        .update(seat)
        .set(seat.status, SeatStatus.AVAILABLE)
        .set(seat.holdUserId, Expressions.nullExpression(long.class))
        .set(seat.holdStartTime, Expressions.nullExpression(LocalDateTime.class))
        .where(seat.status.eq(SeatStatus.HOLD).and(seat.holdStartTime.loe(expireTime)))
        .execute();
  }

  @Override
  public boolean isConfirmed(long seatId) {
    Integer count = queryFactory
        .selectOne()
        .from(seat)
        .where(seat.id.eq(seatId).and(seat.status.eq(SeatStatus.RESERVED)))
        .fetchFirst();

    return count != null;
  }

  @Override
  public long confirmSeatWithRedis(long seatId, long userId) {
    return queryFactory
        .update(seat)
        .set(seat.status, SeatStatus.RESERVED)
        .set(seat.updatedAt, LocalDateTime.now())
        .set(seat.holdUserId, userId)
//        .set(seat.holdStartTime, holdTime)
        .where(seat.id.eq(seatId).and(seat.status.eq(SeatStatus.AVAILABLE)))
        .execute();
  }


}
