package com.self.ticketreservationproject.repository.user;

public interface UserRepositoryCustom {
  long findUserIdByUsernameAndStatus(String username, char status);
}
