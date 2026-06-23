package com.self.ticketreservationproject.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "refreshToken")
public class RefreshToken {

  @Id
  private String username;

  private String token;

  @TimeToLive
  private Long expiration;

  public void updateToken(String token, Long expiration) {
    this.token = token;
    this.expiration = expiration;
  }
}
