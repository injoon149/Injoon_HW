package com.example.demo.member.api;

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
class MemberControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/members → 200, 반환된 id로 GET /api/members/{id} 성공")
    void create_then_get_member_success() throws Exception {
        // given
        String body = """
            {
              "name": "홍길동",
              "email": "hong@test.com"
            }
            """;

        // when: 생성
        MvcResult createResult = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").exists())
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("hong@test.com"))
                .andReturn();

        // then: 응답에서 id 추출하여 조회 검증
        String json = createResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        long id = root.get("memberId").asLong();
        assertThat(id).isPositive();

        mockMvc.perform(get("/api/members/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(id))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("hong@test.com"));
    }

    @Test
    @DisplayName("POST /api/members 중복 email → 에러 상태 코드 반환")
    void create_member_duplicate_email_error() throws Exception {
        // given
        String body1 = """
            {"name":"A","email":"dup@test.com"}
            """;
        String body2 = """
            {"name":"B","email":"dup@test.com"}
            """;

        // 첫 번째는 성공
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isOk());

        // 두 번째는 중복 이메일로 예외 발생
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isBadRequest())  // ← 400 기대
                .andExpect(jsonPath("$.message").value("email exists"));
    }

    @Test
    @DisplayName("GET /api/members/{id} 존재하지 않으면 에러")
    void get_member_not_found() throws Exception {
        mockMvc.perform(get("/api/members/{id}", 999_999L))
                .andExpect(status().isNotFound())                 // ✅ 404
                .andExpect(jsonPath("$.message").value("member not found"));
    }
}