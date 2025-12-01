package com.self.ticketreservationproject.exception.custom.show;

import com.self.ticketreservationproject.exception.AbstractException;
import com.self.ticketreservationproject.exception.ErrorCode;

public class ShowNotFoundException extends AbstractException {

  public ShowNotFoundException() {
    super(ErrorCode.SHOW_NOT_FOUND);
  }
}
