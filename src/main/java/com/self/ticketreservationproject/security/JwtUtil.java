package com.self.ticketreservationproject.security;

import com.self.ticketreservationproject.service.CustomUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Set;
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
  private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 60분

  @Value("${spring.jwt.secretKey}")
  private String secretKey;

  private final CustomUserService customUserService;

  public String generateToken(String username, Set<String> roles) {
    Claims claims = Jwts.claims().setSubject(username);
    claims.put(KEY_ROLES, roles);

    Date now = new Date();
    Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

    return Jwts.builder()
        .setClaims(claims)
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

    Claims claims = this.parseClaims(token);
    return !claims.getExpiration().before(new Date());
  }


}
