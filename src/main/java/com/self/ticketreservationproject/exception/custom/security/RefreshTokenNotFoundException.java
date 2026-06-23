package com.self.ticketreservationproject.exception.custom.security;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class RefreshTokenNotFoundException extends AbstractException {

  public RefreshTokenNotFoundException() {
    super(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
  }
}
