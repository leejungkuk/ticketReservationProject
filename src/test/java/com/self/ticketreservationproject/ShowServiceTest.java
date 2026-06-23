package com.self.ticketreservationproject;

import com.self.ticketreservationproject.domain.show.ShowInfo;
import com.self.ticketreservationproject.domain.show.ShowSchedule;
import com.self.ticketreservationproject.dto.show.ShowRequest.CreateScheduleRequest;
import com.self.ticketreservationproject.dto.show.ShowRequest.UploadShowInfoRequest;
import com.self.ticketreservationproject.service.ShowService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest()
@Transactional
public class ShowServiceTest {

  @Autowired
  private ShowService showService;

  @Test
  void createShowTest() {
    String title = uniqueTitle();
    UploadShowInfoRequest request = showRequest(title);

    ShowInfo show = showService.createShow(request);

    assertThat(show.getTitle()).isEqualTo(title);
    assertThat(show.getRuntime()).isEqualTo(100);
  }

  @Test
  void selectShowTest() {
    String title = uniqueTitle();
    showService.createShow(showRequest(title));

    List<ShowInfo> shows = showService.findShows(title);

    assertThat(shows.size()).isEqualTo(1);
    assertThat(shows.get(0).getTitle()).isEqualTo(title);
  }

  @Test
  void createShowScheduleWithSeatsTest() {
    ShowInfo show = showService.createShow(showRequest(uniqueTitle()));
    CreateScheduleRequest  request = new CreateScheduleRequest();
    List<LocalDateTime> startTimes = new ArrayList<>();
    startTimes.add(LocalDateTime.of(2026, 12, 1, 12, 0));
    request.setStartTimes(startTimes);

    ShowInfo scheduled = showService.createShowSchedule(show.getId(), request);
    ShowSchedule schedule = scheduled.getSchedules().get(0);

    assertThat(schedule.getSeats().size()).isEqualTo(50);
    assertThat(schedule.getSeats().get(0).getSeatNumber()).isEqualTo("A1");
  }

  @Test
  void deleteShowTest() {
    ShowInfo show = showService.createShow(showRequest(uniqueTitle()));

    showService.deleteShow(show.getId());
  }

  private UploadShowInfoRequest showRequest(String title) {
    UploadShowInfoRequest request = new UploadShowInfoRequest();
    request.setTitle(title);
    request.setDescription("description");
    request.setRuntime(100);
    return request;
  }

  private String uniqueTitle() {
    return "title-" + UUID.randomUUID();
  }
}
