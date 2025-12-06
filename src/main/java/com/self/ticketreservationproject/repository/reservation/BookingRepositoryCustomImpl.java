package com.self.ticketreservationproject.repository.reservation;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.self.ticketreservationproject.domain.reservation.QBooking;
import com.self.ticketreservationproject.domain.reservation.QReservationSeat;
import com.self.ticketreservationproject.domain.show.QShowInfo;
import com.self.ticketreservationproject.domain.show.QShowSchedule;
import com.self.ticketreservationproject.domain.show.QShowSeat;
import com.self.ticketreservationproject.dto.reservation.ReservationResponse.ConfirmResponse;
import com.self.ticketreservationproject.dto.show.ShowResponse.SeatResponse;
import com.self.ticketreservationproject.exception.custom.reservation.ReservationNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BookingRepositoryCustomImpl implements BookingRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QBooking booking = QBooking.booking;
  private final QReservationSeat reservationSeat = QReservationSeat.reservationSeat;
  private final QShowSeat showSeat = QShowSeat.showSeat;
  private final QShowSchedule showSchedule = QShowSchedule.showSchedule;
  private final QShowInfo showInfo = QShowInfo.showInfo;

  @Override
  public ConfirmResponse findReservationDetail(long reservationId) {
    List<Tuple> rows = queryFactory
        .select(
            booking.id,
            booking.userId,
            booking.createdAt,
            showSeat.id,
            showSeat.seatNumber,
            showSeat.price,
            showInfo.title,
            showSchedule.startTime
        )
        .from(booking)
        .join(reservationSeat).on(booking.id.eq(reservationSeat.bookingId))
        .join(showSeat).on(showSeat.id.eq(reservationSeat.seatId))
        .join(showSchedule).on(showSchedule.id.eq(showSeat.showSchedule.id))
        .join(showInfo).on(showInfo.id.eq(showSchedule.showInfo.id))
        .fetch();

    if(rows.isEmpty()) {
      throw new ReservationNotFoundException();
    }

    List<SeatResponse> seatDetails = rows.stream()
        .map(r -> SeatResponse.builder()
            .id(r.get(showSeat.id))
            .seatNum(r.get(showSeat.seatNumber))
            .price(r.get(showSeat.price))
            .build()
        ).toList();

    Tuple first = rows.get(0);

    return ConfirmResponse.builder()
        .reservationId(first.get(booking.id))
        .showTitle(first.get(showInfo.title))
        .showSchedule(first.get(showSchedule.startTime))
        .seats(seatDetails)
        .totalPrice(seatDetails.stream().map(SeatResponse::getPrice).reduce(0, Integer::sum))
        .reservedAt(first.get(booking.createdAt))
        .build();
  }
}
