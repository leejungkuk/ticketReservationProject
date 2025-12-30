package com.self.ticketreservationproject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.self.ticketreservationproject.domain.user.User;
import com.self.ticketreservationproject.dto.user.UserRequest;
import com.self.ticketreservationproject.exception.custom.user.UserNotExistException;
import com.self.ticketreservationproject.repository.user.UserQueryRepository;
import com.self.ticketreservationproject.repository.user.UserRepository;
import com.self.ticketreservationproject.service.UserService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {

  @Autowired
  private UserService userService;
  @Autowired
  private UserQueryRepository userQueryRepository;
  @Autowired
  private UserRepository userRepository;

  @Test
  void createTest() {
    UserRequest.RegisterRequest registerRequest = new UserRequest.RegisterRequest();
    registerRequest.setUsername("username");
    registerRequest.setPassword("password");
    registerRequest.setEmail("email@email.com");
    registerRequest.setName("name");

    userService.createUser(registerRequest);

  }

  @Test
  void userLoginTest() {
    UserRequest.SignInRequest user =  new UserRequest.SignInRequest();
    user.setUsername("ADMIN");
    user.setPassword("admin12345");

    User getUser = userService.authenticate(user);

  }

  @Test
  void updateUserTest() {
    UserRequest.UpdateRequest updateUser = new UserRequest.UpdateRequest();
    updateUser.setUsername("test1");
    updateUser.setPassword("test12345");
//    updateUser.setEmail("test1@email.com");
    userService.updateUser(updateUser);
  }

  @Test
  void deleteUserTest() {
    UserRequest.UpdateRequest updateUser = new UserRequest.UpdateRequest();
    updateUser.setUsername("test4");
    userService.deleteUser(updateUser);
  }

  @Test
  void getRolesTest() {
    Set<String> roleNames = userQueryRepository.findRoleNamesByUserId(7L);
    assertThat(roleNames.size()).isEqualTo(2);
    assertThat(roleNames.contains("ROLE_ADMIN")).isTrue();
    assertThat(roleNames.contains("ROLE_USER")).isTrue();
  }

  @Test
  void getUserTest() {
    String username = "ADMIN";
    Character status = 'Y';
    User user = userRepository.findByUsernameAndStatus(username, status).orElseThrow(
        UserNotExistException::new);
    assertThat(user.getUsername()).isEqualTo(username);
    assertThat(user.getStatus()).isEqualTo(status);
  }
}
