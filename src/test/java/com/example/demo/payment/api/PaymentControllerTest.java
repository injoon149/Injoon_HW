package com.example.demo.payment.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper om;

    /**  회원 하나 만들고 id 반환 */
    private long createMember(String name, String email) throws Exception {
        String body = String.format("{\"name\":\"%s\",\"email\":\"%s\"}", name, email);
        MvcResult res = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").exists())
                .andReturn();
        JsonNode node = om.readTree(res.getResponse().getContentAsString());
        return node.get("memberId").asLong();
    }

    /**  주문 하나 만들고 id 반환 */
    private long createOrder(long memberId, double amount) throws Exception {
        String body = String.format("{\"memberId\": %d, \"amount\": %.2f}", memberId, amount);
        MvcResult res = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andReturn();
        JsonNode node = om.readTree(res.getResponse().getContentAsString());
        return node.get("orderId").asLong();
    }

    @Test
    @DisplayName("결제 요청 → 조회 (REQUESTED)")
    void request_then_get_success() throws Exception {
        long memberId = createMember("홍길동", "hong+pay@test.com");
        long orderId = createOrder(memberId, 50000.0);

        // 1) 결제 요청
        String payReqBody = String.format("{\"orderId\": %d, \"amount\": 50000, \"method\": \"CARD\"}", orderId);
        MvcResult payCreate = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payReqBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").exists())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.method").value("CARD"))
                .andExpect(jsonPath("$.amount").value(50000.0))
                .andExpect(jsonPath("$.approvedAt").doesNotExist())
                .andReturn();

        long paymentId = om.readTree(payCreate.getResponse().getContentAsString())
                .get("paymentId").asLong();
        assertThat(paymentId).isPositive();

        // 2) 결제 조회
        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.approvedAt").doesNotExist());
    }

    @Test
    @DisplayName("결제 승인 → 상태 APPROVED & approvedAt 존재, 주문 상태 PAID")
    void approve_success() throws Exception {
        long memberId = createMember("최인준", "choi@test.com");
        long orderId = createOrder(memberId, 42000.0);

        // 결제 요청
        String payReqBody = String.format("{\"orderId\": %d, \"amount\": 42000, \"method\": \"CARD\"}", orderId);
        long paymentId = om.readTree(
                mockMvc.perform(post("/api/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payReqBody))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("paymentId").asLong();

        // 승인
        mockMvc.perform(post("/api/payments/{id}/approve", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedAt").exists());

        // 주문 상태가 PAID로 전이됐는지 확인
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 결제 요청 → 404")
    void request_order_not_found() throws Exception {
        String body = "{\"orderId\": 999999, \"amount\": 1000, \"method\": \"CARD\"}";
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("결제 요청 금액 불일치 → 400 (payment amount must equal order amount)")
    void request_amount_mismatch() throws Exception {
        long memberId = createMember("서아름", "SEO@test.com");
        long orderId = createOrder(memberId, 10000.0);

        String body = String.format("{\"orderId\": %d, \"amount\": 9999, \"method\": \"CARD\"}", orderId);
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("payment amount must equal order amount"));
    }

    @Test
    @DisplayName("존재하지 않는 결제 승인 → 404")
    void approve_not_found() throws Exception {
        mockMvc.perform(post("/api/payments/{id}/approve", 987654321L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("payment not found"));
    }

    @Test
    @DisplayName("결제 조회: 존재하지 않음 → 404")
    void get_not_found() throws Exception {
        mockMvc.perform(get("/api/payments/{id}", 123456789L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("payment not found"));
    }
}