package com.self.ticketreservationproject.dto.reservation;

import com.self.ticketreservationproject.domain.reservation.Booking;
import com.self.ticketreservationproject.domain.reservation.BookingStatus;
import com.self.ticketreservationproject.domain.reservation.ReservationSeat;
import java.util.List;
import lombok.Data;

public class ReservationRequest {

  @Data
  public static class ReserveRequest {
    private String username;
    private Long seatId;
  }

  @Data
  public static class ConfirmRequest {
    private String username;
    private List<Long> seatIds;
    private Long scheduleId;
    private BookingStatus bookingStatus;

    public Booking toEntity(long userId) {
      return Booking.builder()
          .userId(userId)
          .status(BookingStatus.CONFIRMED)
          .scheduleId(scheduleId)
          .build();
    }

    public ReservationSeat toEntity(long bookingId, long seatId) {
      return ReservationSeat.builder()
          .bookingId(bookingId)
          .seatId(seatId)
          .build();
    }
  }


}
