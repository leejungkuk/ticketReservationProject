package com.self.ticketreservationproject;

import com.self.ticketreservationproject.dto.UserDto;
import com.self.ticketreservationproject.dto.UserDto.UserInfo;
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
    UserDto.RegisterRequest registerRequest = new UserDto.RegisterRequest();
    registerRequest.setUsername("username");
    registerRequest.setPassword("password");
    registerRequest.setEmail("email@email.com");
    registerRequest.setName("name");

    userService.createUser(registerRequest);

  }

  @Test
  void userLoginTest() {
    UserDto.SignIn user =  new UserDto.SignIn();
    user.setUsername("ADMIN");
    user.setPassword("admin12345");

    UserInfo getUser = userService.authenticate(user);

  }

  @Test
  void updateUserTest() {
    UserDto.UpdateUser updateUser = new UserDto.UpdateUser();
    updateUser.setUsername("test1");
    updateUser.setPassword("test12345");
//    updateUser.setEmail("test1@email.com");
    userService.updateUser(updateUser);
  }

  @Test
  void deleteUserTest() {
    UserDto.UpdateUser updateUser = new UserDto.UpdateUser();
    updateUser.setUsername("test4");
    userService.deleteUser(updateUser);
  }
}
