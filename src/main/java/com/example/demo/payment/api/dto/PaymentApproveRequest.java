package com.example.demo.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentApproveRequest {
    private Long paymentId;
}
