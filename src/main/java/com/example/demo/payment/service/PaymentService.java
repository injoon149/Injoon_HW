package com.example.demo.payment.service;

import com.example.demo.order.domain.Order;
import com.example.demo.payment.api.dto.PaymentApproveRequest;
import com.example.demo.payment.api.dto.PaymentCreateRequest;
import com.example.demo.payment.api.dto.PaymentResponse;
import com.example.demo.payment.domain.Payment;
import com.example.demo.payment.repository.PaymentRepository;
import com.example.demo.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final Clock clock = Clock.systemDefaultZone();

    @Transactional
    public PaymentResponse request(PaymentCreateRequest req) {
        Order order = orderService.get(req.getOrderId());
        Payment payment = Payment.request(order,
                BigDecimal.valueOf(req.getAmount()),
                req.getMethod());
        Payment saved = paymentRepository.save(payment);
        return toRes(saved);
    }

    @Transactional
    public PaymentResponse approve(PaymentApproveRequest req) {
        Payment payment = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("payment not found"));
        payment.approve(LocalDateTime.now(clock));
        return toRes(payment);
    }

    public PaymentResponse getDto(Long id) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("payment not found"));
        return toRes(p);
    }

    private static PaymentResponse toRes(Payment p) {
        return new PaymentResponse(
                p.getPaymentId(),
                p.getOrder().getOrderId(),
                p.getStatus().name(),
                p.getMethod().name(),
                p.getAmount().doubleValue(),
                p.getApprovedAt() == null ? null : p.getApprovedAt().toString()
        );
    }
}
