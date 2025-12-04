package com.self.ticketreservationproject.service;

import com.self.ticketreservationproject.domain.reservation.Booking;
import com.self.ticketreservationproject.domain.show.ShowSeat;
import com.self.ticketreservationproject.dto.reservation.ReservationRequest.ConfirmRequest;
import com.self.ticketreservationproject.dto.reservation.ReservationRequest.ReserveRequest;
import com.self.ticketreservationproject.dto.reservation.ReservationResponse.ConfirmResponse;
import com.self.ticketreservationproject.exception.custom.reservation.ConfirmFailedException;
import com.self.ticketreservationproject.exception.custom.reservation.SeatAlreadyTakenException;
import com.self.ticketreservationproject.repository.reservation.BookingRepository;
import com.self.ticketreservationproject.repository.reservation.ReservationSeatRepository;
import com.self.ticketreservationproject.repository.show.ShowSeatRepository;
import com.self.ticketreservationproject.repository.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

  private final ShowSeatRepository showSeatRepository;
  private final BookingRepository bookingRepository;
  private final ReservationSeatRepository reservationSeatRepository;
  private final UserRepository userRepository;
  private static volatile LocalDateTime lastActivity = LocalDateTime.now();

  @Transactional
  public void reserveSeat(ReserveRequest request) {
    lastActivity = LocalDateTime.now();
    long userId = getUserId(request.getUsername());
    long updated = showSeatRepository.holdSeat(request.getSeatId(), userId, LocalDateTime.now());
    if (updated == 0) {
      throw new SeatAlreadyTakenException();
    }
  }

  @Transactional
  public ConfirmResponse confirmSeat(ConfirmRequest request) {
    lastActivity = LocalDateTime.now();
    long userId = getUserId(request.getUsername());

    List<Long> seats = request.getSeatIds();

    for (int i = 0; i < seats.size(); i++) {
      List<ShowSeat> exist = showSeatRepository.findByHoldUserIdAndId(userId, seats.get(i));
      if (exist.isEmpty() || exist.get(0).getHoldUserId() != userId) {
        throw new ConfirmFailedException();
      }

      long updated = showSeatRepository.confirmSeat(seats.get(i), userId);
      if (updated == 0) {
        throw new ConfirmFailedException();
      }
    }

    Booking saved = bookingRepository.save(request.toEntity(userId));

    for (long seat : seats) {
      reservationSeatRepository.save(request.toEntity(saved.getId(), seat));
    }

    return bookingRepository.findReservationDetail(saved.getId());
  }

  @Scheduled(fixedRate = 60000) // 1분마다 실행
  @Transactional
  public void releaseExpiredHolds() {
    if(lastActivity.isBefore(LocalDateTime.now().minusMinutes(10))) {
      log.info("Release expired holds");
      return; // 최근 10분 동안 예약 활동 없으면 자동해제 실행 X
    }

    LocalDateTime expireTime = LocalDateTime.now().minusMinutes(5);
    long released = showSeatRepository.releaseExpiredHolds(expireTime);
    if (released > 0) {
      log.info("자동 해제된 좌석 수 : " + released);
    }
  }

  public ConfirmResponse getReservationDetail(long reservationId, String username) {
    return bookingRepository.findReservationDetail(reservationId);
  }

  private long getUserId(String username) {
    long userId = userRepository.findUserIdByUsernameAndStatus(username, 'Y');
    return userId;
  }

}
