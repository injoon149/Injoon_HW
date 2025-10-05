package com.example.demo.payment.api.dto;

import com.example.demo.payment.domain.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRequest {
    private Long orderId;
    private double amount;
    private PaymentMethod method;
}