package com.secureauction.auction.global.security;

import com.secureauction.auction.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    // UserController에서 userDetails.getUser()로 꺼내갈 수 있게 해주는 핵심 필드
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 회원의 권한(USER, ADMIN) 앞에 "ROLE_"을 붙여 Spring Security 규격에 맞게 전달합니다.
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getLoginId(); // Spring Security는 기본적으로 식별자를 'username'으로 부릅니다. 우리는 loginId를 사용합니다.
    }

    // --- 아래는 계정 상태에 대한 설정 (현재는 모두 활성화 상태로 true 반환) ---

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}