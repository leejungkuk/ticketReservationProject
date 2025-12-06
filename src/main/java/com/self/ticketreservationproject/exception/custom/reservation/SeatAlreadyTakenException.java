package com.self.ticketreservationproject.exception.custom.reservation;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class SeatAlreadyTakenException extends AbstractException {

  public SeatAlreadyTakenException() {
    super(ErrorCode.SEAT_ALREADY_HOLD);
  }
}
