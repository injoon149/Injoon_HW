package com.example.demo.member.service;

import com.example.demo.member.api.dto.MemberCreateRequest;
import com.example.demo.member.api.dto.MemberResponse;
import com.example.demo.member.domain.Member;
import com.example.demo.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("register: 신규 이메일이면 저장 후 DTO 반환")
    void register_success() {
        // given
        MemberCreateRequest req = new MemberCreateRequest("홍길동", "hong@test.com");
        when(memberRepository.findByEmail("hong@test.com")).thenReturn(Optional.empty());

        // save()가 반환할 'DB에 저장된 상태'의 엔티티(id 포함) — 빌더로 생성
        Member saved = Member.builder()
                .MemberId(1L)
                .name("홍길동")
                .email("hong@test.com")
                .build();
        when(memberRepository.save(any(Member.class))).thenReturn(saved);

        // when
        MemberResponse res = memberService.register(req);

        // then
        assertThat(res.getMemberId()).isEqualTo(1L);
        assertThat(res.getName()).isEqualTo("홍길동");
        assertThat(res.getEmail()).isEqualTo("hong@test.com");
        verify(memberRepository).findByEmail("hong@test.com");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("register: 중복 이메일이면 IllegalArgumentException 발생")
    void register_duplicateEmail_throws() {
        // given
        MemberCreateRequest req = new MemberCreateRequest("홍길동", "dup@test.com");
        Member existing = Member.builder()
                .MemberId(10L)
                .name("기존사용자")
                .email("dup@test.com")
                .build();
        when(memberRepository.findByEmail("dup@test.com")).thenReturn(Optional.of(existing));

        // expect
        assertThatThrownBy(() -> memberService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("email exists");

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("get: 존재하는 id면 Member 반환")
    void get_success() {
        // given
        Member foundEntity = Member.builder()
                .MemberId(7L)
                .name("홍길동")
                .email("hong@test.com")
                .build();
        when(memberRepository.findById(7L)).thenReturn(Optional.of(foundEntity));

        // when
        Member found = memberService.get(7L);

        // then
        assertThat(found.getMemberId()).isEqualTo(7L);
        assertThat(found.getName()).isEqualTo("홍길동");
        assertThat(found.getEmail()).isEqualTo("hong@test.com");
    }

    @Test
    @DisplayName("get: 존재하지 않으면 IllegalArgumentException 발생")
    void get_notFound_throws() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.get(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("member not found");
    }

    @Test
    @DisplayName("getDto: Member를 조회해 MemberResponse로 매핑")
    void getDto_success() {
        Member entity = Member.builder()
                .MemberId(5L)
                .name("홍길동")
                .email("hong@test.com")
                .build();
        when(memberRepository.findById(5L)).thenReturn(Optional.of(entity));

        MemberResponse res = memberService.getDto(5L);

        assertThat(res.getMemberId()).isEqualTo(5L);
        assertThat(res.getName()).isEqualTo("홍길동");
        assertThat(res.getEmail()).isEqualTo("hong@test.com");
    }
}