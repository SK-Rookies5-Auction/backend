package com.secureauction.auction.domain.user.service;

import com.secureauction.auction.domain.user.dto.request.LoginRequest;
import com.secureauction.auction.domain.user.dto.request.SignUpRequest;
import com.secureauction.auction.domain.user.dto.response.LoginResponse;
import com.secureauction.auction.domain.user.entity.User;
import com.secureauction.auction.domain.user.entity.UserRole;
import com.secureauction.auction.domain.user.repository.UserRepository;
import com.secureauction.auction.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signUp(SignUpRequest request) {
        // 명세서 4. 예외처리(DUPLICATE_RESOURCE) 검증 로직
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        User user = User.builder()
                .loginId(request.getLoginId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(user.getLoginId(), user.getRole().name());

        return LoginResponse.builder()
                .accessToken(token)
                .user(LoginResponse.UserDto.builder()
                        .id(user.getId())
                        .role(user.getRole().name())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .build())
                .build();
    }
}