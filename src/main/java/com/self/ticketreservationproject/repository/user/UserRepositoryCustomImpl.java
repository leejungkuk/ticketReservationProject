package com.self.ticketreservationproject.repository.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.self.ticketreservationproject.domain.user.QUser;
import com.self.ticketreservationproject.exception.custom.user.UserNotExistException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QUser user = QUser.user;

  @Override
  public long findUserIdByUsernameAndStatus(String username, char status) {
    Long id = queryFactory
        .select(user.id)
        .from(user)
        .where(user.username.eq(username).and(user.status.eq(status)))
        .fetchOne();
    if(id == null) {
      throw new UserNotExistException();
    }
    return id;
  }
}
