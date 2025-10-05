package com.example.demo.order.api;

import com.example.demo.order.api.dto.OrderCreateRequest;
import com.example.demo.order.api.dto.OrderResponse;
import com.example.demo.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public OrderResponse create(@RequestBody OrderCreateRequest req)
    {
        return orderService.create(req);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id)
    {
        return orderService.getDto(id);
    }
}
