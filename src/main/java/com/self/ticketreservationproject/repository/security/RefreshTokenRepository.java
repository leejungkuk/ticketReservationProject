package com.self.ticketreservationproject.repository.security;

import com.self.ticketreservationproject.security.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
