package com.self.ticketreservationproject.repository.show;

import java.time.LocalDateTime;

public interface ShowSeatRepositoryCustom {
  long holdSeat (long seatId, long userId, LocalDateTime now);

  long confirmSeat (long seatId, long userId);

  long releaseExpiredHolds(LocalDateTime expireTime);

  boolean isConfirmed(long seatId);

  long confirmSeatWithRedis (long seatId, long userId);
}
