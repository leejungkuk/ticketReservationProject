package com.self.ticketreservationproject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedissonLockTest {

  @Autowired
  private RedissonClient redissonClient;

  @Test
  void lockTest() throws Exception {
    RLock lock = redissonClient.getLock("seat:10");

    boolean isLocked = lock.tryLock(3, 5, TimeUnit.SECONDS);
    assertThat(isLocked).isTrue();

    lock.unlock();
  }
}
