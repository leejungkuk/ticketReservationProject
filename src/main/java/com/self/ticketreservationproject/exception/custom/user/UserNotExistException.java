package com.self.ticketreservationproject.exception.custom.user;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class UserNotExistException extends AbstractException {

  public UserNotExistException() {
    super(ErrorCode.NOT_EXIST_USERNAME);
  }
}
