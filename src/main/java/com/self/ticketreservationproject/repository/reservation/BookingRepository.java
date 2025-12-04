package com.self.ticketreservationproject.repository.reservation;

import com.self.ticketreservationproject.domain.reservation.Booking;
import com.self.ticketreservationproject.dto.reservation.ReservationResponse.ConfirmResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, BookingRepositoryCustom {
  ConfirmResponse findReservationDetail(long reservationId);
}
