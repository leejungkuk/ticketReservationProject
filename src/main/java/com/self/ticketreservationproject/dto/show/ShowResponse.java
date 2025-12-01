package com.self.ticketreservationproject.dto.show;

import com.self.ticketreservationproject.domain.show.ShowSchedule;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class ShowResponse {
  @Data
  @Builder
  public static class UploadShowInfoResponse {
    private String title;
    private String description;
    private int runtime;
  }

  @Data
  @Builder
  public static class SelectShowInfoResponse {
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private List<ScheduleResponse> showSchedules;
    private int runtime;
  }

  @Data
  @Builder
  public static class CreateScheduleResponse {
    private String title;
    private List<ShowSchedule> showSchedules;
    private int runtime;
  }

  @Data
  @Builder
  public static class DeleteShowResponse {
    private String message;
  }

  @Data
  @Builder
  public static class ScheduleResponse {
    private Long id;
    private LocalDateTime startTime;
    private List<SeatResponse> seats;
  }

  @Data
  @Builder
  public static class SeatResponse {
    private Long id;
    private String seatNum;
    private int price;
    private String status;
  }

}
