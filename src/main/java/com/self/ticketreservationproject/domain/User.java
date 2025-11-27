package com.self.ticketreservationproject.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.self.ticketreservationproject.dto.user.UserRequest.UpdateRequest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "user_info")
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

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

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnoreProperties("user")
  @Builder.Default
  private Set<UserRole> userRoles = new HashSet<>();


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return userRoles.stream()
        .map(userRoles -> new SimpleGrantedAuthority(userRoles.getRole().getName()))
        .collect(Collectors.toSet());
  }

  public void updateUser(UpdateRequest updateUser) {
    Optional.ofNullable(updateUser.getPassword()).ifPresent(password -> this.password = password);
    Optional.ofNullable(updateUser.getEmail()).ifPresent(email -> this.email = email);
  }

  public void deleteUser() {
    this.status = 'N';
  }
}
