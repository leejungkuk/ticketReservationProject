package com.self.ticketreservationproject.repository.reservation;

import com.self.ticketreservationproject.dto.reservation.ReservationResponse.ConfirmResponse;

public interface BookingRepositoryCustom {
  ConfirmResponse findReservationDetail(long reservationId);
}
