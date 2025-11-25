package com.self.ticketreservationproject.service;

import com.self.ticketreservationproject.domain.Role;
import com.self.ticketreservationproject.domain.User;
import com.self.ticketreservationproject.domain.UserRole;
import com.self.ticketreservationproject.dto.UserDto;
import com.self.ticketreservationproject.repository.RoleRepository;
import com.self.ticketreservationproject.repository.UserRepository;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public User createUser(UserDto.RegisterRequest registerRequest) {
    registerRequest.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

    if(userRepository.existsByUsername(registerRequest.getUsername())) {
      throw new RuntimeException("이미 존재하는 ID 입니다.");
    }

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

  public UserDto.UserInfo authenticate(UserDto.SignIn userInfo) {
    User user = userRepository.findByUsernameAndStatus(userInfo.getUsername(), 'Y')
        .orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));

    // 권한 String 변환
    Set<String> roleNames = user.getUserRoles().stream()
        .map(userRole -> userRole.getRole().getName())
        .collect(Collectors.toSet());
    UserDto.UserInfo result = UserDto.UserInfo.fromEntity(user, roleNames);

    if(!passwordEncoder.matches(userInfo.getPassword(), user.getPassword())) {
      throw new RuntimeException("비밀번호가 일치하지 않습니다.");
    }

    return result;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return (UserDetails) userRepository.findByUsernameAndStatus(username, 'Y')
        .orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));
  }

  @Transactional
  public void updateUser(UserDto.UpdateUser updateUser) {
    User user = userRepository.findByUsernameAndStatus(updateUser.getUsername(), 'Y')
        .orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));

    updateUser.setPassword(passwordEncoder.encode(updateUser.getPassword()));
    user.updateUser(updateUser);
  }

  @Transactional
  public void deleteUser(UserDto.UpdateUser updateUser) {
    User user = userRepository.findByUsernameAndStatus(updateUser.getUsername(), 'Y')
        .orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));

    // soft delete 처리
    user.deleteUser();
  }


}
