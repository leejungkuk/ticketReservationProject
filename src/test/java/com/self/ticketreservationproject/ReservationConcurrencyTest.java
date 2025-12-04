package com.self.ticketreservationproject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.self.ticketreservationproject.dto.reservation.ReservationRequest.ReserveRequest;
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

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationConcurrencyTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JwtUtil jwtUtil;

  private static final long SEAT_ID = 52L;

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
  }
}
