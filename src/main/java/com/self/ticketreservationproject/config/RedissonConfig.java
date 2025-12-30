package com.self.ticketreservationproject.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RedissonConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  @Value("${spring.data.redis.password}")
  private String redisPassword;

  @Bean
  @Profile("prod")
  public RedissonClient redissonClient() {
    Config config = new Config();
    String address = "redis://" + redisHost + ":" + redisPort;

    config.useSingleServer().setAddress(address)
    .setPassword(redisPassword);

    return Redisson.create(config);
  }

  @Bean
  public RedissonClient redissonClientDev() {
    Config config = new Config();
    String address = "redis://127.0.0.1:" + redisPort;

    config.useSingleServer().setAddress(address)
        .setPassword(redisPassword);

    return Redisson.create(config);
  }
}
