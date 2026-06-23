package com.self.ticketreservationproject.exception.custom.security;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class InvalidAccessTokenException extends AbstractException {

  public InvalidAccessTokenException() {
    super(ErrorCode.INVALID_ACCESS_TOKEN);
  }
}
