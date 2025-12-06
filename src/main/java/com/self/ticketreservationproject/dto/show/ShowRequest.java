package com.self.ticketreservationproject.dto.show;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.self.ticketreservationproject.domain.show.ShowInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

public class ShowRequest {

  @Data
  public static class UploadShowInfoRequest {
    @NotBlank
    @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하여야 합니다.")
    private String title;
    private String description;
    @NotNull
    private int runtime;

    public ShowInfo toEntity() {
      return ShowInfo.builder()
          .title(this.title)
          .description(this.description)
          .runtime(this.runtime)
          .build();
    }
  }

  @Data
  public static class CreateScheduleRequest {
    private String title;
    @NotEmpty
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private List<LocalDateTime> startTimes;
  }

  @Data
  public static class SeatReserveRequest {
    @NotEmpty
    private List<Long> seatsIds;
    @NotNull
    private String userId;
  }

}
