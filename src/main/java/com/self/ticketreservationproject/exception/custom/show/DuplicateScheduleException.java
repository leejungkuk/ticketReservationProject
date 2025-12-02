package com.self.ticketreservationproject.exception.custom.show;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class DuplicateScheduleException extends AbstractException {

  public DuplicateScheduleException() {
    super(ErrorCode.SCHEDULE_ALREADY_EXIST);
  }
}
