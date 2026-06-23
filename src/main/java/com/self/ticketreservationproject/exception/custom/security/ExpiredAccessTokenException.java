package com.self.ticketreservationproject.exception.custom.security;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class ExpiredAccessTokenException extends AbstractException {

  public ExpiredAccessTokenException() {
    super(ErrorCode.EXPIRED_ACCESS_TOKEN);
  }
}
