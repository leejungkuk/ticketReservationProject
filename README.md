# 🎫 Real-Time Ticket Reservation System

<div align="center">

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?style=flat-square&logo=spring-boot)
![MySQL](https://img.shields.io/badge/MySQL-8.0.37-4479A1?style=flat-square&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.4.2-DC382D?style=flat-square&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=json-web-tokens)

**대용량 동시성 처리를 위한 실시간 티켓 예매 시스템**

[Features](#-핵심-기능) • [Architecture](#-아키텍처) • [Tech Stack](#️-기술-스택) • [Getting Started](#-getting-started) • [API Docs](#-api-문서)

</div>

---

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [개발 동기](#-개발-동기)
- [핵심 기능](#-핵심-기능)
- [기술 스택](#️-기술-스택)
- [아키텍처](#-아키텍처)
- [핵심 구현](#-핵심-구현)
- [Getting Started](#-getting-started)
- [API 문서](#-api-문서)
- [데이터베이스 설계](#-데이터베이스-설계)
- [테스트](#-테스트)
- [트러블슈팅](#-트러블슈팅)

---

## 🎯 프로젝트 소개

**실시간 티켓 예매 시스템**은 좌석 중복 예매 문제를 중심으로 동시성 제어 전략을 구현하고 검증한 티켓 예매 백엔드 프로젝트입니다.

### 🚀 프로젝트 하이라이트

```
💡 핵심 가치
┌─────────────────────────────────────────────────────────────┐
│  ✅ 동시 요청 환경에서 좌석 중복 예매 방지                           │
│  ✅ Redis 분산 락을 통한 다중 인스턴스 환경 대비                      │
│  ✅ JWT 자동 갱신으로 끊김없는 사용자 경험                           │
│  ✅ 2단계 예약 프로세스 (HOLD → CONFIRM)                         │
│  ✅ Docker Compose 기반 실행 환경                                 │
└─────────────────────────────────────────────────────────────┘
```

### 📊 주요 성능 지표

- ⚡ **동시 요청 처리**: Redisson 분산 락을 통한 무결성 보장
- 🔐 **보안**: JWT 기반 무상태 인증 + Refresh Token 자동 갱신
- 🚀 **확장성**: Redis를 활용한 분산 락 전략 검증
- 📦 **실행 환경**: Docker Compose 기반 MySQL/Redis/App 구성

---

## 💭 개발 동기

### 해결하고자 한 문제

현대의 티켓 예매 시스템은 다음과 같은 도전 과제를 가지고 있습니다:

#### 1️⃣ **동시성 문제 (Race Condition)**
```
문제 상황:
👤 사용자 A: 좌석 A-1 선택 (동시에)
👤 사용자 B: 좌석 A-1 선택 (동시에)
❌ 결과: 같은 좌석이 두 사람에게 배정될 수 있음

해결 방법:
✅ Redisson 분산 락으로 원자성 보장
✅ DB 조건부 update와 Redis 분산 락 두 가지 전략 구현 및 비교
```

#### 2️⃣ **인증 토큰 만료로 인한 UX 저하**
```
문제 상황:
👤 사용자가 티켓 선택 중 Access Token 만료 (1시간)
❌ 결과: 401 Unauthorized → 다시 로그인 필요

해결 방법:
✅ Refresh Token을 활용한 자동 토큰 갱신
✅ JwtFilter에서 투명하게 처리 (사용자 인지 불필요)
✅ 응답 헤더에 X-New-Access-Token 반환
```

#### 3️⃣ **확장 가능한 아키텍처 필요**
```
문제 상황:
📈 트래픽 증가 시 단일 서버로는 한계
❌ 세션 기반 인증은 수평 확장 어려움

해결 방법:
✅ JWT 무상태 인증
✅ Redis를 통한 분산 상태 관리
✅ Docker Compose 기반 컨테이너 오케스트레이션
```

---

## ⭐ 핵심 기능

### 1. 🔐 사용자 인증 및 권한 관리

#### JWT 기반 무상태 인증
```java
// 로그인 시 두 종류의 토큰 발급
Access Token  (1시간)  → API 호출 시 사용
Refresh Token (7일)    → Access Token 갱신용 (Redis 저장)
```

#### 자동 토큰 갱신 메커니즘
```http
# 클라이언트 요청
GET /api/shows
Authorization: Bearer {expiredAccessToken}
X-Refresh-Token: {refreshToken}

# 서버 응답 (자동 갱신)
HTTP/1.1 200 OK
X-New-Access-Token: {newAccessToken}
{
  "data": [...공연 목록...]
}
```

**구현 하이라이트**: `JwtFilter.java:45-66`
```java
// Access token이 만료되었지만 서명은 유효한 경우
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
        }
    }
}
```

#### Role 기반 권한 관리
- **ROLE_USER**: 공연 조회, 예매, 내 예매 조회
- **ROLE_ADMIN**: 공연 등록/삭제, 일정 등록, 사용자 관리

### 2. 🎭 공연 및 좌석 관리

#### 공연 등록 시 자동 좌석 생성
```java
// ShowSchedule.java - 일정 생성 시 50개 좌석 자동 생성
public List<ShowSeat> generateSeats() {
    List<ShowSeat> seats = new ArrayList<>();
    for (char row = 'A'; row <= 'E'; row++) {
        for (int number = 1; number <= 10; number++) {
            seats.add(ShowSeat.builder()
                .showSchedule(this)
                .seatNumber(row + String.valueOf(number))  // A1 ~ E10
                .price(50000L)
                .status(SeatStatus.AVAILABLE)
                .build());
        }
    }
    return seats;
}
```

**좌석 배치**: 5행 × 10열 = 50석
```
A1  A2  A3  A4  A5  A6  A7  A8  A9  A10
B1  B2  B3  B4  B5  B6  B7  B8  B9  B10
C1  C2  C3  C4  C5  C6  C7  C8  C9  C10
D1  D2  D3  D4  D5  D6  D7  D8  D9  D10
E1  E2  E3  E4  E5  E6  E7  E8  E9  E10
```

### 3. 🎫 실시간 좌석 예매 (2가지 전략)

#### 📌 Strategy A: 데이터베이스 조건부 update

**엔드포인트**:
- `POST /api/tickets/db/reserve` - 좌석 HOLD
- `POST /api/tickets/db/confirm` - 예매 확정

**구현**: `ShowSeatRepositoryCustomImpl.java`
```java
return queryFactory
    .update(seat)
    .set(seat.status, SeatStatus.HOLD)
    .set(seat.holdUserId, userId)
    .set(seat.holdStartTime, now)
    .where(seat.id.eq(seatId).and(seat.status.eq(SeatStatus.AVAILABLE)))
    .execute();
```

**특징**:
- ✅ 구현이 단순함
- ✅ 트랜잭션 관리 용이
- ✅ `AVAILABLE` 상태인 좌석만 갱신해 중복 HOLD 방지
- ❌ 다중 인스턴스 환경의 임시 점유 TTL 관리는 Redis 전략보다 제한적

#### 📌 Strategy B: Redis 분산 락 (권장)

**엔드포인트**:
- `POST /api/tickets/redis/reserve` - 좌석 HOLD (분산 락)
- `POST /api/tickets/redis/confirm` - 예매 확정 (Redis 검증)

**구현**: `ReservationService.java`
```java
public ReservationResponse.Reserve reserveSeat(ReservationRequest.Reserve request, Long userId) {
    Long seatId = request.getSeatId();
    String lockKey = "lock:seat:" + seatId;

    // Redisson 분산 락 획득 (최대 3초 대기)
    RLock lock = redissonClient.getLock(lockKey);

    try {
        boolean isLocked = lock.tryLock(3, TimeUnit.SECONDS);
        if (!isLocked) {
            throw new SeatAlreadyTakenException();
        }

        // 좌석 상태 확인
        ShowSeat seat = showSeatRepository.findById(seatId)
            .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new SeatAlreadyTakenException();
        }

        // Redis에 좌석 점유 정보 저장 (5분 TTL)
        redisService.holdSeat(request, userId);

        // 데이터베이스 상태 업데이트
        seat.hold(userId);
        showSeatRepository.save(seat);

        return ReservationResponse.Reserve.from(seat);
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

**Redis Key 구조**:
```
seat:{seatId} → {userId}    (TTL: 5분)
lock:seat:{seatId}          (Redisson 분산 락)
```

**특징**:
- ✅ 다중 인스턴스 환경에서 안전
- ✅ 자동 TTL로 데드락 방지
- ✅ 데이터베이스 부하 감소
- ✅ 수평 확장 가능

### 4. 📦 2단계 예약 프로세스

```
┌──────────┐       ┌──────────┐       ┌──────────┐
│ AVAILABLE│ ────▶ │   HOLD   │ ────▶ │ RESERVED │
│  (대기중) │       │  (임시)   │       │  (확정)   │
└──────────┘       └──────────┘       └──────────┘
                   (5분 제한)          (Booking 생성)
```

**1단계: HOLD (임시 점유)**
- 사용자가 좌석 선택 시 HOLD 상태로 변경
- Redis에 `userId`와 함께 저장 (5분 TTL)
- 다른 사용자는 선택 불가

**2단계: CONFIRM (확정)**
- 사용자가 결제 완료 후 CONFIRM
- Redis에 저장된 `userId` 확인 (본인 검증)
- 좌석 상태 RESERVED 변경
- `Booking` 엔티티 생성

**자동 만료**:
- Redis TTL (5분) 만료 시 자동 해제
- 다른 사용자가 다시 선택 가능

---

## 🛠️ 기술 스택

### Backend Framework
- **Java 17** - 최신 LTS 버전
- **Spring Boot 3.5.7** - 엔터프라이즈 프레임워크
- **Spring Security** - 인증/인가
- **Spring Data JPA** - ORM
- **QueryDSL 5.0.0** - 타입 안전 쿼리

### Database & Cache
- **MySQL 8.0.37** - 메인 데이터베이스
- **Redis 7.4.2** - 캐시 & 분산 락 & Refresh Token 저장
- **Redisson 3.33.0** - Redis 분산 락 클라이언트

### Security
- **JWT (jjwt 0.11.5)** - 토큰 기반 인증
- **BCrypt** - 비밀번호 암호화
- **HS512** - JWT 서명 알고리즘

### DevOps & Tools
- **Docker & Docker Compose** - 컨테이너 오케스트레이션
- **Swagger/OpenAPI 3** - API 문서 자동 생성
- **Lombok** - 보일러플레이트 코드 제거
- **Gradle 8.x** - 빌드 도구

### Testing
- **JUnit 5** - 단위 테스트
- **MockMvc** - 통합 테스트
- **ExecutorService** - 동시성 테스트

---

## 🏗️ 아키텍처

### 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│  (Web/Mobile) - JWT Access Token + Refresh Token in Headers     │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Controller  │  │   Service    │  │  Repository  │          │
│  │    Layer     │─▶│    Layer     │─▶│    Layer     │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                  │                  │                  │
│         │                  ▼                  ▼                  │
│  ┌──────▼────────┐  ┌─────────────────────────────┐            │
│  │  JwtFilter    │  │  TokenRefreshService        │            │
│  │  (자동 갱신)   │  │  (토큰 검증 & 발급)          │            │
│  └───────────────┘  └─────────────────────────────┘            │
└────────────────────────────┬────────────────┬───────────────────┘
                             │                │
                ┌────────────▼────┐    ┌─────▼──────┐
                │   MySQL 8.0     │    │ Redis 7.4  │
                │                 │    │            │
                │ • User          │    │ • Locks    │
                │ • Show          │    │ • Tokens   │
                │ • Reservation   │    │ • Seat Hold│
                └─────────────────┘    └────────────┘
```

### 패키지 구조

```
ticketReservationProject/
├── 📦 config/                   # 설정
│   ├── RedissonConfig.java      # Redisson 분산 락 설정
│   ├── QuerydslConfig.java      # QueryDSL 설정
│   └── SwaggerConfig.java       # Swagger 설정
│
├── 📦 security/                 # 보안
│   ├── JwtUtil.java             # JWT 생성/검증
│   ├── JwtFilter.java           # JWT 필터 (자동 갱신 포함)
│   ├── TokenRefreshService.java # 토큰 갱신 서비스
│   ├── RefreshToken.java        # Refresh Token 엔티티 (Redis)
│   ├── CustomUserDetails.java  # UserDetails 구현체
│   └── SecurityConfig.java      # Spring Security 설정
│
├── 📦 domain/                   # 도메인 엔티티
│   ├── user/                    # 사용자 도메인
│   │   ├── User.java
│   │   ├── Role.java
│   │   └── UserRole.java
│   ├── show/                    # 공연 도메인
│   │   ├── ShowInfo.java
│   │   ├── ShowSchedule.java
│   │   ├── ShowSeat.java
│   │   └── SeatStatus.java
│   └── reservation/             # 예약 도메인
│       ├── Booking.java
│       ├── ReservationSeat.java
│       └── BookingStatus.java
│
├── 📦 repository/               # 데이터 접근
│   ├── user/
│   ├── show/
│   │   ├── ShowSeatRepository.java
│   │   └── ShowSeatRepositoryCustomImpl.java  # QueryDSL 구현
│   ├── reservation/
│   └── security/
│       └── RefreshTokenRepository.java  # Redis Repository
│
├── 📦 service/                  # 비즈니스 로직
│   ├── UserService.java
│   ├── CustomUserService.java   # UserDetailsService 구현
│   ├── ShowService.java
│   ├── ReservationService.java  # 예약 로직 (Redis 분산 락)
│   ├── RedisService.java        # Redis 캐시 서비스
│   └── RefreshTokenService.java # Refresh Token 관리
│
├── 📦 controller/               # API 엔드포인트
│   ├── UserController.java      # 인증/회원
│   ├── ShowController.java      # 공연 관리
│   └── ReservationController.java # 예약 관리
│
├── 📦 dto/                      # 데이터 전송 객체
│   ├── user/
│   ├── show/
│   └── reservation/
│
└── 📦 exception/                # 예외 처리
    ├── ErrorCode.java           # 에러 코드 정의
    ├── AbstractException.java   # 커스텀 예외 추상 클래스
    ├── CustomExceptionHandler.java # 전역 예외 핸들러
    └── custom/
        ├── user/
        ├── show/
        ├── reservation/
        └── security/            # 토큰 관련 예외
```

---

## 💡 핵심 구현

### 1. 🔒 동시성 제어 - Redis 분산 락

**문제**: 여러 사용자가 동시에 같은 좌석 예약 시도

**해결**: Redisson RLock을 활용한 분산 락

```java
// ReservationService.java
RLock lock = redissonClient.getLock("lock:seat:" + seatId);

try {
    // 최대 3초 대기, 락 획득 실패 시 예외
    boolean isLocked = lock.tryLock(3, TimeUnit.SECONDS);
    if (!isLocked) {
        throw new SeatAlreadyTakenException();
    }

    // 임계 영역: 좌석 상태 확인 및 변경
    // ... 비즈니스 로직 ...

} finally {
    // 현재 스레드가 락을 보유한 경우에만 해제
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

**동시성 테스트 결과** (`ReservationConcurrencyTest.java`):
```java
// 3명의 사용자가 동시에 1개 좌석 예약 시도
ExecutorService executor = Executors.newFixedThreadPool(3);
List<Future<ResultActions>> futures = new ArrayList<>();

for (int i = 0; i < 3; i++) {
    futures.add(executor.submit(() -> {
        return mockMvc.perform(post("/api/tickets/redis/reserve")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));
    }));
}

// 결과: 1명 성공 (200), 2명 실패 (400 - 이미 선택된 좌석)
```

### 2. 🔐 JWT 자동 갱신 메커니즘

**문제**: Access Token 만료 시 사용자가 재로그인 필요

**해결**: JwtFilter에서 투명하게 토큰 갱신

**플로우**:
```
┌────────────────────────────────────────────────────────────────┐
│ 1. 클라이언트 요청                                               │
│    Authorization: Bearer {expiredAccessToken}                  │
│    X-Refresh-Token: {refreshToken}                             │
└────────────────────┬───────────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────────┐
│ 2. JwtFilter 검증                                               │
│    • validateToken(token) → false (만료됨)                     │
│    • isTokenExpired(token) → true                              │
│    • isTokenSignatureValid(token) → true (서명은 유효)         │
└────────────────────┬───────────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────────┐
│ 3. TokenRefreshService 호출                                     │
│    • Refresh token 서명 검증                                    │
│    • Refresh token 만료 확인                                    │
│    • Redis에 저장된 token과 비교                                │
│    • 새 Access Token 발급 (같은 roles 유지)                     │
└────────────────────┬───────────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────────┐
│ 4. 응답                                                         │
│    HTTP/1.1 200 OK                                             │
│    X-New-Access-Token: {newAccessToken}                        │
│    {원래 요청한 데이터}                                          │
└────────────────────────────────────────────────────────────────┘
```

**코드 하이라이트**:

**JwtUtil.java** - 토큰 상태 검증
```java
/**
 * 만료 여부와 관계없이 토큰의 서명이 유효한지 확인
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
```

**TokenRefreshService.java** - 토큰 갱신 로직
```java
public String refreshAccessToken(String expiredAccessToken, String refreshToken) {
    // 1. 만료된 access token에서 username 추출
    String username = jwtUtil.getUsername(expiredAccessToken);

    // 2. Refresh token 서명 검증
    if (!jwtUtil.isTokenSignatureValid(refreshToken)) {
        throw new InvalidRefreshTokenException();
    }

    // 3. Refresh token 만료 확인
    if (jwtUtil.isTokenExpired(refreshToken)) {
        throw new ExpiredRefreshTokenException();
    }

    // 4. Redis에 저장된 refresh token과 비교
    boolean isValid = refreshTokenService.validateRefreshToken(username, refreshToken);
    if (!isValid) {
        throw new RefreshTokenNotFoundException();
    }

    // 5. 새로운 access token 발급
    Set<String> roles = userService.getRolesByUsername(username);
    String newAccessToken = jwtUtil.generateToken(username, roles);

    log.info("Access token refreshed successfully for user: {}", username);
    return newAccessToken;
}
```

### 3. 🗄️ Redis를 활용한 분산 상태 관리

**Redis 사용 목적**:

#### A. Refresh Token 저장
```java
// RefreshToken.java - Redis Hash 엔티티
@RedisHash("refreshToken")
@Data
@Builder
public class RefreshToken {
    @Id
    private String username;  // Key: refreshToken:{username}

    private String token;

    @TimeToLive
    private Long expiration;  // 7일 (604,800초) - 자동 삭제
}
```

#### B. 좌석 임시 점유 정보
```java
// RedisService.java
public void holdSeat(ReservationRequest.Reserve request, Long userId) {
    String key = "seat:" + request.getSeatId();
    String value = String.valueOf(userId);

    // 5분 TTL로 저장
    redisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
}

public boolean verifySeatHolder(Long seatId, Long userId) {
    String key = "seat:" + seatId;
    String storedUserId = redisTemplate.opsForValue().get(key);

    // Redis에 저장된 userId와 현재 userId 비교
    return storedUserId != null && storedUserId.equals(String.valueOf(userId));
}
```

#### C. 분산 락 (Redisson)
```java
// RedissonConfig.java
@Configuration
@Profile("prod")
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://" + redisHost + ":" + redisPort)
            .setPassword(redisPassword)
            .setConnectionPoolSize(10)
            .setConnectionMinimumIdleSize(2);

        return Redisson.create(config);
    }
}
```

### 4. 🎯 2단계 예약 프로세스 구현

**ShowSeat 엔티티 상태 전이**:
```java
// ShowSeat.java
public void hold(Long userId) {
    if (this.status != SeatStatus.AVAILABLE) {
        throw new SeatAlreadyTakenException();
    }
    this.status = SeatStatus.HOLD;
    this.holdUserId = userId;
    this.holdStartTime = LocalDateTime.now();
}

public void reserve() {
    if (this.status != SeatStatus.HOLD) {
        throw new ConfirmFailedException();
    }
    this.status = SeatStatus.RESERVED;
}

public void release() {
    this.status = SeatStatus.AVAILABLE;
    this.holdUserId = null;
    this.holdStartTime = null;
}
```

**Confirm 검증 로직**:
```java
// ReservationService.java - confirmReservation()
public ReservationResponse.Confirm confirmReservation(
        ReservationRequest.Confirm request, Long userId) {

    Long seatId = request.getSeatId();

    // 1. Redis에 저장된 점유자 확인
    boolean isHolder = redisService.verifySeatHolder(seatId, userId);
    if (!isHolder) {
        throw new ConfirmFailedException();
    }

    // 2. 좌석 조회 및 상태 확인
    ShowSeat seat = showSeatRepository.findById(seatId)
        .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));

    if (seat.getStatus() != SeatStatus.HOLD || !seat.getHoldUserId().equals(userId)) {
        throw new ConfirmFailedException();
    }

    // 3. 예약 확정
    seat.reserve();

    // 4. Booking 엔티티 생성
    Booking booking = Booking.builder()
        .userId(userId)
        .scheduleId(seat.getShowSchedule().getId())
        .status(BookingStatus.CONFIRMED)
        .build();
    bookingRepository.save(booking);

    // 5. ReservationSeat (연결 테이블)
    ReservationSeat reservationSeat = ReservationSeat.builder()
        .booking(booking)
        .showSeat(seat)
        .build();
    reservationSeatRepository.save(reservationSeat);

    // 6. Redis 데이터 삭제
    redisService.releaseSeat(seatId);

    return ReservationResponse.Confirm.from(booking, seat);
}
```

### 5. 🛡️ 계층별 예외 처리

**예외 계층 구조**:
```java
AbstractException (추상 클래스)
    ├── UserAlreadyExistsException
    ├── UserNotFoundException
    ├── SeatAlreadyTakenException
    ├── ConfirmFailedException
    ├── InvalidAccessTokenException
    ├── ExpiredAccessTokenException
    └── RefreshTokenNotFoundException
```

**전역 예외 핸들러**:
```java
// CustomExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(AbstractException.class)
    public ResponseEntity<ErrorResponse> handleAbstractException(AbstractException e) {
        ErrorCode errorCode = e.getErrorCode();

        // 401 에러는 별도 로깅
        if (errorCode.getStatus() == 401) {
            log.warn("[SecurityException] {}: {}",
                e.getClass().getSimpleName(), e.getMessage());
        }

        ErrorResponse response = ErrorResponse.builder()
            .status(errorCode.getStatus())
            .message(errorCode.getMessage())
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(response);
    }
}
```

**ErrorCode 정의**:
```java
// ErrorCode.java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Token
    INVALID_ACCESS_TOKEN(401, "유효하지 않은 Access Token입니다."),
    EXPIRED_ACCESS_TOKEN(401, "만료된 Access Token입니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 Refresh Token입니다."),
    EXPIRED_REFRESH_TOKEN(401, "만료된 Refresh Token입니다."),
    REFRESH_TOKEN_NOT_FOUND(401, "Refresh Token을 찾을 수 없습니다."),

    // Reservation
    SEAT_ALREADY_HOLD(400, "이미 다른 사용자가 선택한 좌석입니다."),
    INVALID_CONFIRM(400, "HOLD 상태가 아니거나 다른 사용자가 HOLD한 좌석입니다."),

    // ...
}
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** 이상
- **Docker & Docker Compose**
- **MySQL 8.0** (로컬 개발 시)
- **Redis 7.x** (로컬 개발 시)

### 1️⃣ 프로젝트 클론

```bash
git clone https://github.com/yourusername/ticketReservationProject.git
cd ticketReservationProject
```

### 2️⃣ 환경 변수 설정

`src/main/resources/account_info.yml` 생성:

```yaml
# 로컬 개발용
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ticket_reservation?serverTimezone=Asia/Seoul
    username: your_db_username
    password: your_db_password

  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password  # 비밀번호 없으면 생략

  jwt:
    secretKey: your_jwt_secret_key_at_least_256_bits_long
    access-token-expiration: 3600000      # 1시간 (밀리초)
    refresh-token-expiration: 604800000   # 7일 (밀리초)
```

### 3️⃣ Docker Compose로 실행 (권장)

```bash
# 전체 스택 실행 (MySQL + Redis + Spring Boot)
docker-compose up -d

# 로그 확인
docker-compose logs -f tr-app

# 중지
docker-compose down

# 볼륨까지 삭제 (데이터 초기화)
docker-compose down -v
```

**서비스 접속 정보**:
- **API Server**: http://localhost:80
- **Swagger UI**: http://localhost:80/swagger-ui.html
- **MySQL**: localhost:3306
- **Redis**: localhost:6379

### 4️⃣ 로컬 개발 실행

```bash
# Gradle 빌드
./gradlew clean build

# Spring Boot 실행 (로컬 DB/Redis 설정 사용)
./gradlew bootRun
```

`dev` 프로필은 Docker Compose 내부 서비스명(`tr-db`, `tr-redis`)을 기본값으로 사용합니다. 로컬 JVM에서 `dev` 프로필을 직접 사용할 경우 `REDIS_HOST=127.0.0.1` 등 필요한 접속 정보를 명시하세요.

### 5️⃣ 데이터베이스 초기화

애플리케이션 실행 시 `ddl-auto: update` 설정으로 자동 테이블 생성됩니다.

**기본 Role 데이터 수동 입력**:
```sql
INSERT INTO role (name) VALUES ('ROLE_USER');
INSERT INTO role (name) VALUES ('ROLE_ADMIN');
```

---

## 📚 API 문서

### Swagger UI

애플리케이션 실행 후 접속:
```
http://localhost:8080/swagger-ui.html
```

### 주요 API 엔드포인트

#### 🔐 인증 API (`/api/auth`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/signup` | 회원가입 | Public |
| POST | `/api/auth/signin` | 로그인 (JWT 발급) | Public |
| POST | `/api/auth/refresh` | Access Token 수동 갱신 | Public |
| POST | `/api/auth/logout` | 로그아웃 (Refresh Token 삭제) | USER |

**회원가입 요청 예시**:
```json
POST /api/auth/signup
Content-Type: application/json

{
  "username": "user123",
  "password": "password123!",
  "name": "홍길동",
  "email": "user@example.com"
}
```

**로그인 응답 예시**:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### 🎭 공연 관리 API (`/api/shows`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/shows` | 공연 등록 | ADMIN |
| GET | `/api/shows` | 공연 목록 조회 | USER |
| GET | `/api/shows/{showId}` | 공연 상세 (일정+좌석) | USER |
| POST | `/api/shows/{showId}/schedule` | 일정 추가 | ADMIN |
| DELETE | `/api/shows/{showId}` | 공연 삭제 | ADMIN |

**공연 등록 요청**:
```json
POST /api/shows
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "title": "뮤지컬 레미제라블",
  "description": "감동의 무대",
  "runtime": 180
}
```

**일정 추가 요청**:
```json
POST /api/shows/1/schedule
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "startTimes": [
    "2024-02-01T19:00:00",
    "2024-02-02T19:00:00",
    "2024-02-03T14:00:00"
  ]
}
```

#### 🎫 예약 API (`/api/tickets`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/tickets/redis/reserve` | 좌석 HOLD (Redis 분산 락) | USER |
| POST | `/api/tickets/redis/confirm` | 예약 확정 | USER |
| POST | `/api/tickets/db/reserve` | 좌석 HOLD (DB 조건부 update) | USER |
| POST | `/api/tickets/db/confirm` | 예약 확정 (DB) | USER |
| GET | `/api/tickets/{reservationId}` | 예약 상세 조회 | USER |

**좌석 예약 (HOLD) 요청**:
```json
POST /api/tickets/redis/reserve
Authorization: Bearer {accessToken}
X-Refresh-Token: {refreshToken}
Content-Type: application/json

{
  "seatId": 123
}
```

**예약 확정 (CONFIRM) 요청**:
```json
POST /api/tickets/redis/confirm
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "seatId": 123
}
```

### 자동 토큰 갱신 사용법

**모든 API 요청 시 두 헤더를 함께 전송**:
```http
GET /api/shows
Authorization: Bearer {accessToken}
X-Refresh-Token: {refreshToken}
```

**Access Token이 만료된 경우 응답**:
```http
HTTP/1.1 200 OK
X-New-Access-Token: eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "data": [...]
}
```

**클라이언트는 `X-New-Access-Token` 헤더를 확인하여 새 토큰 저장**:
```javascript
const response = await fetch('/api/shows', {
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'X-Refresh-Token': refreshToken
  }
});

// 응답 헤더에서 새 토큰 확인
const newAccessToken = response.headers.get('X-New-Access-Token');
if (newAccessToken) {
  localStorage.setItem('accessToken', newAccessToken);
  console.log('✅ Token auto-refreshed');
}
```

---

## 🗄️ 데이터베이스 설계

### ERD

[📊 ERD Cloud 링크](https://www.erdcloud.com/d/Wh8SZhQ6Q3KHrdmPj)

### 주요 엔티티

```sql
-- 사용자
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    status CHAR(1) DEFAULT 'Y',  -- 'Y': 활성, 'N': 탈퇴
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 권한
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL  -- 'ROLE_USER', 'ROLE_ADMIN'
);

-- 사용자-권한 매핑
CREATE TABLE user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

-- 공연
CREATE TABLE show_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    runtime INT NOT NULL,  -- 러닝타임 (분)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 공연 일정
CREATE TABLE show_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    show_info_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (show_info_id) REFERENCES show_info(id) ON DELETE CASCADE
);

-- 좌석
CREATE TABLE show_seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    show_schedule_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,  -- 'A1', 'B5' 등
    price BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,  -- 'AVAILABLE', 'HOLD', 'RESERVED'
    hold_user_id BIGINT,
    hold_start_time TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (show_schedule_id) REFERENCES show_schedule(id) ON DELETE CASCADE
);

-- 예약
CREATE TABLE booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,  -- 'CONFIRMED', 'CANCELLED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 예약-좌석 매핑
CREATE TABLE reservation_seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES booking(id),
    FOREIGN KEY (seat_id) REFERENCES show_seat(id)
);
```

### 관계 다이어그램

```
User ──┐
       │
       ├─ UserRole ─ Role
       │
       └─ Booking ──┐
                    │
ShowInfo           │
    │              │
ShowSchedule       │
    │              │
ShowSeat ──────────┴─ ReservationSeat
```

**주요 관계**:
- User : UserRole : Role = N : M (다대다)
- ShowInfo : ShowSchedule = 1 : N (일대다, CASCADE)
- ShowSchedule : ShowSeat = 1 : N (일대다, CASCADE)
- Booking : ReservationSeat : ShowSeat = N : M (다대다)

---

## 🧪 테스트

### 동시성 테스트

**ReservationConcurrencyTest.java**:
```java
@Test
@DisplayName("3명이 동시에 1개 좌석 예약 - Redis 분산 락")
void testConcurrentReservation_Redis() throws Exception {
    // Given: 1개 좌석, 3명 사용자
    Long seatId = 1L;
    int numberOfThreads = 3;

    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);

    List<Future<ResultActions>> futures = new ArrayList<>();

    // When: 3명이 동시에 예약 시도
    for (int i = 0; i < numberOfThreads; i++) {
        futures.add(executor.submit(() -> {
            latch.countDown();
            latch.await();

            return mockMvc.perform(post("/api/tickets/redis/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createReserveRequest(seatId)));
        }));
    }

    // Then: 1명만 성공, 2명은 실패
    int successCount = 0;
    int failCount = 0;

    for (Future<ResultActions> future : futures) {
        ResultActions result = future.get();
        MvcResult mvcResult = result.andReturn();

        if (mvcResult.getResponse().getStatus() == 200) {
            successCount++;
        } else {
            failCount++;
        }
    }

    assertThat(successCount).isEqualTo(1);
    assertThat(failCount).isEqualTo(2);

    executor.shutdown();
}
```

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests ReservationConcurrencyTest

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

---

## 🔧 트러블슈팅

### 1. Redis 연결 실패

**증상**:
```
Unable to connect to Redis; nested exception is io.lettuce.core.RedisConnectionException
```

**해결**:
```bash
# Redis 실행 확인
docker ps | grep redis

# Redis 재시작
docker-compose restart tr-redis

# Redis 접속 테스트
redis-cli -h localhost -p 6379 ping
# 응답: PONG
```

### 2. JWT 서명 오류

**증상**:
```json
{
  "status": 401,
  "message": "유효하지 않은 Access Token입니다."
}
```

**원인**: `account_info.yml`의 `secretKey`가 충분히 길지 않음 (최소 256비트 필요)

**해결**:
```yaml
spring:
  jwt:
    secretKey: your_secret_key_must_be_at_least_256_bits_long_for_HS512_algorithm
```

### 3. 분산 락 타임아웃

**증상**:
```json
{
  "status": 400,
  "message": "이미 다른 사용자가 선택한 좌석입니다."
}
```

**원인**:
- Redis 응답 지연
- 락 대기 시간 초과 (3초)

**해결**:
```java
// ReservationService.java - 대기 시간 조정
boolean isLocked = lock.tryLock(5, TimeUnit.SECONDS);  // 3초 → 5초
```

### 4. Docker 볼륨 권한 문제

**증상**:
```
MySQL initialization failed: Permission denied
```

**해결**:
```bash
# 볼륨 삭제 후 재생성
docker-compose down -v
docker volume prune -f
docker-compose up -d
```

### 5. 포트 충돌

**증상**:
```
Bind for 0.0.0.0:3306 failed: port is already allocated
```

**해결**:
```bash
# 기존 프로세스 확인
lsof -i :3306

# 프로세스 종료
kill -9 <PID>

# 또는 docker-compose.yml에서 포트 변경
ports:
  - "3307:3306"  # 호스트 포트 변경
```

---

## 📈 향후 개선 계획

- [ ] **Token Rotation**: Refresh Token도 갱신 시 재발급
- [ ] **Rate Limiting**: Redis를 활용한 API 요청 제한
- [ ] **Monitoring**: Prometheus + Grafana 대시보드
- [ ] **Circuit Breaker**: Resilience4j 적용
- [ ] **Event Sourcing**: Kafka를 활용한 이벤트 기반 아키텍처
- [ ] **Multi-tenancy**: 여러 공연장 지원
- [ ] **WebSocket**: 실시간 좌석 현황 업데이트
- [ ] **Payment Integration**: 실제 PG 연동

---

## 👥 기여

이슈와 PR은 언제든 환영합니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 라이선스

This project is licensed under the MIT License.

---

## 📧 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

<div align="center">

**Built with ❤️ using Spring Boot**

[⬆ Back to top](#-real-time-ticket-reservation-system)

</div>
