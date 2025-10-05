package com.example.demo.payment.api;


import com.example.demo.payment.api.dto.PaymentApproveRequest;
import com.example.demo.payment.api.dto.PaymentCreateRequest;
import com.example.demo.payment.api.dto.PaymentResponse;
import com.example.demo.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public PaymentResponse request(@RequestBody PaymentCreateRequest req) {
        return paymentService.request(req);
    }

    @PostMapping("/{id}/approve")
    public PaymentResponse approve(@PathVariable long id) {
        return paymentService.approve(new PaymentApproveRequest(id));
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable long id) {
        return paymentService.getDto(id);
    }
}
