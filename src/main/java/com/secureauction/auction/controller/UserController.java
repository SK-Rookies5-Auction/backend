package com.secureauction.auction.controller;

import com.secureauction.auction.dto.*;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/me/likes")
    public ApiResponse<Object> getMyLikes(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<AuctionDto.LikeListResponse> resultPage = userService.getMyWishlist(userDetails.getUser(), pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", resultPage.getContent());
        responseData.put("page_info", Map.of(
                "current_page", resultPage.getNumber(),
                "page_size", resultPage.getSize(),
                "total_pages", resultPage.getTotalPages(),
                "total_elements", resultPage.getTotalElements(),
                "is_first", resultPage.isFirst(),
                "is_last", resultPage.isLast(),
                "has_next", resultPage.hasNext(),
                "has_previous", resultPage.hasPrevious()
        ));

        return ApiResponse.success(responseData, "마이페이지 관심 내역을 성공적으로 조회했습니다.");
    }
}
