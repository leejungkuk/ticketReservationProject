package com.self.ticketreservationproject.controller;

import com.self.ticketreservationproject.domain.show.ShowInfo;
import com.self.ticketreservationproject.dto.show.ShowRequest;
import com.self.ticketreservationproject.dto.show.ShowRequest.CreateScheduleRequest;
import com.self.ticketreservationproject.dto.show.ShowResponse.CreateScheduleResponse;
import com.self.ticketreservationproject.dto.show.ShowResponse.DeleteShowResponse;
import com.self.ticketreservationproject.dto.show.ShowResponse.ScheduleResponse;
import com.self.ticketreservationproject.dto.show.ShowResponse.SeatResponse;
import com.self.ticketreservationproject.dto.show.ShowResponse.SelectShowInfoResponse;
import com.self.ticketreservationproject.dto.show.ShowResponse.UploadShowInfoResponse;
import com.self.ticketreservationproject.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@Slf4j
public class ShowController {

  private final ShowService showService;

  @Operation(summary = "공연 정보 등록")
  @PostMapping
  public ResponseEntity<UploadShowInfoResponse> createShow(
      @Valid @RequestBody ShowRequest.UploadShowInfoRequest request) {
    ShowInfo showInfo = showService.createShow(request);

    UploadShowInfoResponse response = UploadShowInfoResponse.builder()
        .title(showInfo.getTitle())
        .description(showInfo.getDescription())
        .runtime(showInfo.getRuntime())
        .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공연 조회")
  @GetMapping
  public ResponseEntity<List<SelectShowInfoResponse>> selectShow(
      @RequestParam(required = false) String title) {
    List<ShowInfo> showInfos = showService.findShows(title);

    List<SelectShowInfoResponse> response = new ArrayList<>();
    for (ShowInfo showInfo : showInfos) {
      SelectShowInfoResponse dto = SelectShowInfoResponse.builder()
          .title(showInfo.getTitle())
          .description(showInfo.getDescription())
          .runtime(showInfo.getRuntime())
          .build();
      response.add(dto);
    }
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "특정 공연 일정 조회")
  @PreAuthorize("hasRole('USER')")
  @GetMapping("/{showId}")
  public ResponseEntity<SelectShowInfoResponse> selectShowWithSchedule(@PathVariable Long showId) {
    ShowInfo show = showService.findShowWithSchedule(showId);

    List<ScheduleResponse> schedules = show.getSchedules().stream()
        .map(s -> ScheduleResponse.builder()
            .id(s.getId())
            .startTime(s.getStartTime())
            .seats(s.getSeats().stream()
                .map(seat -> SeatResponse.builder()
                    .id(seat.getId())
                    .seatNum(seat.getSeatNumber())
                    .price(seat.getPrice())
                    .status(seat.getStatus().name())
                    .build()
                ).toList())
            .build())
        .toList();

    SelectShowInfoResponse response = SelectShowInfoResponse.builder()
        .title(show.getTitle())
        .description(show.getDescription())
        .runtime(show.getRuntime())
        .showSchedules(schedules)
        .build();
    return ResponseEntity.ok(response);
  }


  @Operation(summary = "공연 일정 등록")
  @PostMapping("/{showId}/schedule")
  public ResponseEntity<CreateScheduleResponse> createSchedule(@PathVariable Long showId,
      @RequestBody CreateScheduleRequest request) {

    ShowInfo createSchedule = showService.createShowSchedule(showId, request);

    List<ScheduleResponse> schedules = createSchedule.getSchedules().stream()
        .map(s -> ScheduleResponse.builder()
            .id(s.getId())
            .startTime(s.getStartTime())
            .seats(s.getSeats().stream()
                .map(seat -> SeatResponse.builder()
                    .id(seat.getId())
                    .seatNum(seat.getSeatNumber())
                    .price(seat.getPrice())
                    .status(seat.getStatus().name())
                    .build()
                ).toList())
            .build())
        .toList();

    CreateScheduleResponse response = CreateScheduleResponse.builder()
        .title(createSchedule.getTitle())
        .showSchedules(schedules)
        .runtime(createSchedule.getRuntime())
        .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공연 삭제")
  @DeleteMapping("/{showId}")
  public ResponseEntity<DeleteShowResponse> deleteShow(@PathVariable Long showId) {
    showService.deleteShow(showId);

    DeleteShowResponse response = DeleteShowResponse.builder()
        .message("삭제 완료되었습니다.")
        .build();
    return ResponseEntity.ok(response);
  }


}
