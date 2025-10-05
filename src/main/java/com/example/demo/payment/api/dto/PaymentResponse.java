package com.example.demo.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private String status;
    private String method;
    private double amount;
    private String approvedAt;
}
