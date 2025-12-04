package com.self.ticketreservationproject.dto.payment;

import lombok.Data;

@Data
public class PaymentRequest {
  private int amount;
  private String method;
  private String status;
  private String paidAt;
}
