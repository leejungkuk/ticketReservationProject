package com.self.ticketreservationproject.controller;

import com.self.ticketreservationproject.dto.reservation.ReservationRequest.ConfirmRequest;
import com.self.ticketreservationproject.dto.reservation.ReservationRequest.ReserveRequest;
import com.self.ticketreservationproject.dto.reservation.ReservationResponse.ConfirmResponse;
import com.self.ticketreservationproject.dto.reservation.ReservationResponse.ReserveResponse;
import com.self.ticketreservationproject.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class ReservationController {

  private final ReservationService reservationService;

  @Operation(summary = "좌석 HOLD")
  @PostMapping("/db/reserve")
  public ResponseEntity<ReserveResponse> reserve(@RequestBody ReserveRequest request) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    request.setUsername(username);

    reservationService.reserveSeat(request);

    ReserveResponse response = ReserveResponse.builder()
        .message("좌석 HOLD 완료")
        .build();

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "좌석 예매 확정")
  @PostMapping("/db/confirm")
  public ResponseEntity<ConfirmResponse> confirm(@RequestBody ConfirmRequest request) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    request.setUsername(username);

    ConfirmResponse response = reservationService.confirmSeat(request);
    response.setMessage("예매 완료되었습니다.");

    return ResponseEntity.ok(response);
  }

  @Operation
  @GetMapping("/{reservationId}")
  public ResponseEntity<ConfirmResponse> getReservation(@PathVariable("reservationId") long reservationId) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();

    ConfirmResponse response = reservationService.getReservationDetail(reservationId, username);

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "좌석 HOLD with Redis")
  @PostMapping("/redis/reserve")
  public ResponseEntity<ReserveResponse> reserveWithRedis(@RequestBody ReserveRequest request) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    request.setUsername(username);

    reservationService.reserveSeatWithRedis(request);

    ReserveResponse response = ReserveResponse.builder()
        .message("좌석 HOLD 완료")
        .build();

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "좌석 예매 확정 with Redis")
  @PostMapping("/redis/confirm")
  public ResponseEntity<ConfirmResponse> confirmWithRedis(@RequestBody ConfirmRequest request) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    request.setUsername(username);

    ConfirmResponse response = reservationService.confirmSeatWithRedis(request);
    response.setMessage("예매 완료되었습니다.");

    return ResponseEntity.ok(response);
  }

}
