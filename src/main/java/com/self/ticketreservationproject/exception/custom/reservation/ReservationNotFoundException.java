package com.self.ticketreservationproject.exception.custom.reservation;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class ReservationNotFoundException extends AbstractException {

  public ReservationNotFoundException() {
    super(ErrorCode.NOT_EXIST_RESERVATION);
  }
}
