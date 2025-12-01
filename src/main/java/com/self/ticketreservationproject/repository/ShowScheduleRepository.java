package com.self.ticketreservationproject.repository;

import com.self.ticketreservationproject.domain.show.ShowSchedule;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowScheduleRepository extends JpaRepository<ShowSchedule, Long> {
  boolean existsByShowInfoIdAndStartTime(Long showId, LocalDateTime startTime);
}
