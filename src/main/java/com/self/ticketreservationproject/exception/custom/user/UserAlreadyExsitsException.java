package com.self.ticketreservationproject.exception.custom.user;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class UserAlreadyExsitsException extends AbstractException {

  public UserAlreadyExsitsException() {
    super(ErrorCode.USERNAME_ALREADY_EXIST);
  }
}
