package com.example.demo.payment.service;

import com.example.demo.member.domain.Member;
import com.example.demo.order.domain.Order;
import com.example.demo.order.domain.OrderStatus;
import com.example.demo.order.service.OrderService;
import com.example.demo.payment.api.dto.PaymentApproveRequest;
import com.example.demo.payment.api.dto.PaymentCreateRequest;
import com.example.demo.payment.api.dto.PaymentResponse;
import com.example.demo.payment.domain.Payment;
import com.example.demo.payment.domain.PaymentMethod;
import com.example.demo.payment.domain.PaymentStatus;
import com.example.demo.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;
    @Mock
    OrderService orderService;

    @InjectMocks
    PaymentService paymentService;

    // ---------- request() ----------
    @Test
    @DisplayName("request: 주문 존재 & 금액 일치 → PAYMENT REQUESTED 저장 및 DTO 반환")
    void request_success() {
        // given
        long orderId = 10L;

        Member member = Member.builder()
                .MemberId(1L)
                .name("홍길동")
                .email("hong@test.com")
                .build();

        Order order = Order.builder()
                .OrderId(orderId)
                .member(member)
                .orderDate(LocalDateTime.of(2025, 10, 3, 12, 0, 0))
                .totalAmount(new BigDecimal("50000.00"))
                .status(OrderStatus.CREATED)
                .build();

        when(orderService.get(orderId)).thenReturn(order);

        // save()가 반환할 저장된 Payment (id 포함)
        Payment saved = Payment.builder()
                .PaymentId(100L)
                .order(order)
                .amount(new BigDecimal("50000.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.REQUESTED)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        PaymentCreateRequest req = new PaymentCreateRequest(orderId, 50000.0, PaymentMethod.CARD);

        // when
        PaymentResponse res = paymentService.request(req);

        // then
        assertThat(res.getPaymentId()).isEqualTo(100L);
        assertThat(res.getOrderId()).isEqualTo(orderId);
        assertThat(res.getMethod()).isEqualTo("CARD");
        assertThat(res.getStatus()).isEqualTo("REQUESTED");
        assertThat(res.getAmount()).isEqualTo(50000.0);
        assertThat(res.getApprovedAt()).isNull();

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment toSave = captor.getValue();
        assertThat(toSave.getOrder()).isSameAs(order);
        assertThat(toSave.getAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(toSave.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(toSave.getStatus()).isEqualTo(PaymentStatus.REQUESTED);

        verify(orderService).get(orderId);
        verifyNoMoreInteractions(orderService, paymentRepository);
    }

    @Test
    @DisplayName("request: 주문 없음 → IllegalArgumentException('order not found')")
    void request_order_not_found() {
        when(orderService.get(999L)).thenThrow(new IllegalArgumentException("order not found"));
        PaymentCreateRequest req = new PaymentCreateRequest(999L, 1000.0, PaymentMethod.CARD);

        assertThatThrownBy(() -> paymentService.request(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order not found");

        verify(orderService).get(999L);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("request: 금액 불일치 → IllegalArgumentException('payment amount must equal order amount')")
    void request_amount_mismatch() {
        // given
        long orderId = 11L;

        Member member = Member.builder()
                .MemberId(2L)
                .name("최인준")
                .email("lim@test.com")
                .build();

        Order order = Order.builder()
                .OrderId(orderId)
                .member(member)
                .orderDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("10000.00")) // 주문 금액 10,000
                .status(OrderStatus.CREATED)
                .build();

        when(orderService.get(orderId)).thenReturn(order);

        PaymentCreateRequest req = new PaymentCreateRequest(orderId, 5000.0, PaymentMethod.CARD); // 5,000 → 불일치

        // expect
        assertThatThrownBy(() -> paymentService.request(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("payment amount must equal order amount");

        verify(orderService).get(orderId);
        verify(paymentRepository, never()).save(any());
    }

    // ---------- approve() ----------

    @Test
    @DisplayName("approve: REQUESTED 결제 승인 → APPROVED & approvedAt 세팅, 주문 상태 PAID")
    void approve_success() {
        // given
        long orderId = 20L;
        long paymentId = 200L;

        Member member = Member.builder()
                .MemberId(3L)
                .name("서아름")
                .email("seo@test.com")
                .build();

        Order order = Order.builder()
                .OrderId(orderId)
                .member(member)
                .orderDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("42000.00"))
                .status(OrderStatus.CREATED)
                .build();

        Payment payment = Payment.builder()
                .PaymentId(paymentId)
                .order(order)
                .amount(new BigDecimal("42000.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.REQUESTED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // when
        PaymentResponse res = paymentService.approve(new PaymentApproveRequest(paymentId));

        // then: 응답
        assertThat(res.getPaymentId()).isEqualTo(paymentId);
        assertThat(res.getOrderId()).isEqualTo(orderId);
        assertThat(res.getStatus()).isEqualTo("APPROVED");
        assertThat(res.getMethod()).isEqualTo("CARD");
        assertThat(res.getAmount()).isEqualTo(42000.0);
        assertThat(res.getApprovedAt()).isNotNull();

        // 도메인 상태 전이까지 검증
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getApprovedAt()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        verify(paymentRepository).findById(paymentId);
        // approve는 영속 상태 변경이라 save 호출이 필수는 아님(JPA flush로 반영)
        verifyNoMoreInteractions(paymentRepository, orderService);
    }

    @Test
    @DisplayName("approve: 결제 없음 → IllegalArgumentException('payment not found')")
    void approve_not_found() {
        when(paymentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.approve(new PaymentApproveRequest(404L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("payment not found");

        verify(paymentRepository).findById(404L);
        verifyNoMoreInteractions(paymentRepository);
    }

    // ---------- getDto() ----------

    @Test
    @DisplayName("getDto: 존재하는 결제를 DTO로 매핑")
    void getDto_success() {
        long orderId = 33L;
        long paymentId = 330L;

        Member member = Member.builder()
                .MemberId(9L)
                .name("풀솦")
                .email("pulsop@test.com")
                .build();

        Order order = Order.builder()
                .OrderId(orderId)
                .member(member)
                .orderDate(LocalDateTime.of(2025, 10, 3, 14, 0, 0))
                .totalAmount(new BigDecimal("15000.00"))
                .status(OrderStatus.CREATED)
                .build();

        LocalDateTime approvedAt = LocalDateTime.of(2025, 10, 3, 14, 30, 0);

        Payment payment = Payment.builder()
                .PaymentId(paymentId)
                .order(order)
                .amount(new BigDecimal("15000.00"))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.REQUESTED)
                .build();

        payment.approve(approvedAt);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        PaymentResponse res = paymentService.getDto(paymentId);

        assertThat(res.getPaymentId()).isEqualTo(paymentId);
        assertThat(res.getOrderId()).isEqualTo(orderId);
        assertThat(res.getStatus()).isEqualTo("APPROVED");
        assertThat(res.getMethod()).isEqualTo("CARD");
        assertThat(res.getAmount()).isEqualTo(15000.0);
        assertThat(res.getApprovedAt()).isEqualTo(approvedAt.toString());
    }

    @Test
    @DisplayName("getDto: 결제 없음 → IllegalArgumentException('payment not found')")
    void getDto_not_found() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getDto(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("payment not found");

        verify(paymentRepository).findById(999L);
    }
}