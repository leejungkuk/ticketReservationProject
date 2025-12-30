package com.self.ticketreservationproject.domain.user;

import com.self.ticketreservationproject.dto.user.UserRequest.UpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_info")
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String username;
  private String name;
  private String email;
  private String password;
  @Column(length = 1)
  private Character status;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime created_at;

  @LastModifiedDate
  private LocalDateTime updated_at;

  public void updateUser(UpdateRequest updateUser) {
    Optional.ofNullable(updateUser.getPassword()).ifPresent(password -> this.password = password);
    Optional.ofNullable(updateUser.getEmail()).ifPresent(email -> this.email = email);
  }

  public void deleteUser() {
    this.status = 'N';
  }

}
