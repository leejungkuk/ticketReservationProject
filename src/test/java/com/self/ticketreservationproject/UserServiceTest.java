package com.self.ticketreservationproject;

import com.self.ticketreservationproject.domain.user.User;
import com.self.ticketreservationproject.dto.user.UserRequest;
import com.self.ticketreservationproject.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {

  @Autowired
  private UserService userService;

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
}
