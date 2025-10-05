package com.example.demo.order.domain;

import com.example.demo.member.domain.Member;
import com.example.demo.payment.domain.Payment;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long OrderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Payment payment;

    public static Order create(Member member, BigDecimal amount, LocalDateTime now) {
        if (amount == null || amount.signum() < 0) throw new IllegalArgumentException("amount must be >= 0");
        Order order = Order.builder()
                .member(member)
                .orderDate(now)
                .totalAmount(amount.setScale(2))
                .status(OrderStatus.CREATED)
                .build();
        member.addOrder(order);
        return order;
    }

    public void markPaid() {
        if (this.status == OrderStatus.PAID) return;
        this.status = OrderStatus.PAID;
    }

    public void attachPayment(Payment payment) { this.payment = payment; }
}
