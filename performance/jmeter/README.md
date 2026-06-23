# JMeter Load Test

This folder keeps the ticket reservation load-test assets reproducible from the repository.

## Scenario

- Flow: sign in -> seat HOLD -> mock payment -> seat CONFIRM
- Strategies:
  - `redis`: `/api/tickets/redis/reserve`, `/api/tickets/redis/confirm`
  - `db`: `/api/tickets/db/reserve`, `/api/tickets/db/confirm`
- Default contention range: 50 seats, `seatId` 51-100
- Portfolio-safe scope: a JMeter load-test scenario built with 10,000 generated test accounts against the same 50-seat schedule

In this scenario, failed reservation responses for already-taken seats are expected. The plan marks expected `400`/`409` conflicts as successful samples so the flow can continue, so JMeter success counts must not be described as successful reservations. The main validation point is not that all requests succeed, but that confirmed reservations never exceed the available seat count and duplicate seat confirmation does not occur.

## Prerequisites

- MySQL, Redis, and the Spring Boot app are running.
- The target show schedule and seats exist. Create a dedicated load-test show/schedule before running the reservation plan, then set `seatMin` and `seatMax` to the actual generated seat ID range.
- The 10,000 test accounts exist and match the generated CSV credentials.

## Generate User CSV

```bash
./performance/jmeter/generate-users.sh
```

This creates `performance/jmeter/generated/users.csv` with `loaduser00001` through `loaduser10000`.

## Seed Test Accounts

```bash
/Users/j/dev/apache/apache-jmeter-5.6.3/bin/jmeter \
  -n \
  -t performance/jmeter/seed-users.jmx \
  -l performance/jmeter/results/seed-users.jtl \
  -Jhost=127.0.0.1 \
  -Jport=80 \
  -Jusers=10000 \
  -Jramp=60 \
  -JusersCsv=performance/jmeter/generated/users.csv
```

The seed plan treats both first-time signup success and duplicate-user rejection as acceptable so it can be rerun against an already seeded database.

## Run

Redis strategy:

```bash
/Users/j/dev/apache/apache-jmeter-5.6.3/bin/jmeter \
  -n \
  -t performance/jmeter/ticket-reservation-load-test.jmx \
  -l performance/jmeter/results/redis-10000.jtl \
  -e \
  -o performance/jmeter/report/redis-10000 \
  -Jhost=127.0.0.1 \
  -Jport=80 \
  -Jstrategy=redis \
  -Jusers=10000 \
  -Jramp=1 \
  -JseatMin=<actual first seat id> \
  -JseatMax=<actual last seat id> \
  -JusersCsv=performance/jmeter/generated/users.csv
```

DB strategy:

```bash
/Users/j/dev/apache/apache-jmeter-5.6.3/bin/jmeter \
  -n \
  -t performance/jmeter/ticket-reservation-load-test.jmx \
  -l performance/jmeter/results/db-10000.jtl \
  -e \
  -o performance/jmeter/report/db-10000 \
  -Jhost=127.0.0.1 \
  -Jport=80 \
  -Jstrategy=db \
  -Jusers=10000 \
  -Jramp=1 \
  -JseatMin=<actual first seat id> \
  -JseatMax=<actual last seat id> \
  -JusersCsv=performance/jmeter/generated/users.csv
```

## Verify Result

After a run, verify the database state with `verify-reservations.sql` by setting the tested seat range and schedule ID. The portfolio claim should be based on this verification, not on JMeter HTTP status counts alone.

## Portfolio Wording

Use measured numbers only after saving the JMeter report and verifying the database:

> JMeter 10,000 테스트 계정 기반 부하 테스트 시나리오를 구성했습니다. 동일 공연 회차의 50개 좌석에 HOLD -> CONFIRM 요청을 발생시키고, 이미 점유된 좌석 요청은 예상 충돌 응답으로 분리했습니다. 실행 후 DB 검증 쿼리로 confirmed 예약 수와 좌석별 중복 확정 여부를 확인했습니다.

If exact metrics are available, add:

> 테스트 환경: Docker Compose 기반 Spring Boot, MySQL 8, Redis 7.4. JMeter 5.6.3, 10,000 테스트 계정, ramp-up 1s. 결과: HOLD 200 응답 N건, 예상 충돌 응답 N건, 평균 응답 시간 N ms, p95 N ms, 처리량 N req/s, confirmed 예약 N건, 중복 확정 0건.
