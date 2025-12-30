package com.self.ticketreservationproject.repository.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.self.ticketreservationproject.domain.user.QRole;
import com.self.ticketreservationproject.domain.user.QUserRole;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final QRole role = QRole.role;
  private final QUserRole userRole = QUserRole.userRole;

  public Set<String> findRoleNamesByUserId(Long userId) {
    List<String> result = queryFactory
        .select(role.name)
        .from(userRole)
        .join(userRole.role, role)
        .where(userRole.user.id.eq(userId))
        .fetch();
    return new HashSet<>(result);
  }


}
