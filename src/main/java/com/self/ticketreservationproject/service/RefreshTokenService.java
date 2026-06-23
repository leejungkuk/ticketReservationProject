package com.self.ticketreservationproject.service;

import com.self.ticketreservationproject.repository.security.RefreshTokenRepository;
import com.self.ticketreservationproject.security.JwtUtil;
import com.self.ticketreservationproject.security.RefreshToken;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtUtil jwtUtil;

  @Value("${spring.jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  @Transactional
  public String createRefreshToken(String username) {
    String token = jwtUtil.generateRefreshToken(username);

    RefreshToken refreshToken = RefreshToken.builder()
        .username(username)
        .token(token)
        .expiration(refreshTokenExpiration / 1000) // Redis TTL은 초 단위
        .build();

    refreshTokenRepository.save(refreshToken);
    log.info("Refresh token created for user: {}", username);

    return token;
  }

  public Optional<RefreshToken> findByUsername(String username) {
    return refreshTokenRepository.findById(username);
  }

  public boolean validateRefreshToken(String username, String token) {
    return findByUsername(username)
        .map(RefreshToken::getToken)
        .map(savedToken -> savedToken.equals(token) && jwtUtil.validateToken(token))
        .orElse(false);
  }

  @Transactional
  public void deleteByUsername(String username) {
    refreshTokenRepository.deleteById(username);
    log.info("Refresh token deleted for user: {}", username);
  }
}
