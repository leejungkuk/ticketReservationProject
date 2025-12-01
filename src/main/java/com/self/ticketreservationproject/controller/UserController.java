package com.self.ticketreservationproject.controller;

import com.self.ticketreservationproject.domain.user.User;
import com.self.ticketreservationproject.dto.user.UserRequest;
import com.self.ticketreservationproject.dto.user.UserRequest.SignInRequest;
import com.self.ticketreservationproject.dto.user.UserRequest.UpdateRequest;
import com.self.ticketreservationproject.dto.user.UserResponse.DeleteResponse;
import com.self.ticketreservationproject.dto.user.UserResponse.RegisterResponse;
import com.self.ticketreservationproject.dto.user.UserResponse.SignInResponse;
import com.self.ticketreservationproject.dto.user.UserResponse.UpdateResponse;
import com.self.ticketreservationproject.security.JwtUtil;
import com.self.ticketreservationproject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
  public ResponseEntity<RegisterResponse> signup(@Valid @RequestBody UserRequest.RegisterRequest request) {
    User user = userService.createUser(request);

    RegisterResponse response =  RegisterResponse.builder()
        .username(user.getUsername())
        .name(user.getName())
        .email(user.getEmail())
        .message("회원가입 완료")
        .build();

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "로그인 API")
  @PostMapping("/signin")
  public ResponseEntity<SignInResponse> signin(@RequestBody SignInRequest request) {
    User user = userService.authenticate(request);

    // 권한 변경
    Set<String> role = SignInResponse.roleTypeCasting(user.getUserRoles());
    String token = jwtUtil.generateToken(user.getUsername(), role);

    SignInResponse response = SignInResponse.builder()
        .username(user.getUsername())
        .roles(role)
        .accessToken(token)
        .build();

    log.info(token);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "관리자용 회원 정보 수정")
  @PatchMapping("/admin/user")
  public ResponseEntity<UpdateResponse> updateUserInfo(@RequestBody UpdateRequest request) {
    User user = userService.updateUser(request);

    UpdateResponse response = UpdateResponse.builder()
        .username(user.getUsername())
        .email(user.getEmail())
        .message("수정 완료되었습니다.")
        .build();

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "관리자용 회원 정보 삭제")
  @DeleteMapping("/admin/user")
  public ResponseEntity<DeleteResponse> deleteUserInfo(@RequestBody UpdateRequest request) {
    userService.deleteUser(request);

    DeleteResponse response = DeleteResponse.builder()
        .message("삭제 완료되었습니다.")
        .build();

    return ResponseEntity.ok(response);
  }

}
