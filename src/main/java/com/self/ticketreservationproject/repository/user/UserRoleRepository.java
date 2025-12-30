package com.self.ticketreservationproject.repository.user;

import com.self.ticketreservationproject.domain.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

}
