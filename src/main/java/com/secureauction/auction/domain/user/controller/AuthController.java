package com.secureauction.auction.domain.user.controller;

import com.secureauction.auction.domain.user.dto.request.LoginRequest;
import com.secureauction.auction.domain.user.dto.request.SignUpRequest;
import com.secureauction.auction.domain.user.dto.response.LoginResponse;
import com.secureauction.auction.domain.user.service.AuthService;
import com.secureauction.auction.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<Object> signup(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ApiResponse.success(null, "회원가입이 성공적으로 완료되었습니다.");
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response, "로그인 성공");
    }
}
