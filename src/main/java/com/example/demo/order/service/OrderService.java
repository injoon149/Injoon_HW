package com.example.demo.order.service;

import com.example.demo.member.domain.Member;
import com.example.demo.order.api.dto.OrderCreateRequest;
import com.example.demo.order.api.dto.OrderResponse;
import com.example.demo.order.domain.Order;
import com.example.demo.order.repository.OrderRepository;
import com.example.demo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberService memberService;
    private final Clock clock = Clock.systemDefaultZone();

    @Transactional
    public OrderResponse create(OrderCreateRequest req) {
        Member member = memberService.get(req.getMemberId());
        Order order = Order.create(member,
                BigDecimal.valueOf(req.getAmount()),
                LocalDateTime.now(clock));
        Order saved = orderRepository.save(order);

        return new OrderResponse(
                saved.getOrderId(),
                saved.getMember().getMemberId(),
                saved.getStatus().name(),
                saved.getOrderDate().toString(),
                saved.getTotalAmount().doubleValue()
        );
    }

    public Order get(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));
    }

    public OrderResponse getDto(Long id) {
        Order o = get(id);
        return new OrderResponse(
                o.getOrderId(),
                o.getMember().getMemberId(),
                o.getStatus().name(),
                o.getOrderDate().toString(),
                o.getTotalAmount().doubleValue()
        );
    }
}
