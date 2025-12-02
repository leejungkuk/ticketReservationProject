package com.self.ticketreservationproject;

import com.self.ticketreservationproject.domain.show.ShowInfo;
import com.self.ticketreservationproject.domain.show.ShowSchedule;
import com.self.ticketreservationproject.dto.show.ShowRequest.CreateScheduleRequest;
import com.self.ticketreservationproject.dto.show.ShowRequest.UploadShowInfoRequest;
import com.self.ticketreservationproject.service.ShowService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
public class ShowServiceTest {

  @Autowired
  private ShowService showService;

  @Test
  void createShowTest() {
    UploadShowInfoRequest request = new UploadShowInfoRequest();
    request.setTitle("title2");
    request.setDescription("description2");
    request.setRuntime(100);

    showService.createShow(request);
  }

  @Test
  void selectShowTest() {
    String title = "title";
    List<ShowInfo> shows = showService.findShows(title);
    for(ShowInfo show : shows) {
      System.out.println(show.getId());
      System.out.println(show.getTitle());
      System.out.println(show.getDescription());
      System.out.println(show.getRuntime());
      for(ShowSchedule schedule : show.getSchedules()) {
        System.out.println(schedule.getId());
        System.out.println(schedule.getShowInfo().getId());
        System.out.println(schedule.getSeats());
      }
    }
  }

  @Test
  void createShowScheduleWithSeatsTest() {
    Long id = 1L;
    CreateScheduleRequest  request = new CreateScheduleRequest();
    List<LocalDateTime> startTimes = new ArrayList<>();
    startTimes.add(LocalDateTime.of(2025, 12, 1, 12, 0));
    request.setStartTimes(startTimes);
    showService.createShowSchedule(id, request);
  }

  @Test
  void deleteShowTest() {
    Long id = 2L;
    showService.deleteShow(id);
  }
}
