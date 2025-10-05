package com.example.demo.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private Long OrderId;
    private Long memberId;
    private String status;
    private String orderDate;
    private double amount;
}