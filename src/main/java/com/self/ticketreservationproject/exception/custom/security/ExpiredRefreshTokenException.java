package com.self.ticketreservationproject.exception.custom.security;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class ExpiredRefreshTokenException extends AbstractException {

  public ExpiredRefreshTokenException() {
    super(ErrorCode.EXPIRED_REFRESH_TOKEN);
  }
}
