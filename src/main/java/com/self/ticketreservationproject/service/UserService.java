package com.self.ticketreservationproject.service;

import com.self.ticketreservationproject.domain.Role;
import com.self.ticketreservationproject.domain.User;
import com.self.ticketreservationproject.domain.UserRole;
import com.self.ticketreservationproject.dto.UserDto;
import com.self.ticketreservationproject.repository.RoleRepository;
import com.self.ticketreservationproject.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public User createUser(UserDto.RegisterRequest registerRequest) {
    registerRequest.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

    // role 조회
    Role role = roleRepository.findByName("ROLE_USER")
        .orElseThrow(() -> new RuntimeException("Role not found"));

    // entity로 변환
    User user = registerRequest.toEntity();

    // role 추가
    UserRole userRole = UserRole.builder()
        .role(role)
        .user(user)
        .build();

    // user entity에 추가
    user.getUserRoles().add(userRole);

    return userRepository.save(user);
  }
}
