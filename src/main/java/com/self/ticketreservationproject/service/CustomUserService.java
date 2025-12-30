package com.self.ticketreservationproject.service;

import com.self.ticketreservationproject.domain.user.User;
import com.self.ticketreservationproject.exception.custom.user.UserNotExistException;
import com.self.ticketreservationproject.repository.user.UserQueryRepository;
import com.self.ticketreservationproject.repository.user.UserRepository;
import com.self.ticketreservationproject.security.CustomUserDetails;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserQueryRepository userQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 1. 사용자 조회
    User user = userRepository.findByUsernameAndStatus(username, 'Y').orElseThrow(
        UserNotExistException::new);

    // 2. 권한 조회
    Set<String> roleNames = userQueryRepository.findRoleNamesByUserId(user.getId());

    // security 권한 객체 리스트로 변환
    List<SimpleGrantedAuthority> authorities = roleNames.stream()
        .map(SimpleGrantedAuthority::new)
        .toList();

    return new CustomUserDetails(user, authorities);
  }
}
