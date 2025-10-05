package com.example.demo.member.service;

import com.example.demo.member.api.dto.MemberCreateRequest;
import com.example.demo.member.api.dto.MemberResponse;
import com.example.demo.member.domain.Member;
import com.example.demo.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse register(MemberCreateRequest req) {
        memberRepository.findByEmail(req.getEmail()).ifPresent(m -> {
            throw new IllegalArgumentException("email exists");
        });
        Member saved = memberRepository.save(Member.create(req.getName(), req.getEmail()));
        return new MemberResponse(saved.getMemberId(), saved.getName(), saved.getEmail());
    }

    public Member get(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));
    }

    public MemberResponse getDto(Long id) {
        Member m = get(id);
        return new MemberResponse(m.getMemberId(), m.getName(), m.getEmail());
    }
}
