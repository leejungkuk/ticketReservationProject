package com.self.ticketreservationproject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.self.ticketreservationproject.dto.reservation.ReservationRequest.ConfirmRequest;
import com.self.ticketreservationproject.dto.reservation.ReservationRequest.ReserveRequest;
import com.self.ticketreservationproject.repository.show.ShowSeatRepository;
import com.self.ticketreservationproject.security.JwtUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReservationConcurrencyTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private ShowSeatRepository showSeatRepository;

  private static final long SEAT_ID = 51L;

  private static final String USER1 = "testid1";
  private static final String USER2 = "testid2";
  private static final String USER3 = "testid3";

  private String generateJwt(String username) {
    Set<String> roles = Set.of("ROLE_USER");
    return jwtUtil.generateToken(username, roles);
  }

  @Test
  void concurrencyTest() throws Exception {
    String token1 = generateJwt(USER1);
    String token2 = generateJwt(USER2);
    String token3 = generateJwt(USER3);

    List<String> tokens = List.of(token1, token2, token3);

    int threadCount = 3;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    List<Future<Integer>> results = new ArrayList<>();

    for (String token : tokens) {
      Future<Integer> future = executor.submit(() -> {
        try {
          String username = jwtUtil.getUsername(token);
          ReserveRequest req =
              new ReserveRequest();
          req.setUsername(username);
          req.setSeatId(SEAT_ID);

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
          latch.countDown();
        }
      });

      results.add(future);
    }

    latch.await();

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
    CountDownLatch confirmLatch = new CountDownLatch(confirmThreadCount);

    List<Future<Integer>> confirmResults = new ArrayList<>();

    for (String token : tokens) {
      Future<Integer> future = confirmExecutor.submit(() -> {
        try {
          String username = jwtUtil.getUsername(token);
          ConfirmRequest req =
              new ConfirmRequest();
          req.setUsername(username);
          req.setSeatIds(List.of(SEAT_ID));

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
          confirmLatch.countDown();
        }
      });

      confirmResults.add(future);
    }

    confirmLatch.await();

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
    long seatId = 51L;

    String token1 = generateJwt(USER1);
    String token2 = generateJwt(USER2);
    String token3 = generateJwt(USER3);

    List<String> tokens = List.of(token1, token2, token3);

    int threadCount = 3;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    List<Future<Integer>> results = new ArrayList<>();

    for (String token : tokens) {
      Future<Integer> future = executor.submit(() -> {
        try {
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
          latch.countDown();
        }
      });

      results.add(future);
    }

    latch.await();

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
    CountDownLatch confirmLatch = new CountDownLatch(3);
    List<Future<Integer>> confirmResults = new ArrayList<>();

    for (String token : tokens) {

      Future<Integer> future = confirmExecutor.submit(() -> {
        try {
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
          confirmLatch.countDown();
        }
      });

      confirmResults.add(future);
    }


    confirmLatch.await();

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
}
