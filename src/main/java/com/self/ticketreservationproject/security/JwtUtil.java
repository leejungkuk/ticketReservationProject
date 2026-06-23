package com.self.ticketreservationproject.security;

import com.self.ticketreservationproject.service.CustomUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

  private static final String KEY_ROLES = "roles";

  @Value("${spring.jwt.secretKey}")
  private String secretKey;

  @Value("${spring.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  @Value("${spring.jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  private final CustomUserService customUserService;

  public String generateToken(String username, Set<String> roles) {
    Claims claims = Jwts.claims().setSubject(username);
    claims.put(KEY_ROLES, roles);

    Date now = new Date();
    Date expiration = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
        .setClaims(claims)
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(now)
        .setExpiration(expiration)
        .signWith(SignatureAlgorithm.HS512, this.secretKey)
        .compact();
  }

  public String generateRefreshToken(String username) {
    Claims claims = Jwts.claims().setSubject(username);

    Date now = new Date();
    Date expiration = new Date(now.getTime() + refreshTokenExpiration);

    return Jwts.builder()
        .setClaims(claims)
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(now)
        .setExpiration(expiration)
        .signWith(SignatureAlgorithm.HS512, this.secretKey)
        .compact();
  }

  public Authentication getAuthentication(String jwt) {
    UserDetails userDetails = customUserService.loadUserByUsername(getUsername(jwt));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  private Claims parseClaims(String token) {
    try{
      return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException e){
      return e.getClaims();
    }
  }

  public String getUsername(String token) {
    return this.parseClaims(token).getSubject();
  }

  public boolean validateToken(String token) {
    if(!StringUtils.hasText(token)) return false;

    try {
      Claims claims = this.parseClaims(token);
      return !claims.getExpiration().before(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 토큰이 만료되었는지 확인
   * @param token JWT 토큰
   * @return 만료되었으면 true, 유효하면 false
   */
  public boolean isTokenExpired(String token) {
    try {
      Claims claims = Jwts.parser()
          .setSigningKey(this.secretKey)
          .parseClaimsJws(token)
          .getBody();
      return claims.getExpiration().before(new Date());
    } catch (ExpiredJwtException e) {
      return true;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * 만료 여부와 관계없이 토큰의 서명이 유효한지 확인
   * @param token JWT 토큰
   * @return 서명이 유효하면 true, 유효하지 않으면 false
   */
  public boolean isTokenSignatureValid(String token) {
    try {
      Jwts.parser()
          .setSigningKey(this.secretKey)
          .parseClaimsJws(token);
      return true;
    } catch (ExpiredJwtException e) {
      // 만료되었지만 서명은 유효함
      return true;
    } catch (Exception e) {
      // 서명이 유효하지 않음
      return false;
    }
  }

}
