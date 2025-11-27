package com.self.ticketreservationproject.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
  private int code;
  private String message;

  public static ErrorResponse error(ErrorCode errorCode) {
    return ErrorResponse.builder()
        .code(errorCode.getStatus())
        .message(errorCode.getMessage())
        .build();
  }
}
