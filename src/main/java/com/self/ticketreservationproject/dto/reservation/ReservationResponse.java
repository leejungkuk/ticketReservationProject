package com.self.ticketreservationproject.dto.reservation;

import com.self.ticketreservationproject.dto.show.ShowResponse.SeatResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class ReservationResponse {

  @Data
  @Builder
  public static class ReserveResponse{
    private String message;
  }

  @Data
  @Builder
  public static class ConfirmResponse{
    private Long reservationId;
    private String showTitle;
    private LocalDateTime showSchedule;
    private List<SeatResponse> seats;
    private int totalPrice;
    private LocalDateTime reservedAt;
    private String message;
  }
}
