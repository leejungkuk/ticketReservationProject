package com.self.ticketreservationproject.controller;

import com.self.ticketreservationproject.domain.User;
import com.self.ticketreservationproject.dto.UserDto;
import com.self.ticketreservationproject.dto.UserDto.UserInfo;
import com.self.ticketreservationproject.security.JwtUtil;
import com.self.ticketreservationproject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;
  private final JwtUtil jwtUtil;

  @Operation(summary = "회원 가입 API")
  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody UserDto.RegisterRequest user) {
    User result = userService.createUser(user);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "로그인 API")
  @PostMapping("/signin")
  public ResponseEntity<?> signin(@RequestBody UserDto.SignIn userInfo) {
    UserInfo user = userService.authenticate(userInfo);
    var token = jwtUtil.generateToken(user.getUsername(), user.getRoles());
    log.info(token);
    return ResponseEntity.ok(user);
  }

  @Operation(summary = "회원 정보 수정")
  @PatchMapping("/user")
  public ResponseEntity<?> updateUserInfo(@RequestBody UserDto.UpdateUser userInfo) {
    userService.updateUser(userInfo);
    return ResponseEntity.ok(Map.of("message", "수정 완료되었습니다."));
  }

  @Operation(summary = "회원 정보 삭제")
  @DeleteMapping("/user")
  public ResponseEntity<?> deleteUserInfo(@RequestBody UserDto.UpdateUser userInfo) {
    userService.deleteUser(userInfo);
    return ResponseEntity.ok(Map.of("message", "삭제 완료되었습니다."));
  }

  @GetMapping("/admin/test")
  public String adminOnly() {

    return "admin ok";
  }

  @GetMapping("/user/test")
  public String userOnly() {
    return "user ok";
  }


}
