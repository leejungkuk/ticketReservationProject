package com.self.ticketreservationproject.repository.show;

import com.self.ticketreservationproject.domain.show.ShowSeat;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long>, ShowSeatRepositoryCustom {

  List<ShowSeat> findByHoldUserIdAndSeatNumber(long holdUserId, String seatNumber);

  List<ShowSeat> findByHoldUserIdAndId(long holdUserId, long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  long holdSeat (long seatId, long userId, LocalDateTime now);

  long confirmSeat (long seatId, long userId);

  long releaseExpiredHolds(LocalDateTime expireTime);

  boolean isConfirmed(long seatId);

  long confirmSeat (long seatId, long userId, LocalDateTime holdTime); // redis용

  // query for test
  @Query("select s.showSchedule.id from ShowSeat s where s.id = :id")
  Long findScheduleIdById(@Param("id")Long id);
}
