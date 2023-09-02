package com.webnorm.prototypever1.controller;

import com.webnorm.prototypever1.api.request.MemberLoginRequest;
import com.webnorm.prototypever1.api.request.MemberSignupRequest;
import com.webnorm.prototypever1.api.response.MemberListResponse;
import com.webnorm.prototypever1.entity.Member;
import com.webnorm.prototypever1.service.MemberService;
import com.webnorm.prototypever1.security.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Slf4j
public class MemberController {
    private final MemberService memberService;
    private final EmailController emailController;

    // 회원가입 : request(dto)를 member 엔티티에 매핑, 메일 발송
    @PostMapping
    public ResponseEntity signup(@RequestBody MemberSignupRequest request) {
        //System.out.println(request.getUserId());
        Member member = Member.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        memberService.createMember(member);
        emailController.sendWelcomeEmail(member.getEmail(), member.getFirstName()+ " " + member.getLastName());
        return ResponseEntity.ok().build();
    }

    // 회원목록 조회(관리자) : response(dto) 리스트를 stream을 이용해 member 리스트로 매핑
    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity memberList() {
        List<Member> findMembers = memberService.findAllMember();
        List<MemberListResponse> collect = findMembers.stream()
                .map(m -> MemberListResponse.builder()
                        .email(m.getEmail())
                        .firstName(m.getFirstName())
                        .lastName(m.getLastName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(collect);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody MemberLoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        TokenInfo tokenInfo = memberService.login(email, password);
        return ResponseEntity.ok(tokenInfo);
    }
}
