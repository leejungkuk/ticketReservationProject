package com.self.ticketreservationproject.dto;

import com.self.ticketreservationproject.domain.User;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import lombok.Builder.Default;
import lombok.Data;

public class UserDto {

  @Data
  public static class RegisterRequest {
    @NotBlank(message = "ID는 필수 항목입니다.")
    @Size(min = 4, max = 30, message = "ID는 4자 이상 30자 이하이어야 합니다.")
    private String username;

    @NotBlank(message = "이름은 필수 항목입니다.")
    private String name;

    @Nullable
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;


    public User toEntity() {
      return User.builder()
          .username(this.username)
          .name(this.name)
          .email(this.email)
          .password(this.password)
          .build();
    }
  }

  @Data
  public static class SignIn {
    private String username;
    private String password;
  }

}
