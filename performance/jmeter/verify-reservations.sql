-- Set these values to the load-test target.
SET @seat_min = 51;
SET @seat_max = 100;
SET @schedule_id = 1;

-- Confirmed reservations for the tested schedule.
SELECT COUNT(*) AS confirmed_booking_count
FROM booking
WHERE schedule_id = @schedule_id
  AND status = 'CONFIRMED';

-- Reserved seats in the tested seat range.
SELECT COUNT(*) AS reserved_seat_count
FROM show_seat
WHERE id BETWEEN @seat_min AND @seat_max
  AND status = 'RESERVED';

-- This query must return zero rows.
SELECT rs.seat_id, COUNT(*) AS confirmed_count
FROM reservation_seat rs
JOIN booking b ON b.id = rs.booking_id
WHERE rs.seat_id BETWEEN @seat_min AND @seat_max
  AND b.status = 'CONFIRMED'
GROUP BY rs.seat_id
HAVING COUNT(*) > 1;
