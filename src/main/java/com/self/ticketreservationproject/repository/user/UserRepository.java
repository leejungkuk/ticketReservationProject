package com.self.ticketreservationproject.repository.user;

import com.self.ticketreservationproject.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsernameAndStatus(String username, Character status);

  boolean existsByUsername(String username);

  long findUserIdByUsernameAndStatus(String username,  Character status);

}
