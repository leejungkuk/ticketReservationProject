package com.self.ticketreservationproject.exception.custom.show;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class AlreadyExistShowException extends AbstractException {

  public AlreadyExistShowException() {
    super(ErrorCode.SHOW_ALREADY_EXIST);
  }
}
