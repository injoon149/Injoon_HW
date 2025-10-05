package com.example.demo.member.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponse {
    private Long memberId;
    private String name;
    private String email;
}
