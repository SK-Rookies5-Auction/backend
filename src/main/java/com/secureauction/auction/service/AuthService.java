package com.secureauction.auction.service;

import com.secureauction.auction.domain.User;
import com.secureauction.auction.domain.UserRole;
import com.secureauction.auction.dto.LoginRequest;
import com.secureauction.auction.dto.LoginResponse;
import com.secureauction.auction.dto.SignUpRequest;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.global.security.JwtTokenProvider;
import com.secureauction.auction.repository.UserRepository;
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
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }

        User user = User.builder()
                .loginId(request.getLoginId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(UserRole.ROLE_USER)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PASSWORD));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
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

    @Transactional(readOnly = true)
    public boolean isLoginIdAvailable(String loginId) {
        return !userRepository.existsByLoginId(loginId);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}
