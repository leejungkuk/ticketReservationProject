package com.self.ticketreservationproject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.self.ticketreservationproject.domain.show.ShowInfo;
import com.self.ticketreservationproject.dto.reservation.ReservationRequest.ConfirmRequest;
import com.self.ticketreservationproject.dto.reservation.ReservationRequest.ReserveRequest;
import com.self.ticketreservationproject.dto.show.ShowRequest.CreateScheduleRequest;
import com.self.ticketreservationproject.dto.show.ShowRequest.UploadShowInfoRequest;
import com.self.ticketreservationproject.dto.user.UserRequest;
import com.self.ticketreservationproject.repository.show.ShowSeatRepository;
import com.self.ticketreservationproject.security.JwtUtil;
import com.self.ticketreservationproject.service.ShowService;
import com.self.ticketreservationproject.service.UserService;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationConcurrencyTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private ShowSeatRepository showSeatRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private ShowService showService;

  @Autowired
  private EntityManager entityManager;

  private String generateJwt(String username) {
    Set<String> roles = Set.of("ROLE_USER");
    return jwtUtil.generateToken(username, roles);
  }

  @Test
  void concurrencyTest() throws Exception {
    TestData testData = createTestData("db");
    Long seatId = testData.seatId();
    List<String> tokens = testData.tokens();

    int threadCount = 3;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch readyLatch = new CountDownLatch(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    List<Future<Integer>> results = new ArrayList<>();

    for (String token : tokens) {
      Future<Integer> future = executor.submit(() -> {
        try {
          readyLatch.countDown();
          startLatch.await();
          String username = jwtUtil.getUsername(token);
          ReserveRequest req =
              new ReserveRequest();
          req.setUsername(username);
          req.setSeatId(seatId);

          String json = objectMapper.writeValueAsString(req);

          MvcResult result = mockMvc.perform(
                  post("/api/tickets/db/reserve")
                      .header("Authorization", "Bearer " + token)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json)
              )
              .andReturn();

          return result.getResponse().getStatus();
        } finally {
          doneLatch.countDown();
        }
      });

      results.add(future);
    }

    readyLatch.await();
    startLatch.countDown();
    doneLatch.await();
    executor.shutdownNow();

    int success = 0;
    int fail = 0;

    for (Future<Integer> r : results) {
      int status = r.get();
      if (status == 200) {
        success++;
      } else {
        fail++;
      }
    }

    System.out.println("SUCCESS = " + success);
    System.out.println("FAIL = " + fail);

    Assertions.assertEquals(1, success);
    Assertions.assertEquals(2, fail);

    int confirmThreadCount = 3;
    ExecutorService confirmExecutor = Executors.newFixedThreadPool(confirmThreadCount);
    CountDownLatch confirmReadyLatch = new CountDownLatch(confirmThreadCount);
    CountDownLatch confirmStartLatch = new CountDownLatch(1);
    CountDownLatch confirmDoneLatch = new CountDownLatch(confirmThreadCount);

    List<Future<Integer>> confirmResults = new ArrayList<>();

    for (String token : tokens) {
      Future<Integer> future = confirmExecutor.submit(() -> {
        try {
          confirmReadyLatch.countDown();
          confirmStartLatch.await();
          String username = jwtUtil.getUsername(token);
          ConfirmRequest req =
              new ConfirmRequest();
          req.setUsername(username);
          req.setSeatIds(List.of(seatId));

          String json = objectMapper.writeValueAsString(req);

          MvcResult result = mockMvc.perform(
                  post("/api/tickets/db/confirm")
                      .header("Authorization", "Bearer " + token)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json)
              )
              .andReturn();

          return result.getResponse().getStatus();
        } finally {
          confirmDoneLatch.countDown();
        }
      });

      confirmResults.add(future);
    }

    confirmReadyLatch.await();
    confirmStartLatch.countDown();
    confirmDoneLatch.await();
    confirmExecutor.shutdownNow();

    int confirmSuccess = 0;
    int confirmFail = 0;

    for (Future<Integer> r : confirmResults) {
      int status = r.get();
      if (status == 200) {
        confirmSuccess++;
      } else {
        confirmFail++;
      }
    }

    System.out.println("SUCCESS = " + confirmSuccess);
    System.out.println("FAIL = " + confirmFail);

    Assertions.assertEquals(1, confirmSuccess);
    Assertions.assertEquals(2, confirmFail);
  }

  // redis 테스트
  @Test
  void concurrencyTestWithRedis() throws Exception {
    TestData testData = createTestData("redis");
    long seatId = testData.seatId();
    List<String> tokens = testData.tokens();

    int threadCount = 3;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch readyLatch = new CountDownLatch(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    List<Future<Integer>> results = new ArrayList<>();

    for (String token : tokens) {
      Future<Integer> future = executor.submit(() -> {
        try {
          readyLatch.countDown();
          startLatch.await();
          String username = jwtUtil.getUsername(token);

          ReserveRequest req = new ReserveRequest();
          req.setUsername(username);
          req.setSeatId(seatId);

          String json = objectMapper.writeValueAsString(req);

          MvcResult result = mockMvc.perform(
                  post("/api/tickets/redis/reserve")
                      .header("Authorization", "Bearer " + token)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json)
              )
              .andReturn();

          return result.getResponse().getStatus();
        } finally {
          doneLatch.countDown();
        }
      });

      results.add(future);
    }

    readyLatch.await();
    startLatch.countDown();
    doneLatch.await();
    executor.shutdownNow();

    int reserveSuccess = 0;
    int reserveFail = 0;

    for (Future<Integer> r : results) {
      if (r.get() == 200) reserveSuccess++;
      else reserveFail++;
    }

    System.out.println("REDIS SUCCESS = " + reserveSuccess);
    System.out.println("REDIS FAIL = " + reserveFail);

    Assertions.assertEquals(1, reserveSuccess); // 성공 1명
    Assertions.assertEquals(2, reserveFail);

//    // confirm test
    ExecutorService confirmExecutor = Executors.newFixedThreadPool(3);
    CountDownLatch confirmReadyLatch = new CountDownLatch(3);
    CountDownLatch confirmStartLatch = new CountDownLatch(1);
    CountDownLatch confirmDoneLatch = new CountDownLatch(3);
    List<Future<Integer>> confirmResults = new ArrayList<>();

    for (String token : tokens) {

      Future<Integer> future = confirmExecutor.submit(() -> {
        try {
          confirmReadyLatch.countDown();
          confirmStartLatch.await();
          String username = jwtUtil.getUsername(token);
          // 테스트용
          long scheduleId = showSeatRepository.findScheduleIdById(seatId);

          ConfirmRequest req = new ConfirmRequest();
          req.setUsername(username);
          req.setSeatIds(List.of(seatId));
          req.setScheduleId(scheduleId);


          String json = objectMapper.writeValueAsString(req);

          MvcResult result = mockMvc.perform(
              post("/api/tickets/redis/confirm")
                  .header("Authorization", "Bearer " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(json)
          ).andReturn();

          return result.getResponse().getStatus();

        } finally {
          confirmDoneLatch.countDown();
        }
      });

      confirmResults.add(future);
    }


    confirmReadyLatch.await();
    confirmStartLatch.countDown();
    confirmDoneLatch.await();
    confirmExecutor.shutdownNow();

    int confirmSuccess = 0;
    int confirmFail = 0;

    for (Future<Integer> f : confirmResults) {
      if (f.get() == 200) confirmSuccess++;
      else confirmFail++;
    }

    System.out.println("CONFIRM SUCCESS = " + confirmSuccess);
    System.out.println("CONFIRM FAIL = " + confirmFail);

    // 최종 검증
    Assertions.assertEquals(1, confirmSuccess);
    Assertions.assertEquals(2, confirmFail);
  }

  private TestData createTestData(String prefix) {
    List<String> usernames = new ArrayList<>();
    List<String> tokens = new ArrayList<>();

    for (int i = 0; i < 3; i++) {
      String username = unique(prefix + "user" + i);
      userService.createUser(registerRequest(username));
      usernames.add(username);
      tokens.add(generateJwt(username));
    }

    ShowInfo show = showService.createShow(showRequest(unique(prefix + "show")));
    CreateScheduleRequest scheduleRequest = new CreateScheduleRequest();
    scheduleRequest.setStartTimes(List.of(LocalDateTime.now().plusDays(30).withNano(0)));
    showService.createShowSchedule(show.getId(), scheduleRequest);
    Long seatId = entityManager.createQuery("""
            select s.id
            from ShowSeat s
            where s.showSchedule.showInfo.id = :showId
            order by s.id
            """, Long.class)
        .setParameter("showId", show.getId())
        .setMaxResults(1)
        .getSingleResult();

    return new TestData(usernames, tokens, seatId);
  }

  private UserRequest.RegisterRequest registerRequest(String username) {
    UserRequest.RegisterRequest request = new UserRequest.RegisterRequest();
    request.setUsername(username);
    request.setPassword("password1234");
    request.setEmail(username + "@email.com");
    request.setName(username);
    return request;
  }

  private UploadShowInfoRequest showRequest(String title) {
    UploadShowInfoRequest request = new UploadShowInfoRequest();
    request.setTitle(title);
    request.setDescription("description");
    request.setRuntime(100);
    return request;
  }

  private String unique(String prefix) {
    return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }

  private record TestData(List<String> usernames, List<String> tokens, Long seatId) {
  }
}
