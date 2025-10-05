package com.example.demo.order.service;

import com.example.demo.member.domain.Member;
import com.example.demo.member.service.MemberService;
import com.example.demo.order.api.dto.OrderCreateRequest;
import com.example.demo.order.api.dto.OrderResponse;
import com.example.demo.order.domain.Order;
import com.example.demo.order.domain.OrderStatus;
import com.example.demo.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    MemberService memberService;

    @InjectMocks
    OrderService orderService;

    @Test
    @DisplayName("create: 존재하는 회원으로 주문 생성 → 저장 후 DTO 매핑")
    void create_success() {
        // given
        OrderCreateRequest req = new OrderCreateRequest(1L, 50000.0);

        Member member = Member.builder()
                .MemberId(1L)
                .name("홍길동")
                .email("hong@test.com")
                .build();

        when(memberService.get(1L)).thenReturn(member);

        // save()가 반환할 '저장된' 주문 (id 포함)
        LocalDateTime now = LocalDateTime.of(2025, 10, 3, 12, 30, 0);
        Order saved = Order.builder()
                .OrderId(100L)
                .member(member)
                .status(OrderStatus.CREATED)
                .orderDate(now)
                .totalAmount(new BigDecimal("50000.00"))
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        // when
        OrderResponse res = orderService.create(req);

        // then
        assertThat(res.getOrderId()).isEqualTo(100L);
        assertThat(res.getMemberId()).isEqualTo(1L);
        assertThat(res.getStatus()).isEqualTo("CREATED");
        assertThat(res.getOrderDate()).isEqualTo(now.toString());
        assertThat(res.getAmount()).isEqualTo(50000.0);

        verify(memberService).get(1L);
        verify(orderRepository).save(any(Order.class));
        verifyNoMoreInteractions(memberService, orderRepository);
    }

    @Test
    @DisplayName("create: 회원이 없으면 memberService.get()에서 예외")
    void create_member_not_found_throws() {
        // given
        OrderCreateRequest req = new OrderCreateRequest(999L, 1000.0);
        when(memberService.get(999L))
                .thenThrow(new IllegalArgumentException("member not found"));

        // expect
        assertThatThrownBy(() -> orderService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("member not found");

        verify(memberService).get(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("get: 존재하는 주문을 반환")
    void get_success() {
        // given
        Member member = Member.builder()
                .MemberId(1L)
                .name("홍길동")
                .email("hong@test.com")
                .build();

        LocalDateTime whenOrdered = LocalDateTime.of(2025, 10, 3, 12, 0, 0);
        Order entity = Order.builder()
                .OrderId(10L)
                .member(member)
                .status(OrderStatus.CREATED)
                .orderDate(whenOrdered)
                .totalAmount(new BigDecimal("12345.67"))
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(entity));

        // when
        Order found = orderService.get(10L);

        // then
        assertThat(found.getOrderId()).isEqualTo(10L);
        assertThat(found.getMember().getMemberId()).isEqualTo(1L);
        assertThat(found.getStatus().name()).isEqualTo("CREATED");
        assertThat(found.getOrderDate()).isEqualTo(whenOrdered);
        assertThat(found.getTotalAmount()).isEqualTo(new BigDecimal("12345.67"));
    }

    @Test
    @DisplayName("get: 주문이 없으면 IllegalArgumentException('order not found')")
    void get_not_found_throws() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.get(404L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order not found");
    }

    @Test
    @DisplayName("getDto: 주문을 조회해 DTO로 매핑")
    void getDto_success() {
        Member member = Member.builder()
                .MemberId(3L)
                .name("최인준")
                .email("lim@test.com")
                .build();

        LocalDateTime whenOrdered = LocalDateTime.of(2025, 10, 3, 13, 0, 0);
        Order entity = Order.builder()
                .OrderId(77L)
                .member(member)
                .status(OrderStatus.CREATED)
                .orderDate(whenOrdered)
                .totalAmount(new BigDecimal("50000.00"))
                .build();

        when(orderRepository.findById(77L)).thenReturn(Optional.of(entity));

        OrderResponse res = orderService.getDto(77L);

        assertThat(res.getOrderId()).isEqualTo(77L);
        assertThat(res.getMemberId()).isEqualTo(3L);
        assertThat(res.getStatus()).isEqualTo("CREATED");
        assertThat(res.getOrderDate()).isEqualTo(whenOrdered.toString());
        assertThat(res.getAmount()).isEqualTo(50000.0);
    }
}