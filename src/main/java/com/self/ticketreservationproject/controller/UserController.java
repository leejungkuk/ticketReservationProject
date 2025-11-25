package com.self.ticketreservationproject.controller;

import com.self.ticketreservationproject.domain.User;
import com.self.ticketreservationproject.dto.UserDto;
import com.self.ticketreservationproject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(summary = "회원 가입 API")
  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody UserDto.RegisterRequest user) {
    User result = userService.createUser(user);
    return ResponseEntity.ok(result);
  }

}
