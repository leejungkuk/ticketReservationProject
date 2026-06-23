package com.self.ticketreservationproject.repository.user;

import com.self.ticketreservationproject.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsernameAndStatus(String username, Character status);

  boolean existsByUsername(String username);

  @Query("select u.id from User u where u.username = :username and u.status = :status")
  long findUserIdByUsernameAndStatus(@Param("username") String username,
      @Param("status") Character status);

}
