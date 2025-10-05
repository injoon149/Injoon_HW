package com.example.demo.order.api;

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
class OrderControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper om;

    @Test
    @DisplayName("POST /api/orders → 저장 성공 후 GET /api/orders/{id} 조회")
    void create_then_get_order_success() throws Exception {
        // 1) 회원 생성
        String memberBody = """
            {"name":"홍길동","email":"hong@test.com"}
            """;
        MvcResult memberCreate = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        JsonNode memberNode = om.readTree(memberCreate.getResponse().getContentAsString());
        long memberId = memberNode.get("id").asLong();
        assertThat(memberId).isPositive();

        // 2) 주문 생성
        String orderBody = String.format("""
            {"memberId": %d, "amount": 50000}
            """, memberId);

        MvcResult orderCreate = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.amount").value(50000.0))
                .andReturn();

        JsonNode orderNode = om.readTree(orderCreate.getResponse().getContentAsString());
        long orderId = orderNode.get("orderId").asLong();
        assertThat(orderId).isPositive();

        // 3) 주문 조회
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.amount").value(50000.0))
                .andExpect(jsonPath("$.orderDate").exists());
    }

    /**
     * 멤버가 존재하지 않는 경우: memberService.get() 에서 not found → 전역 예외 처리로 404 반환
     */
    @Test
    @DisplayName("POST /api/orders - 존재하지 않는 memberId → 404")
    void create_order_member_not_found() throws Exception {
        String body = """
            {"memberId": 999999, "amount": 1000}
            """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * 음수/유효하지 않은 금액: Order.create()에서 IllegalArgumentException("amount must be >= 0")
     * → 전역 예외 처리로 400 반환
     */
    @Test
    @DisplayName("POST /api/orders - amount<0 → 400 Bad Request")
    void create_order_invalid_amount() throws Exception {
        // 먼저 회원 하나 생성
        String memberBody = """
            {"name":"최인준","email":"lim@test.com"}
            """;
        MvcResult memberCreate = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody))
                .andExpect(status().isOk())
                .andReturn();
        long memberId = om.readTree(memberCreate.getResponse().getContentAsString())
                .get("id").asLong();

        // 음수 금액으로 주문 생성
        String badOrderBody = String.format("""
            {"memberId": %d, "amount": -1}
            """, memberId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badOrderBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("amount must be >= 0"));
    }
}