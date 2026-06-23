package com.self.ticketreservationproject.security;

import com.self.ticketreservationproject.exception.custom.security.ExpiredRefreshTokenException;
import com.self.ticketreservationproject.exception.custom.security.InvalidRefreshTokenException;
import com.self.ticketreservationproject.exception.custom.security.RefreshTokenNotFoundException;
import com.self.ticketreservationproject.service.CustomUserService;
import com.self.ticketreservationproject.service.RefreshTokenService;
import com.self.ticketreservationproject.service.UserService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRefreshService {

  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;
  private final UserService userService;
  private final CustomUserService customUserService;

  /**
   * 만료된 access token과 refresh token을 사용하여 새로운 access token 발급
   * @param expiredAccessToken 만료된 access token
   * @param refreshToken refresh token
   * @return 새로운 access token
   * @throws RefreshTokenNotFoundException refresh token이 Redis에 없는 경우
   * @throws InvalidRefreshTokenException refresh token이 유효하지 않은 경우
   * @throws ExpiredRefreshTokenException refresh token이 만료된 경우
   */
  public String refreshAccessToken(String expiredAccessToken, String refreshToken) {
    // 1. 만료된 access token에서 username 추출
    String username = jwtUtil.getUsername(expiredAccessToken);
    log.info("Token refresh requested for user: {}", username);

    // 2. Refresh token 서명 검증
    if (!jwtUtil.isTokenSignatureValid(refreshToken)) {
      log.warn("Invalid refresh token signature for user: {}", username);
      throw new InvalidRefreshTokenException();
    }

    // 3. Refresh token 만료 확인
    if (jwtUtil.isTokenExpired(refreshToken)) {
      log.warn("Refresh token expired for user: {}", username);
      throw new ExpiredRefreshTokenException();
    }

    // 4. Redis에 저장된 refresh token과 비교
    boolean isValid = refreshTokenService.validateRefreshToken(username, refreshToken);
    if (!isValid) {
      log.warn("Refresh token not found or mismatch for user: {}", username);
      throw new RefreshTokenNotFoundException();
    }

    // 5. 새로운 access token 발급
    Set<String> roles = userService.getRolesByUsername(username);
    String newAccessToken = jwtUtil.generateToken(username, roles);

    log.info("Access token refreshed successfully for user: {}", username);
    return newAccessToken;
  }

  /**
   * Access token으로부터 Authentication 객체 생성
   * @param accessToken access token
   * @return Authentication 객체
   */
  public Authentication getAuthenticationFromToken(String accessToken) {
    UserDetails userDetails = customUserService.loadUserByUsername(jwtUtil.getUsername(accessToken));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }
}
