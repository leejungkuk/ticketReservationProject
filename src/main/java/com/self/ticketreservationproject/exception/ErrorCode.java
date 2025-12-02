package com.self.ticketreservationproject.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  INVALID_REQUEST(400, "잘못된 요청입니다."),
  // user
  USERNAME_ALREADY_EXIST(400, "이미 존재하는 ID 입니다."),
  INVALID_PASSWORD(400, "비밀번호가 일치하지 않습니다."),
  NOT_EXIST_USERNAME(400, "존재하지 않는 ID 입니다."),
  // show
  SHOW_ALREADY_EXIST(400, "이미 존재하는 공연입니다."),
  SHOW_NOT_FOUND(400, "존재하지 않는 공연입니다."),
  SCHEDULE_ALREADY_EXIST(400, "이미 존재하는 일정입니다."),
  // global
  NOT_FOUND(404, "대상을 찾을 수 없습니다."),
  SERVER_ERROR(500, "서버 오류입니다.");

  private final int status;
  private final String message;
}
