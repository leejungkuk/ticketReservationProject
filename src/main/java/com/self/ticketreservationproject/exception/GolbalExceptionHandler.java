package com.self.ticketreservationproject.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GolbalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getAllErrors().isEmpty()
        ? "Validation failed"
        : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

    Map<String, Object> response = new HashMap<>();
    response.put("code", HttpStatus.BAD_REQUEST.value());
    response.put("message", message);

    return ResponseEntity.badRequest().body(response);
  }
}
