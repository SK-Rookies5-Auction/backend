package com.secureauction.auction.controller;

import com.secureauction.auction.dto.*;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me/summary")
    public ApiResponse<UserSummaryResponse> getMySummary(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserSummaryResponse response = userService.getUserSummary(userDetails.getUser().getId());
        return ApiResponse.success(response, "마이페이지 요약 정보를 성공적으로 조회했습니다.");
    }

    @PatchMapping("/password")
    public ApiResponse<Object> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(userDetails.getUser().getId(), request);
        return ApiResponse.success(null, "비밀번호가 성공적으로 변경되었습니다.");
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse response = userService.getUserInfo(userDetails.getUser().getId());
        return ApiResponse.success(response, "내 정보 조회 성공");
    }

    @PutMapping("/me")
    public ApiResponse<Object> updateInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(userDetails.getUser().getId(), request);
        return ApiResponse.success(null, "회원 정보가 수정되었습니다.");
    }
}
