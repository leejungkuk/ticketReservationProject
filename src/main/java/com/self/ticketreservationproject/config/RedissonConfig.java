package com.self.ticketreservationproject.config;

import java.io.IOException;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

  @Bean
  public RedissonClient redissonClient() throws IOException {
    return Redisson.create(
        Config.fromYAML("""
            singleServerConfig:
              address: "redis://127.0.0.1:6379"
              connectionMinimumIdleSize: 10
              connectionPoolSize: 64
              idleConnectionTimeout: 10000
              connectTimeout: 100000
            """)
    );
  }
}
