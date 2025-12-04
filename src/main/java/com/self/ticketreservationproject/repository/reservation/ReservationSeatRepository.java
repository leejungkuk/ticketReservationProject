package com.self.ticketreservationproject.repository.reservation;

import com.self.ticketreservationproject.domain.reservation.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

}
