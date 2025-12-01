package com.self.ticketreservationproject.service;

import com.self.ticketreservationproject.domain.show.ShowInfo;
import com.self.ticketreservationproject.domain.show.ShowSchedule;
import com.self.ticketreservationproject.dto.show.ShowRequest.CreateScheduleRequest;
import com.self.ticketreservationproject.dto.show.ShowRequest.UploadShowInfoRequest;
import com.self.ticketreservationproject.exception.custom.show.AlreadyExistShowException;
import com.self.ticketreservationproject.exception.custom.show.DuplicateScheduleException;
import com.self.ticketreservationproject.exception.custom.show.ShowNotFoundException;
import com.self.ticketreservationproject.repository.ShowRepository;
import com.self.ticketreservationproject.repository.ShowScheduleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShowService {

  private final ShowRepository showRepository;
  private final ShowScheduleRepository showScheduleRepository;

  // 공연 등록
  public ShowInfo createShow(UploadShowInfoRequest request) {
    if (showRepository.existsByTitle(request.getTitle())) {
      throw new AlreadyExistShowException();
    }
    ShowInfo showInfo = request.toEntity();

    return showRepository.save(showInfo);
  }

  // 공연 조회
  public List<ShowInfo> findShows(String title) {
    if (!showRepository.existsByTitle(title)) {
      return showRepository.findAll();
    }
    return showRepository.findShowInfoByTitle(title);
  }

  // 특정 공연 스케줄 포함 조회
  public ShowInfo findShowWithSchedule(Long id) {
    return showRepository.findById(id).orElseThrow(ShowNotFoundException::new);
  }

  // 공연 스케줄 등록
  @Transactional
  public ShowInfo createShowSchedule(Long id, CreateScheduleRequest request) {
    ShowInfo show = showRepository.findById(id).orElseThrow(ShowNotFoundException::new);

    for (LocalDateTime startTime : request.getStartTimes()) {
      boolean existsInDb = showScheduleRepository.existsByShowInfoIdAndStartTime(id, startTime);

      if (existsInDb) {
        throw new DuplicateScheduleException();
      }
      ShowSchedule showSchedule = ShowSchedule.builder()
          .startTime(startTime)
          .build();
      List<String> seats = createSeat();
      showSchedule.generateSeats(seats, 50000);
      show.addSchedule(showSchedule);
    }

    return show;
  }

  private List<String> createSeat() {
    List<String> seatNumbers = new ArrayList<>();

    int rowCount = 5;
    int seatCount = 10;

    for (int r = 0; r < rowCount; r++) {
      char row = (char) ('A' + r);
      for (int c = 1; c <= seatCount; c++) {
        seatNumbers.add(row + String.valueOf(c));
      }
    }
    return seatNumbers;
  }

  public void deleteShow(Long id) {
    ShowInfo show = showRepository.findById(id)
        .orElseThrow(ShowNotFoundException::new);

    showRepository.delete(show);
  }


}
