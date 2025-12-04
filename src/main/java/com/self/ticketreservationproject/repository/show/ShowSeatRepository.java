package com.self.ticketreservationproject.repository.show;

import com.self.ticketreservationproject.domain.show.ShowSeat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long>, ShowSeatRepositoryCustom {

  List<ShowSeat> findByHoldUserIdAndSeatNumber(long holdUserId, String seatNumber);

  List<ShowSeat> findByHoldUserIdAndId(long holdUserId, long id);
}
