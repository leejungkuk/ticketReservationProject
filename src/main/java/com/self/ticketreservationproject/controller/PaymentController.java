package com.self.ticketreservationproject.controller;

import com.self.ticketreservationproject.dto.payment.PaymentRequest;
import com.self.ticketreservationproject.dto.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

  @PostMapping
  public ResponseEntity<PaymentResponse> confirmPayment(@RequestBody PaymentRequest request) {
    boolean isSuccess = mockPayment(request);

    if (!isSuccess) {
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(PaymentResponse.builder()
              .status("FAILED")
              .message("결제 실패")
              .build());
    }

    return ResponseEntity.ok(
        PaymentResponse.builder()
            .status("SUCCESS")
            .message("결제 성공")
            .build()
    );
  }

  private boolean mockPayment(PaymentRequest request) {
    if (request.getAmount() <= 0) return false;

    return true;
  }

}
