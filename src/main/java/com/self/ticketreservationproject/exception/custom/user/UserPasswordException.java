package com.self.ticketreservationproject.exception.custom.user;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class UserPasswordException extends AbstractException {

  public UserPasswordException() {
    super(ErrorCode.INVALID_PASSWORD);
  }
}
