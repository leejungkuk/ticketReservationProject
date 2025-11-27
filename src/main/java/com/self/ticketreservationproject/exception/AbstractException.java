package com.self.ticketreservationproject.exception;

import lombok.Getter;

@Getter
public class AbstractException extends RuntimeException {

  private final ErrorCode errorCode;

  public AbstractException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public int getStatusCode() {
    return errorCode.getStatus();
  }


}
