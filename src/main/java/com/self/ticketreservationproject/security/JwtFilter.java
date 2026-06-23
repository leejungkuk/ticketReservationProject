package com.self.ticketreservationproject.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final TokenRefreshService tokenRefreshService;

  private static final String TOKEN_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";
  private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";
  private static final String NEW_ACCESS_TOKEN_HEADER = "X-New-Access-Token";

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = resolveToken(request);

    if (StringUtils.hasText(token)) {
      // 1. Access token이 유효한 경우 - 기존 로직
      if (jwtUtil.validateToken(token)) {
        Authentication auth = jwtUtil.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info(String.format("[%s] : %s", jwtUtil.getUsername(token), request.getRequestURI()));
      }
      // 2. Access token이 만료되었지만 서명은 유효한 경우 - 자동 갱신 시도
      else if (jwtUtil.isTokenExpired(token) && jwtUtil.isTokenSignatureValid(token)) {
        String refreshToken = resolveRefreshToken(request);

        if (StringUtils.hasText(refreshToken)) {
          try {
            // 새 access token 발급
            String newAccessToken = tokenRefreshService.refreshAccessToken(token, refreshToken);

            // 응답 헤더에 새 토큰 추가
            response.setHeader(NEW_ACCESS_TOKEN_HEADER, newAccessToken);

            // SecurityContext에 인증 정보 설정
            Authentication auth = tokenRefreshService.getAuthenticationFromToken(newAccessToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.info("Token refreshed for user: {} on request: {}",
                jwtUtil.getUsername(token), request.getRequestURI());
          } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            // 갱신 실패 시 인증 없이 진행 (Spring Security가 401 처리)
          }
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String token = request.getHeader(TOKEN_HEADER);

    if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
      return token.substring(TOKEN_PREFIX.length());
    }

    return token;
  }

  private String resolveRefreshToken(HttpServletRequest request) {
    return request.getHeader(REFRESH_TOKEN_HEADER);
  }
}
