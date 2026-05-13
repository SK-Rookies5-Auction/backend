package com.secureauction.auction.controller;

import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.LoginRequest;
import com.secureauction.auction.dto.LoginResponse;
import com.secureauction.auction.dto.SignUpRequest;
import com.secureauction.auction.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/check-login-id")
    public ApiResponse<Boolean> checkLoginId(@RequestParam(name = "login_id") String loginId) {
        boolean isAvailable = authService.isLoginIdAvailable(loginId);
        String message = isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.";
        return ApiResponse.success(isAvailable, message);
    }

    @GetMapping("/check-nickname")
    public ApiResponse<Boolean> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = authService.isNicknameAvailable(nickname);
        String message = isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
        return ApiResponse.success(isAvailable, message);
    }

    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        boolean isAvailable = authService.isEmailAvailable(email);
        String message = isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
        return ApiResponse.success(isAvailable, message);
    }
}
