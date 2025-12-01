package com.self.ticketreservationproject.dto.user;

import com.self.ticketreservationproject.domain.user.UserRole;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;

public class UserResponse {
  @Data
  @Builder
  public static class RegisterResponse {
    private String username;
    private String name;
    private String email;
    private String message;
  }

  @Data
  @Builder
  public static class SignInResponse {
    private String username;
    private Set<String> roles;
    private String accessToken;

    public static Set<String> roleTypeCasting(Set<UserRole> userRoles) {
      return userRoles.stream()
          .map(userRole -> userRole.getRole().getName())
          .collect(Collectors.toSet());
    }
  }

  @Data
  @Builder
  public static class UpdateResponse {
    private String username;
    private String email;
    private String message;
  }

  @Data
  @Builder
  public static class DeleteResponse {
    private String message;
  }
}
