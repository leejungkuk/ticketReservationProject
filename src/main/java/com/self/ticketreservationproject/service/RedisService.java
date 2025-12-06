package com.self.ticketreservationproject.service;

import com.self.ticketreservationproject.dto.reservation.ReservationRequest.ReserveRequest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

  private final StringRedisTemplate redisTemplate;
  private final RedissonClient redissonClient;

  public boolean holdSeat(ReserveRequest request, long userId) {
    Boolean result = redisTemplate.opsForValue().setIfAbsent(
        key(request.getSeatId()),
        String.valueOf(userId),
        Duration.ofMinutes(5)
    );
    return result != null && result;
  }

  public Long getHoldUser(long seatId) {
    String v = redisTemplate.opsForValue().get(key(seatId));
    return v == null ? null : Long.valueOf(v);
  }

  public boolean isHeldByUser(long seatId, long userId) {
    Long holdUser = getHoldUser(seatId);
    return holdUser != null && holdUser == userId;
  }

  public void releaseSeat(long seatId) {
    redisTemplate.delete(key(seatId));

    // Redisson RMap 삭제
    RMap<String, Object> map = redissonClient.getMap("map:seat:" + seatId);
    map.delete();
  }

  private String key(long seatId) {
    return "seat:" + seatId;
  }
}
