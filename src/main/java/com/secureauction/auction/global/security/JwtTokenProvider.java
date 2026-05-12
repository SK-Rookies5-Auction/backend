package com.secureauction.auction.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    // Access Token 만료시간: 1시간
    private final long accessTokenValidTime = 60 * 60 * 1000L;

    // Refresh Token 만료시간: 7일
    private final long refreshTokenValidTime = 7 * 24 * 60 * 60 * 1000L;

    private Key key;
    private final CustomUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 1. Access Token 생성
    public String createToken(String loginId, String role) {
        Claims claims = Jwts.claims().setSubject(loginId); // Payload에 들어갈 정보 (아이디)
        claims.put("role", role); // 권한 정보
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidTime)) // 1시간 후 만료
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Refresh Token 생성
    // Refresh Token은 오직 Access Token을 재발급받는 용도이므로 권한(role) 정보를 넣지 않고 가볍게 만듭니다.
    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime)) // 7일 후 만료
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 3. JWT 토큰에서 인증 정보 조회 (토큰이 유효하면 SecurityContext에 넣을 객체 반환)
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserLoginId(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 4. 토큰에서 회원 아이디(loginId) 추출
    public String getUserLoginId(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 5. Request의 Header에서 token 값을 가져옵니다. ("Authorization" : "Bearer 토큰값")
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 글자 빼고 진짜 토큰만 추출
        }
        return null;
    }

    // 6. 토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우 (이때 클라이언트는 Refresh Token으로 재발급을 요청해야 함)
            return false;
        } catch (Exception e) {
            // 토큰이 변조되었거나 형식이 잘못된 경우
            return false;
        }
    }
}