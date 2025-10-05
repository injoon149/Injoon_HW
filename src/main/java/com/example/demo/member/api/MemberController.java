package com.example.demo.member.api;


import com.example.demo.member.api.dto.MemberCreateRequest;
import com.example.demo.member.api.dto.MemberResponse;
import com.example.demo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    public MemberResponse register(@RequestBody MemberCreateRequest req){
        return memberService.register(req);
    }

    @GetMapping("/{id}")
    public MemberResponse get(@PathVariable Long id) {
        return memberService.getDto(id);
    }
}
