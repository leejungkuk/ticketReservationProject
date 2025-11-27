package com.self.ticketreservationproject.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(AbstractException.class)
  protected ResponseEntity<ErrorResponse> handleAbstractException(AbstractException e) {
    log.error("[CustomException] {}", e.getMessage());

    ErrorCode errorCode = e.getErrorCode();

    ErrorResponse response = ErrorResponse.builder()
        .code(errorCode.getStatus())
        .message(errorCode.getMessage())
        .build();

    return ResponseEntity
        .status(e.getStatusCode())
        .body(response);

  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getAllErrors().isEmpty()
        ? "Validation failed"
        : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

    ErrorResponse response = ErrorResponse.builder()
        .code(HttpStatus.BAD_REQUEST.value())
        .message(message)
        .build();

    return ResponseEntity.badRequest().body(response);
  }

}
