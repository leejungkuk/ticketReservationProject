package com.self.ticketreservationproject.config;

import java.io.File;
import java.io.IOException;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class RedissonConfig {

  @Bean
  public RedissonClient redissonClient() throws IOException {
    File configFile = new ClassPathResource("redisson.yml").getFile();
    Config config = Config.fromYAML(configFile);
    return Redisson.create(config);
  }
}
