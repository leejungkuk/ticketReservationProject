package com.self.ticketreservationproject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.self.ticketreservationproject.domain.user.User;
import com.self.ticketreservationproject.dto.user.UserRequest;
import com.self.ticketreservationproject.repository.user.UserQueryRepository;
import com.self.ticketreservationproject.repository.user.UserRepository;
import com.self.ticketreservationproject.service.UserService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class UserServiceTest {

  @Autowired
  private UserService userService;
  @Autowired
  private UserQueryRepository userQueryRepository;
  @Autowired
  private UserRepository userRepository;

  @Test
  void createTest() {
    String username = unique("user");
    UserRequest.RegisterRequest registerRequest = registerRequest(username);

    User saved = userService.createUser(registerRequest);

    assertThat(saved.getUsername()).isEqualTo(username);
    assertThat(saved.getStatus()).isEqualTo('Y');
  }

  @Test
  void userLoginTest() {
    String username = unique("login");
    userService.createUser(registerRequest(username));

    UserRequest.SignInRequest user =  new UserRequest.SignInRequest();
    user.setUsername(username);
    user.setPassword("password1234");

    User getUser = userService.authenticate(user);

    assertThat(getUser.getUsername()).isEqualTo(username);
  }

  @Test
  void updateUserTest() {
    String username = unique("update");
    userService.createUser(registerRequest(username));

    UserRequest.UpdateRequest updateUser = new UserRequest.UpdateRequest();
    updateUser.setUsername(username);
    updateUser.setPassword("test12345");
    updateUser.setEmail(username + "@updated.test");

    User updated = userService.updateUser(updateUser);

    assertThat(updated.getEmail()).isEqualTo(username + "@updated.test");
  }

  @Test
  void deleteUserTest() {
    String username = unique("delete");
    userService.createUser(registerRequest(username));

    UserRequest.UpdateRequest updateUser = new UserRequest.UpdateRequest();
    updateUser.setUsername(username);
    userService.deleteUser(updateUser);

    User deleted = userRepository.findByUsernameAndStatus(username, 'N').orElseThrow();
    assertThat(deleted.getStatus()).isEqualTo('N');
  }

  @Test
  void getRolesTest() {
    String username = unique("roles");
    User user = userService.createUser(registerRequest(username));

    Set<String> roleNames = userQueryRepository.findRoleNamesByUserId(user.getId());

    assertThat(roleNames.size()).isEqualTo(1);
    assertThat(roleNames.contains("ROLE_USER")).isTrue();
  }

  @Test
  void getUserTest() {
    String username = unique("select");
    userService.createUser(registerRequest(username));
    Character status = 'Y';
    User user = userRepository.findByUsernameAndStatus(username, status).orElseThrow();

    assertThat(user.getUsername()).isEqualTo(username);
    assertThat(user.getStatus()).isEqualTo(status);
  }

  private UserRequest.RegisterRequest registerRequest(String username) {
    UserRequest.RegisterRequest registerRequest = new UserRequest.RegisterRequest();
    registerRequest.setUsername(username);
    registerRequest.setPassword("password1234");
    registerRequest.setEmail(username + "@email.com");
    registerRequest.setName("name");
    return registerRequest;
  }

  private String unique(String prefix) {
    return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }
}
