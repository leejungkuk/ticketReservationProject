package com.self.ticketreservationproject.exception.custom.reservation;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class ConfirmFailedException extends AbstractException {

  public ConfirmFailedException() {
    super(ErrorCode.INVALID_CONFIRM);
  }
}
