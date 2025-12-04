package com.self.ticketreservationproject.service;

import com.self.ticketreservationproject.domain.user.Role;
import com.self.ticketreservationproject.domain.user.User;
import com.self.ticketreservationproject.domain.user.UserRole;
import com.self.ticketreservationproject.dto.user.UserRequest;
import com.self.ticketreservationproject.exception.custom.user.UserAlreadyExsitsException;
import com.self.ticketreservationproject.exception.custom.user.UserNotExistException;
import com.self.ticketreservationproject.exception.custom.user.UserPasswordException;
import com.self.ticketreservationproject.repository.user.RoleRepository;
import com.self.ticketreservationproject.repository.user.UserRepository;
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
  public User createUser(UserRequest.RegisterRequest registerRequest) {
    registerRequest.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

    if(userRepository.existsByUsername(registerRequest.getUsername())) {
      throw new UserAlreadyExsitsException();
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

  public User authenticate(UserRequest.SignInRequest userInfo) {
    User user = userRepository.findByUsernameAndStatus(userInfo.getUsername(), 'Y')
        .orElseThrow(UserNotExistException::new);

    if(!passwordEncoder.matches(userInfo.getPassword(), user.getPassword())) {
      throw new UserPasswordException();
    }

    return user;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return (UserDetails) userRepository.findByUsernameAndStatus(username, 'Y')
        .orElseThrow(UserNotExistException::new);
  }

  @Transactional
  public User updateUser(UserRequest.UpdateRequest request) {
    User user = userRepository.findByUsernameAndStatus(request.getUsername(), 'Y')
        .orElseThrow(UserNotExistException::new);

    request.setPassword(passwordEncoder.encode(request.getPassword()));
    user.updateUser(request);
    return user;
  }

  @Transactional
  public void deleteUser(UserRequest.UpdateRequest request) {
    User user = userRepository.findByUsernameAndStatus(request.getUsername(), 'Y')
        .orElseThrow(UserNotExistException::new);

    // soft delete 처리
    user.deleteUser();
  }


}
