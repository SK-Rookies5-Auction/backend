package com.secureauction.auction.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 헤더에서 토큰을 꺼내옵니다.
        String token = jwtTokenProvider.resolveToken(request);

        // 2. 토큰이 존재하고, 유효성 검사를 통과했다면
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰에서 인증 정보를 꺼내와서 시큐리티 컨텍스트(안전지대)에 저장합니다.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 4. 다음 필터로 부드럽게 넘겨줍니다.
        filterChain.doFilter(request, response);
    }
}