package com.self.ticketreservationproject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class RedisConnectionTest {

  @Autowired
  private StringRedisTemplate redis;

  @Test
  void redisWorks() {
    redis.opsForValue().set("key", "value");
    assertThat(redis.opsForValue().get("key")).isEqualTo("value");
  }
}
