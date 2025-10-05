package com.example.demo.payment.domain;


import com.example.demo.order.domain.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PaymentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;
    private LocalDateTime approvedAt;

    public static Payment request(Order order, BigDecimal amount, PaymentMethod method) {
        if (order == null) throw new IllegalArgumentException("order required");
        if (amount == null || amount.setScale(2).compareTo(order.getTotalAmount()) != 0)
            throw new IllegalArgumentException("payment amount must equal order amount");
        var p = Payment.builder()
                .order(order)
                .amount(amount.setScale(2))
                .method(method)
                .status(PaymentStatus.REQUESTED)
                .build();
        order.attachPayment(p);
        return p;
    }

    public void approve(LocalDateTime now) {
        if (this.status != PaymentStatus.REQUESTED) throw new IllegalStateException("already processed");
        this.status = PaymentStatus.APPROVED;
        this.approvedAt = now;
        this.order.markPaid();
    }
}
