package com.self.ticketreservationproject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.self.ticketreservationproject.service.RefreshTokenService;
import com.self.ticketreservationproject.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Test
  void refreshWithInvalidTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"invalid-token\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logoutWithStaleRefreshTokenDoesNotDeleteCurrentToken() throws Exception {
    String username = "logout-user";
    String staleToken = jwtUtil.generateRefreshToken(username);
    String currentToken = refreshTokenService.createRefreshToken(username);

    org.assertj.core.api.Assertions.assertThat(staleToken).isNotEqualTo(currentToken);

    mockMvc.perform(post("/api/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + staleToken + "\"}"))
        .andExpect(status().isOk());

    boolean currentTokenStillValid = refreshTokenService.validateRefreshToken(username, currentToken);

    org.assertj.core.api.Assertions.assertThat(currentTokenStillValid).isTrue();
    refreshTokenService.deleteByUsername(username);
  }
}
