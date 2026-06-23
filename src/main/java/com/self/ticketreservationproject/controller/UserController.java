package com.self.ticketreservationproject.controller;

import com.self.ticketreservationproject.domain.user.User;
import com.self.ticketreservationproject.dto.user.UserRequest;
import com.self.ticketreservationproject.dto.user.UserRequest.RefreshTokenRequest;
import com.self.ticketreservationproject.dto.user.UserRequest.SignInRequest;
import com.self.ticketreservationproject.dto.user.UserRequest.UpdateRequest;
import com.self.ticketreservationproject.dto.user.UserResponse.DeleteResponse;
import com.self.ticketreservationproject.dto.user.UserResponse.RegisterResponse;
import com.self.ticketreservationproject.dto.user.UserResponse.SignInResponse;
import com.self.ticketreservationproject.dto.user.UserResponse.UpdateResponse;
import com.self.ticketreservationproject.security.JwtUtil;
import com.self.ticketreservationproject.service.RefreshTokenService;
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
  private final RefreshTokenService refreshTokenService;

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

    // 권한 조회
    Set<String> roles = userService.getRoles(user.getId());
    String accessToken = jwtUtil.generateToken(user.getUsername(), roles);
    String refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

    SignInResponse response = SignInResponse.builder()
        .username(user.getUsername())
        .roles(roles)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();

    log.info("User signed in: {}", user.getUsername());
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

  @Operation(summary = "토큰 갱신 API")
  @PostMapping("/refresh")
  public ResponseEntity<SignInResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();

    // Refresh token 검증
    if (!jwtUtil.validateToken(refreshToken)) {
      return ResponseEntity.status(401).build();
    }

    String username = jwtUtil.getUsername(refreshToken);

    // Redis에 저장된 refresh token과 비교
    if (!refreshTokenService.validateRefreshToken(username, refreshToken)) {
      return ResponseEntity.status(401).build();
    }

    // 새로운 access token 발급
    Set<String> roles = userService.getRolesByUsername(username);
    String newAccessToken = jwtUtil.generateToken(username, roles);

    SignInResponse response = SignInResponse.builder()
        .username(username)
        .roles(roles)
        .accessToken(newAccessToken)
        .refreshToken(refreshToken)
        .build();

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "로그아웃 API")
  @PostMapping("/logout")
  public ResponseEntity<DeleteResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();

    if (jwtUtil.validateToken(refreshToken)) {
      String username = jwtUtil.getUsername(refreshToken);
      if (refreshTokenService.validateRefreshToken(username, refreshToken)) {
        refreshTokenService.deleteByUsername(username);
      }
    }

    DeleteResponse response = DeleteResponse.builder()
        .message("로그아웃 되었습니다.")
        .build();

    return ResponseEntity.ok(response);
  }

}
