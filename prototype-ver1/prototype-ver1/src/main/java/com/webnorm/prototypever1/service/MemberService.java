package com.webnorm.prototypever1.service;

import com.webnorm.prototypever1.entity.member.Member;
import com.webnorm.prototypever1.entity.redis.RefreshToken;
import com.webnorm.prototypever1.exception.Exceptions.AuthException;
import com.webnorm.prototypever1.exception.Exceptions.BusinessLogicException;
import com.webnorm.prototypever1.exception.Exceptions.MemberException;
import com.webnorm.prototypever1.repository.MemberRepository;
import com.webnorm.prototypever1.repository.RefreshTokenRepository;
import com.webnorm.prototypever1.security.provider.JwtTokenProvider;
import com.webnorm.prototypever1.security.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    /*
    * [회원가입]
    * # 아이디(이메일) 중복검사
    * # 비밀번호 인코딩
    * # 이메일 전송
    */
    public Member createMember(Member member) {
        member.encodePassword(passwordEncoder);
        Optional<Member> findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember.isPresent())
            throw new BusinessLogicException(MemberException.USER_EXIST);
        return memberRepository.save(member);
    }

    // 회원목록 조회(관리자)
    public List<Member> findAllMember() {
        return memberRepository.findAll();
    }

    /*
     * [로그인]
     * */
    public TokenInfo login(String memberId, String password) {
        // login id, pw 값을 넣어 Authentication 객체 생성 (authenticated = false)
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberId, password);
        // 사용자 인증 (id로 사용자를 불러와 pw 체크)
        Authentication authentication = authenticationManagerBuilder
                .getObject()
                .authenticate(authenticationToken);
        // 인증 결과를 넣어 atk 생성
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        // refresh token 생성해서 redis 에 저장
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .id(memberId)
                        .refreshToken(jwtTokenProvider.generateRefreshToken(authentication))
                        .accessToken(accessToken)
                        .build()
        );
        // TokenInfo 생성 후 리턴
        TokenInfo tokenInfo = TokenInfo.builder()
                .accessToken(accessToken)
                .grantType("Bearer")
                .build();
        return tokenInfo;
    }

    /*
     * [ATK 재발급]
     * RTK 를 받아 유효성 검증
     * 유효한 경우 새로운 ATK 리턴
     * */
    public TokenInfo reissueToken(String refreshToken, String accessToken) {
        // rtk 가 존재하고 rtk, atk 모두 유효한 경우
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)
                && jwtTokenProvider.validateToken(accessToken)) {
            // Authentication 객체 생성 (ATK 기반)
            Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
            // Authentication 객체 기반으로 새 ATK 생성
            String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
            TokenInfo tokenInfo = TokenInfo.builder()
                    .grantType("Bearer")
                    .accessToken(newAccessToken)
                    .build();
            return tokenInfo;
        } else
            throw new BusinessLogicException(AuthException.TOKEN_NOT_FOUND);
    }
}
